package com.leokarlsson.connect4.lobbyView

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import com.leokarlsson.connect4.gameEngine.GameInfo
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay

class GameViewModel : ViewModel(){
    private val db = Firebase.firestore

    private val _gameMap = MutableStateFlow<Map<String?,GameInfo>>(emptyMap())
    val gameMap: StateFlow<Map<String?, GameInfo>> = _gameMap.asStateFlow()

    private val _playerMap = MutableStateFlow<Map<String?, UserInfo>>(emptyMap())
    val playerMap: StateFlow<Map<String?, UserInfo>> = _playerMap.asStateFlow()

    private val _acceptedGameIDState = MutableStateFlow<String?>(null)
    val acceptedGameIDState: StateFlow<String?> = _acceptedGameIDState

    private val _board = MutableStateFlow(Array(6){IntArray(7){-1} })
    val board: StateFlow<Array<IntArray>> = _board.asStateFlow()

    private val _currentPlayer = MutableStateFlow(0)
    val currentPlayer: StateFlow<Int> = _currentPlayer.asStateFlow()

    private val _winner = MutableStateFlow(-1)
    val winner: StateFlow<Int> = _winner.asStateFlow()

    private val _isMyTurn = MutableStateFlow(false)
    val isMyTurn: StateFlow<Boolean> = _isMyTurn.asStateFlow()

    private val _startedListenerUser = MutableStateFlow(false)
    val startedListenerUser: StateFlow<Boolean> = _startedListenerUser.asStateFlow()

    private var playerIndex = -1

    private var gameUpdatesListener: ListenerRegistration? = null
    private var requestUpdatesListener: ListenerRegistration? = null
    private var onlineGameListener: ListenerRegistration? = null

    fun listenUsers(){
        if(_startedListenerUser.value) return
        _startedListenerUser.value = true

        db.collection("User")
            .addSnapshotListener{snapshot, error ->
                if(error != null){
                    Log.e("listenUsers", "Error")
                    return@addSnapshotListener
                }
                _playerMap.value = snapshot?.documents?.associate{
                    it.id to it.toObject(UserInfo::class.java)!!
                } ?: emptyMap()
            }
    }

    fun listenGameUpdates(gameID: String){
        Log.d("FireStore", "Inside listenGameUpdates with gameID: $gameID")
        gameUpdatesListener?.remove()
        gameUpdatesListener = db.collection("game")
            .whereEqualTo("gameID", gameID)
            .addSnapshotListener{snapshot, error ->
                if(error != null){
                    Log.e("FireStore", "Error fetching game updates: ${error.message}")
                    return@addSnapshotListener
                }
                if(snapshot == null || snapshot.isEmpty){
                    Log.d("FireStore", "No Games found")
                    return@addSnapshotListener
                }
                val updatedGameMap = snapshot.documents.mapNotNull{document ->
                    Log.d("FireStore", "Document Data from game collection: ${document.data}")
                    document.toObject(GameInfo::class.java)?.let{gameInfo -> gameInfo.gameID to gameInfo}
                }.toMap()
                Log.d("FireStore", "Updated gameMap: $updatedGameMap")
                _gameMap.value = updatedGameMap
            }
    }

    fun listenRequestUpdates(gameID: String){
        requestUpdatesListener?.remove()
        requestUpdatesListener = db.collection("GameRequest")
            .whereEqualTo("gameID", gameID)
            .whereEqualTo("status", "Accepted")
            .addSnapshotListener{snapshot, error ->
                if(error != null){
                    Log.e("FireStore", "Error fetching game requests: ${error.message}")
                    return@addSnapshotListener
                }
                if(snapshot == null || snapshot.isEmpty){
                    Log.d("FireStore", "No accepted game requests found")
                    return@addSnapshotListener
                }
                snapshot.documents.forEach{document ->
                    Log.d("FireStore", "Document Data from GameRequest collection: ${document.data}")
                    val requestData = document.toObject(RequestInfo::class.java)
                    if(requestData != null){
                        _acceptedGameIDState.value = requestData.gameID
                        requestData.gameID?.let { listenGameUpdates(it) }
                    }else{
                        Log.e("FireStore", "Malformed GameRequest document detected")
                    }
                }
            }
    }

    fun acceptGameRequest(userInfo: UserInfo?, gameID:String, challenger: String, requestID: String, onComplete: () -> Unit){
        Log.d("FireStore", "Accepting game request with gameID $gameID")

        val emptyBoard = List(6*7){-1L}
        val gameState = mapOf(
            "gameID" to gameID,
            "player1" to userInfo?.gameTag,
            "player2" to "",
            "currentPlayer" to 0,
            "winner" to -1,
            "loser" to "",
            "gameIsReady" to false,
            "board" to emptyBoard,
            "lastMove" to mapOf<String, Long>()
        )
        db.collection("GameRequest").document(requestID).update("status", "Accepted")
        db.collection("game").add(gameState)
            .addOnSuccessListener{
                joinGame(gameID, challenger)
                onComplete()
            }
            .addOnFailureListener{e ->
                Log.e("Failure", "Error accepting game Request", e)
            }
    }

    private fun joinGame(gameID: String, playerName: String){
        db.collection("game")
            .whereEqualTo("gameID", gameID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FireStore", "Error fetching game updates: ${error.message}")
                    return@addSnapshotListener
                }
                snapshot?.documents?.forEach { document ->
                    val docID = document.id
                    db.collection("game").document(docID)
                        .update(mapOf("player2" to playerName, "gameIsReady" to true))
                }
            }
    }

    fun onlineGameStateListener(gameID: String, gameTag: String){
        Log.d("OnlineFireStore", "gameID: $gameID")
        onlineGameListener?.remove()
        onlineGameListener = db.collection("game")
            .whereEqualTo("gameID", gameID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OnlineGame", "Error fetching game updates: ${error.message}")
                    return@addSnapshotListener
                }
                snapshot?.documents?.forEach { document ->
                    Log.d("FireStore", "Doc data: ${document.data}")
                    val data = document.data ?: return@forEach
                    try {
                        val flatBoard = data["board"] as? List<Long> ?: emptyList()
                        val boardData = Array(6) { IntArray(7) }
                        for (i in 0 until 6) {
                            for (j in 0 until 7) {
                                boardData[i][j] = flatBoard[i * 7 + j].toInt()
                            }
                        }

                        if (boardData.isNotEmpty()) {
                            _board.value = boardData
                        } else {
                            Log.e("FireStore", "Malformed board data detected")
                        }

                        val player1 = data["player1"] as? String ?: ""
                        val player2 = data["player2"] as? String ?: ""

                        Log.d("turn", "player1: $player1")
                        Log.d("turn", "player2: $player2")
                        Log.d("turn", "gameTag: $gameTag")
                        Log.d("turn", "_currentPlayer: ${_currentPlayer.value}")

                        if(playerIndex == -1){
                            playerIndex = if(gameTag == player1) 1 else if (gameTag == player2) 0 else -1
                        }

                        val currentPlayerIndex = (data["currentPlayer"] as? Long)?.toInt()
                        if(currentPlayerIndex != null && currentPlayerIndex != _currentPlayer.value){
                            _currentPlayer.value = currentPlayerIndex
                        }
                        _isMyTurn.value = currentPlayerIndex == playerIndex

                        Log.d("PlayerIndex", "playerIndex: $playerIndex")

                        Log.d("turn", "isMyTurn: ${_isMyTurn.value}")
                        _winner.value = (data["winner"] as? Long)?.toInt() ?: -1
                    } catch(e: Exception){
                        Log.e("ParsingError", "Error parsing board data: ${e.message}")
                    }
                }
            }
    }

    fun makeMove(gameID: String, column: Int){
        if(_winner.value != -1 || !_isMyTurn.value) return

        val updatedBoard = _board.value.flatMap{it.toList()}.toMutableList()
        for(row in 5 downTo 0){
            val index = row * 7 + column
            if(updatedBoard[index] == -1){
                updatedBoard[index] = _currentPlayer.value
                val isWin = checkForWin(row, column, _currentPlayer.value)
                val nextPlayer = if(_currentPlayer.value == 0) 1 else 0
                _winner.value = if(isWin) _currentPlayer.value else -1

                _currentPlayer.value = nextPlayer
                _board.value = updatedBoard.chunked(7).map { it.toIntArray()}.toTypedArray()

                Log.d("OnlineGame", "makeMove _currentPlayer: ${_currentPlayer.value}")

                db.collection("game")
                    .whereEqualTo("gameID", gameID)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        querySnapshot.documents.forEach { document ->
                            val docID = document.id
                            db.collection("game").document(docID)
                                .update(
                                    "board", updatedBoard,
                                    "currentPlayer", nextPlayer,
                                    "winner", _winner.value,
                                    "lastMove", mapOf("row" to row, "column" to column)
                                )
                        }
                    }
                    .addOnFailureListener{e ->
                        Log.e("FireStore", "Error accessing game updates", e)
                    }
                return
            }

        }
    }

    private fun checkForWin(row: Int, column: Int, player: Int): Boolean{
        fun countConsecutive(dx: Int, dy: Int): Int{
            var count = 0
            var r = row + dx
            var c = column + dy
            while(r in 0..5 && c in 0..6 && _board.value[r][c] == player){
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

    fun resetGame(gameID: String, player1Tag: String, player2Tag: String){
        val emptyBoard = List(6*7){-1L}
        db.collection("game")
            .whereEqualTo("gameID", gameID)
            .addSnapshotListener{snapshot, error ->
                if(error != null){
                    Log.e("ResetGame", "Error")
                    return@addSnapshotListener
                }
                snapshot?.documents?.forEach{ document ->
                    val docID = document.id

                    db.collection("game").document(docID).set(
                        mapOf(
                            "gameID" to gameID,
                            "player1" to player1Tag,
                            "player2" to player2Tag,
                            "board" to emptyBoard,
                            "currentPlayer" to 0,
                            "winner" to -1,
                            "loser" to "",
                            "lastMove" to mapOf<String, Long>(),
                            "gameIsReady" to true
                        )
                    )
                }
            }
    }


    override fun onCleared(){
        super.onCleared()
        gameUpdatesListener?.remove()
        requestUpdatesListener?.remove()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyGameScreen(navController: NavController, gameID: String?, viewModel: GameViewModel){
    val db = Firebase.firestore
    val gameIDForListener = gameID ?: ""
    val acceptedGameID by viewModel.acceptedGameIDState.collectAsStateWithLifecycle()
    val updatedGameMap by viewModel.gameMap.collectAsStateWithLifecycle()
    val playerMap by viewModel.playerMap.collectAsStateWithLifecycle()
    val updatedGameInfo = updatedGameMap[gameIDForListener]
    val userInfo = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<UserInfo>("userInfo")
    var showDialog by remember { mutableStateOf(false) }
    Log.d("LobbyScreen", "Extracted gameID: $gameID")
    Log.d("LobbyScreen", "userInfo value: $userInfo")


    LaunchedEffect(Unit){
        Log.d("LobbyScreen", "Listening for gameID: $gameID")
        Log.d("LobbyScreen", "Going into listenForGameUpdates")
        viewModel.listenRequestUpdates(gameIDForListener)
    }

    LaunchedEffect(acceptedGameID){
        Log.d("LobbyScreen", "Accepted gameID updated: $acceptedGameID")
        if(!acceptedGameID.isNullOrEmpty()){
            showDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("account"){
                            navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
                        }}) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "account",
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {navController.navigate("search"){
                navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
            } },
                modifier = Modifier.offset(y = (-80).dp)){
                Icon(imageVector = Icons.Default.Search, contentDescription = "search")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Players: ", modifier = Modifier.padding(start = 15.dp))

                Button(onClick = { navController.navigate("localGame") }) {
                    Text("Local Game")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("GameRequestBox"){
                    navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
                } }){
                    Text("Game Requests")
                }

                if(showDialog && acceptedGameID != null){
                    Log.d("LobbyScreen", "acceptedGameID: $acceptedGameID")
                    db.collection("game")
                        .whereEqualTo("gameID", acceptedGameID)
                        .addSnapshotListener{snapshots, error ->
                            if(error != null){
                                println("Error listening")
                                return@addSnapshotListener
                            }
                            snapshots?.documents?.firstOrNull()?.let { document ->
                                val docID = document.id
                                Log.d("LobbyScreen", "DocID value: $docID")
                                db.collection("game").document(docID)
                                    .update("gameIsReady", true, "player2", userInfo?.gameTag)
                            }
                        }

                    GameJoinDialog(
                        onJoinGame={
                            Log.d("LobbyScreen", "Navigating to WaitingForGame with gameID: $gameID")
                            Log.d("LobbyScreen", "Sending UpdatedGameMap: $updatedGameMap")
                            Log.d("LobbyScreen", "Sending GameInfo: $updatedGameInfo")
                            Log.d("listenUsers", "Sending PlayerMap: $playerMap")
                        navController.navigate("waitingForGame/${gameID}"){
                            navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
                        }
                            showDialog = false
                    },
                        onDismiss = {
                            showDialog = false
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun GameJoinDialog(onJoinGame: () -> Unit, onDismiss: () -> Unit){
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("Join Game")},
        text = {Text("Are you sure you want to join this game?")},
        confirmButton = {
            Button(
                onClick = {
                    onJoinGame()
                }
            ){
                Text("join")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss){
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WaitingForGame(navController: NavController, gameID: String, viewModel: GameViewModel) {
    val gameMap by viewModel.gameMap.collectAsStateWithLifecycle()
    val gameInfo = gameMap[gameID]
    val userInfo = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<UserInfo>("userInfo")

    Log.d("WaitingForGame", "gameID: $gameID")
    Log.d("WaitingForGame", "gameMap: $gameMap")
    Log.d("WaitingForGame", "gameInfo: $gameInfo")
    Log.d("WaitingForGame", "playerMap: ${viewModel.playerMap}")

    if(gameInfo == null){
        LoadingUI()
    }

    LaunchedEffect(gameInfo){
        Log.d("WaitingForGame", "LaunchedEffect triggered")
        if(gameInfo?.player1 != null && gameInfo.player2 != null && gameInfo.gameIsReady == true){
            delay(1000)
            Log.d("WaitingForGame", "Navigating to OnlineGame")
            navController.navigate("onlineGame/${gameID}"){
                navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
            }
        }else{
            Log.d("WaitingForGame", "GameInfo is not ready or missing players")
        }
    }
}

@Composable
fun LoadingUI(){
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ){
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController){
    val userInfo = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<UserInfo>("userInfo")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("account"){
                            navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
                        }}) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "account",
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {navController.navigate("search"){
                navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
            } },
                modifier = Modifier.offset(y = (-80).dp)){
                Icon(imageVector = Icons.Default.Search, contentDescription = "search")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Players: ", modifier = Modifier.padding(start = 15.dp))

                Button(onClick = { navController.navigate("localGame") }) {
                    Text("Local Game")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("GameRequestBox"){
                    navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
                } }){
                    Text("Game Requests")
                }
            }
        }
    )
}