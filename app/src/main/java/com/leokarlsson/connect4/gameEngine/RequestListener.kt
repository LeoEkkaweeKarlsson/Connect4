package com.leokarlsson.connect4.gameEngine

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.DisposableEffect
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue





@Composable
fun GameRequestListener(currentUsername: String, navController: NavController){
    val db = FirebaseFirestore.getInstance()
    var showDialog by remember { mutableStateOf(false) }
    var senderUsername by remember { mutableStateOf("") }
    var requestID by remember { mutableStateOf("") }

    DisposableEffect(Unit){
        val listener = db.collection("GameRequest")
            .whereEqualTo("Receiver", currentUsername)
            .whereEqualTo("Status", "Pending")
            .addSnapshotListener { snapshots, error ->
                if(error != null){
                    println("Error listening for game request: ${error.message}")
                    return@addSnapshotListener
                }
                snapshots?.documents?.firstOrNull()?.let { document ->
                    senderUsername = document.getString("Sender") ?: ""
                    requestID = document.id
                    showDialog = true
                }
            }

        onDispose{
            listener.remove()
        }
    }

    if(showDialog){
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {Text("Game Request")},
            text = { Text("$senderUsername sent you a game request!")},
            confirmButton = {
                Button(onClick = {
                    db.collection("GameRequest").document(requestID)
                        .update("Status", "Accepted"
                        ,"CheckReceived", "received")
                        .addOnSuccessListener {
                            navController.navigate("OnlineGameInit")
                        }
                    showDialog = false
                }){
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(onClick = {
                    db.collection("GameRequest").document(requestID)
                        .update("Stauts", "Declined")
                    showDialog = false
                }){
                    Text("Decline")
                }
            }
        )
    }

}