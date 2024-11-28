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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import java.util.UUID
import android.util.Log



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRequestListener(currentUsername: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var request by remember { mutableStateOf(false) }
    var senderUsername by remember { mutableStateOf("") }
    var requestID by remember { mutableStateOf("") }

    Column {
        TopAppBar(
            title = { Text("Game Requests") },
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
    DisposableEffect(Unit) {
        val listener = db.collection("GameRequest")
            .whereEqualTo("Receiver", currentUsername)
            .whereEqualTo("Status", "Pending")
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
        GameRequestBox(senderUsername = senderUsername, currentUsername = currentUsername, navController = navController, requestID = requestID)
     }else{
         Box(modifier = Modifier
             .fillMaxSize()
             .wrapContentSize(Alignment.Center)
         ){
             Text(
                 text = "No Game Requests",
                 modifier = Modifier.align(Alignment.Center)
             )
         }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRequestBox(senderUsername: String, currentUsername: String, navController: NavController, requestID: String){
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Game Request")},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }){
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back")
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
                value = senderUsername,
                onValueChange = {},
                label = { Text("Sender Username") },
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
                    Text("Reject")
                }
                Button(
                    onClick = {
                        val gameRequestRef = db.collection("GameRequest").document(requestID)

                        gameRequestRef.get().addOnSuccessListener { document ->
                            if (document.exists()) {
                                val gameID = document.getString("GameID") ?: ""

                                val gameState = mapOf(
                                    "GameID" to gameID,
                                    "Player1" to "$currentUsername",
                                    "Player2" to "Pending",
                                    "Player x Turn" to "",
                                    "Winner" to "",
                                    "Loser" to ""
                                )
                                db.collection("game").add(gameState)
                                    .addOnSuccessListener {
                                        navController.navigate("onlineGame/${currentUsername}/${senderUsername}")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FireStore", "Error adding gameState: $e")
                                    }

                                gameRequestRef.update("Status", "Accepted")
                            } else {
                                Log.e("FireStore", "GameRequest document does not exist")
                            }
                        }.addOnFailureListener { e ->
                            Log.e("FireStore", "Error getting GameRequest document: $e")
                        }
                    }
                ){
                    Text("Accept")
                }
            }
        }
    }
}


