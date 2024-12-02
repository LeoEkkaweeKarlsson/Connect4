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

@Composable
fun GameOverView(player: Int, onRestart: () -> Unit, navController: NavController, uniqueID: String, gameTag: String, uniqueGameID: String){
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
                onClick = {
                    onRestart()}){
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

