package com.leokarlsson.connect4.gameEngine

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import android.util.Log
import com.leokarlsson.connect4.lobbyView.UserInfo
import java.io.Serializable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.leokarlsson.connect4.lobbyView.GameViewModel


data class GameInfo(
    val player1: String? = null,
    val player2: String? = null,
    val gameID: String? = null,
    val currentPlayer: Int? = null,
    val winner: Long? = null,
    val loser: String? = null,
    val gameIsReady: Boolean? = false,
    val board: List<Long> = emptyList(),
    val lastMove: Map<String, Long> = emptyMap()
) : Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRequestScreen(navController: NavController, viewModel: GameViewModel){
    val db = Firebase.firestore
    val userInfo = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<UserInfo>("userInfo")
    var challenger by remember { mutableStateOf("") }
    var requestID by remember { mutableStateOf("") }
    var gameID by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("GameRequest")
            .whereEqualTo("receiver", userInfo?.gameTag)
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    println("Error listening")
                    return@addSnapshotListener
                }
                snapshots?.documents?.firstOrNull()?.let { document ->
                    challenger = document.getString("challenger") ?: ""
                    gameID = document.getString("gameID") ?: ""
                    requestID = document.id
                    Log.d("Request", "Request received: $challenger")
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Game Requests")},
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
    ){padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ){
            OutlinedTextField(
                value = challenger,
                onValueChange = {},
                label = {Text("Challenger")},
                enabled = false,
                modifier = Modifier.padding(16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Button(
                    onClick = {
                        db.collection("GameRequest").document(requestID).delete()
                        navController.popBackStack()
                    }
                ){
                    Text("reject")
                }
                Button(
                    onClick = {
                        Log.d("Request", "gameID: $gameID")
                        Log.d("Request", "requestID: $requestID")
                        viewModel.acceptGameRequest(
                            userInfo,
                            gameID,
                            challenger,
                            requestID
                        ) {
                            viewModel.listenUsers()
                            viewModel.listenGameUpdates(gameID)
                            navController.navigate("waitingForGame/${gameID}"){
                                navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)

                            }
                        }
                    }
                ){
                    Text("Accept")
                }
            }
        }
    }
}

