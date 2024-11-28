package com.leokarlsson.connect4.gameEngine

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.FieldValue
import java.util.UUID
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineGameInit(player1: String, player2: String, navController: NavController, gameTag: String){
    val gameID = UUID.randomUUID().toString()
    var gameReady by remember {mutableStateOf(false)}
    val firestore = FirebaseFirestore.getInstance()

    Column{
        TopAppBar(
            title = {Text("")},
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }){
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
    }

    DisposableEffect(Unit){
        val listener = firestore.collection("game").document(gameID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error listening for game state: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    gameReady = true
                }
            }
        onDispose{
            listener.remove()
        }
    }
    if(gameReady){
        OnlineGame(players = listOf(player1, player2),
            navController = navController,
            uniqueID = player1,
            gameTag = gameTag,
            uniqueGameID = gameID
        )
    }else{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "Waiting for the game to start...")
        }
    }
}

@Composable
fun OnlineGame(players: List<String>, navController: NavController, uniqueID: String, gameTag: String, uniqueGameID: String){
    var board by remember { mutableStateOf(Array(6){IntArray(7){-1} }) }
    var currentPlayer by remember { mutableIntStateOf(0)}
    var winner by remember { mutableIntStateOf(-1)}
    val db = FirebaseFirestore.getInstance()
    var isWinRecorded by remember {mutableStateOf(false)}

    DisposableEffect(Unit){
        val gameListener = db.collection("game").document(uniqueGameID)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    println("Error listening for game state: ${error.message}")
                    return@addSnapshotListener
                }
                if(snapshot != null && snapshot.exists()){
                    val data = snapshot.data ?: return@addSnapshotListener
                    board = (data["board"] as? List<List<Long>>)
                        ?.map { row -> row.map {cell -> cell.toInt() }.toIntArray()}
                        ?.toTypedArray() ?: board
                    currentPlayer = data["currentPlayer"] as Int
                    winner = data["winner"] as Int
                }
            }
        onDispose{gameListener.remove()}
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        BoardCreation(board = board){column ->
            if(winner == -1){
                makeMoveOnline(board, column, currentPlayer, uniqueGameID, players){ isWin ->
                    if(isWin){
                        winner = currentPlayer
                    }else{
                        currentPlayer = (currentPlayer + 1) % players.size
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if(winner != -1 && !isWinRecorded){
            if(winner == players.indexOf(uniqueID)) {
                db.collection("User").document(uniqueID)
                    .update(
                        "Win", FieldValue.increment(1)
                        , "Games Played", FieldValue.increment(1)
                    )
            }else{
                db.collection("User").document(uniqueID)
                    .update(
                        "Loss", FieldValue.increment(1)
                        ,"Games Played", FieldValue.increment(1)
                    )
            }
            isWinRecorded = true
            GameOverView(player = winner, onRestart = {
                    board = Array(6){IntArray(7){-1}}
                    currentPlayer = 0
                    winner = -1
                    isWinRecorded = false
                },
                navController = navController,
                uniqueID = uniqueID,
                gameTag = gameTag,
                uniqueGameID = uniqueGameID
            )

        }else{
            Text(text = "Player ${currentPlayer + 1}'s turn", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                navController.navigate("lobby/${uniqueID}/${gameTag}"){
                    popUpTo("lobby/${uniqueID}/${gameTag}"){inclusive = true}
                }
            }
        ){
            Text(text = "Withdraw")
        }
    }
}

fun makeMoveOnline(board: Array<IntArray>, column: Int, player: Int, uniqueGameID: String, players: List<String>, onMoveResult: (Boolean) -> Unit){
    val db = FirebaseFirestore.getInstance()
    db.runTransaction { transaction ->
        val gameRef = db.collection("game").document(uniqueGameID)
        val snapshot = transaction.get(gameRef)

        val currentBoard = (snapshot["board"] as? List<List<Long>>)
            ?.map { row -> row.map { cell -> cell.toInt() }.toIntArray() }
            ?.toTypedArray() ?: board
        val currentWinner = (snapshot["winner"] as? Long)?.toInt() ?: -1
        val currentPlayer = (snapshot["currentPlayer"] as? Long)?.toInt() ?: 0

        if (currentWinner != -1 || currentPlayer != player){
            return@runTransaction
        }

        for (row in 5 downTo 0) {
            if (currentBoard[row][column] == -1) {
                currentBoard[row][column] = player
                val isWin = checkForWinOnline(currentBoard, row, column, player)

                transaction.update(
                    gameRef,
                    mapOf(
                        "board" to currentBoard.map { it.toList() },
                        "currentPlayer" to (player + 1) % players.size,
                        "winner" to if (isWin) player else -1

                    )
                )
                onMoveResult(isWin)
                return@runTransaction
            }
        }
        throw Exception("Columns is full")
    }.addOnFailureListener {
        println("Move update Failed: ${it.message}")
    }
}

fun checkForWinOnline(board: Array<IntArray>, row: Int, column: Int, player: Int): Boolean{
    fun countConsecutive(dx: Int, dy: Int): Int{
        var count = 0
        var r = row + dx
        var c = column + dy
        while(r in 0..5 && c in 0..6 && board[r][c] == player){
            count++
            r += dx
            c += dy
        }
        return count
    }

    val directions = listOf(
        Pair(1,0), Pair(0,1), Pair(1,1), Pair(1,-1)
    )
    return directions.any{(dx,dy) ->
        1 + countConsecutive(dx, dy) + countConsecutive(-dx, -dy) >= 4
    }
}

