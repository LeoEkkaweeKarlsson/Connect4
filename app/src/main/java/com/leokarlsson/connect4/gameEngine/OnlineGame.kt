package com.leokarlsson.connect4.gameEngine

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leokarlsson.connect4.lobbyView.GameViewModel
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import com.leokarlsson.connect4.lobbyView.UserInfo

@Composable
fun OnlineGame(navController: NavController, gameID: String, viewModel: GameViewModel){
    val board by viewModel.board.collectAsStateWithLifecycle()
    val winner by viewModel.winner.collectAsStateWithLifecycle()
    val playerMapState by viewModel.playerMap.collectAsStateWithLifecycle()
    val isMyTurn by viewModel.isMyTurn.collectAsStateWithLifecycle()
    val userInfo = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<UserInfo>("userInfo")


    val uniqueID = playerMapState.keys.find{it == userInfo?.uniqueID} ?: ""
    val gameTag = playerMapState[uniqueID]?.gameTag ?: ""

    Log.d("OnlineGame", "Navigated Successfully")
    Log.d("OnlineGame", "playerMapState: $playerMapState")
    Log.d("OnlineGame", "uniqueID: $uniqueID")
    Log.d("OnlineGame", "gameTag: $gameTag")
    Log.d("OnlineGame", "uniqueGameID: $gameID")

    LaunchedEffect(Unit){
        viewModel.onlineGameStateListener(gameID, gameTag)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        BoardCreation(board = board){ column ->
            if(winner == -1 && isMyTurn){
                Log.d("OnlineGame", "gameTag: $gameTag")
                viewModel.makeMove(gameID, column)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if(winner != -1) {
            GameOverView(winner, {
                viewModel.resetGame(gameID)
            }, navController, uniqueID, gameTag, gameID)
        }else{
            Text(
                text = if(isMyTurn) "Your Turn" else "Waiting for opponent...",
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                navController.navigate("lobby"){
                    popUpTo("lobby"){inclusive = true}
                }
            }
        ){
            Text(text = "Withdraw")
        }
    }
}