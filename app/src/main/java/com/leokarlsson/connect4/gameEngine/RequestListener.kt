package com.leokarlsson.connect4.gameEngine

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import android.util.Log
import com.leokarlsson.connect4.lobbyView.UserInfo
import java.io.Serializable
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment

data class Players(
    val player1: String,
    val player2: String
) : Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRequestBox(navController: NavController){
    val db = FirebaseFirestore.getInstance()
    val userInfo = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<UserInfo>("userInfo")
    var senderUsername by remember { mutableStateOf("") }
    var requestID by remember { mutableStateOf("") }
    var request by remember { mutableStateOf(false) }
    val currentUsername = userInfo?.gameTag ?: ""


    DisposableEffect(Unit) {
        val listener = db.collection("GameRequest")
            .whereEqualTo("Receiver", userInfo?.gameTag)
            .whereEqualTo("Status", "Pending")
            .limit(1)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    println("Error listening for game request: ${error.message}")
                    return@addSnapshotListener
                }
                snapshots?.documents?.firstOrNull()?.let { document ->
                    senderUsername = document.getString("Sender") ?: ""
                    requestID = document.id
                    request = true
                }
            }
        onDispose {
            listener.remove()
        }
    }

    if(request) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Game Request") },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = senderUsername,
                    onValueChange = {},
                    label = { Text("Sender Username") },
                    enabled = false,
                    modifier = Modifier.padding(16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            db.collection("GameRequest").document(requestID).delete()
                            navController.popBackStack()
                            request = false
                        }
                    ) {
                        Text("Reject")
                    }
                    Button(
                        onClick = {
                            val playerInstance = Players(player1 = currentUsername, player2 = senderUsername)
                            val gameRequestRef = db.collection("GameRequest").document(requestID)
                            gameRequestRef.get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val gameID = document.getString("GameID") ?: ""

                                    val gameState = mapOf(
                                        "GameID" to gameID,
                                        "Player1" to currentUsername,
                                        "Player2" to "Pending",
                                        "CurrentPlayer" to "",
                                        "Winner" to "",
                                        "Loser" to "",
                                        "Board" to "",
                                        "GameReady" to "No"
                                    )
                                    db.collection("game").add(gameState)
                                        .addOnSuccessListener {
                                            navController.navigate("onlineGame") {
                                                navController.currentBackStackEntry?.savedStateHandle?.set("players", playerInstance)
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("FireStore", "Error adding gameState: $e")
                                        }

                                    gameRequestRef.update("Status", "Accepted")
                                    request = false
                                } else {
                                    Log.e("FireStore", "GameRequest document does not exist")
                                }
                            }.addOnFailureListener { e ->
                                Log.e("FireStore", "Error getting GameRequest document: $e")
                            }
                        }
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Game Request") },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ){ paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .wrapContentSize(Alignment.Center)
            ){
                Text(
                    text = "No game requests found",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}


