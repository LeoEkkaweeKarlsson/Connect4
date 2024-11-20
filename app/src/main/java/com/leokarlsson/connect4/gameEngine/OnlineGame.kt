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


@Composable
fun OnlineGameInit(player1: String, player2: String, navController: NavController, gameID: String){
   var gameReady by remember {mutableStateOf(false)}
    val firestore = FirebaseFirestore.getInstance()

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
            gameTag = gameID)
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
fun OnlineGame(players: List<String>, navController: NavController,uniqueID: String, gameTag: String){
    var board by remember { mutableStateOf(Array(6){IntArray(7){-1} }) }
    var currentPlayer by remember { mutableIntStateOf(0)}
    var winner by remember { mutableIntStateOf(-1)}

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        BoardCreation(board = board){column ->
            if(winner == -1){
                makeMove(board, column, currentPlayer){ isWin ->
                    if(isWin){
                        winner = currentPlayer
                    }else{
                        currentPlayer = (currentPlayer + 1) % players.size
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if(winner != -1){
            GameOverView(
                player = winner,
                onRestart = { board = Array(6){IntArray(7){-1}}
                currentPlayer = 0
                winner = -1
                },
                navController = navController,
                uniqueID = uniqueID,
                gameTag = gameTag
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
