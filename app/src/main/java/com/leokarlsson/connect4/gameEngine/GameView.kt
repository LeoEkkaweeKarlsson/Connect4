package com.leokarlsson.connect4.gameEngine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.leokarlsson.connect4.lobbyView.GameViewModel
import android.util.Log


@Composable
fun GameOverViewOnline(winner: Int, navController: NavController, gameID: String, player1Tag: String, player2Tag: String){
    val viewModel = GameViewModel()
    Log.d("GameOverView", "winnerInit: $winner")
    Log.d("GameOverView", "player1Tag: $player1Tag")
    Log.d("GameOverView", "player2Tag: $player2Tag")

    fun converter(winner:Int): String{
        return when(winner){
            1 -> player1Tag
            0 -> player2Tag
            else -> "shit"
        }
    }

    val winnerUsername = converter(winner)
    Log.d("GameOverView", "winner: $winnerUsername")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ){
            Text(
                text = "Player $winnerUsername wins!",
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = {
                    viewModel.resetGame(gameID, player1Tag, player2Tag)
                    }){
                Text("Restart Game")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("lobby"){
                        popUpTo("lobby"){inclusive = true}
                    }
                }
            ){
                Text(text = "Return to Lobby")
            }
        }
    }
}

@Composable
fun GameOverView(player: Int, onRestart: () -> Unit, navController: NavController){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ){
            Text(
                text = "Player ${player + 1} wins!",
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = {onRestart() })
            {
                Text("Restart Game")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("lobby"){
                        popUpTo("lobby"){inclusive = true}
                    }
                }
            ){
                Text(text = "Return to Lobby")
            }
        }
    }
}
