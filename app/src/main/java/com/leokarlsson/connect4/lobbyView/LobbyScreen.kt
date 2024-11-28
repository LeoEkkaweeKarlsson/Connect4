package com.leokarlsson.connect4.lobbyView

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.DisposableEffect
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, uniqueID: String, gameTag: String){
    val db = FirebaseFirestore.getInstance()
    var requestAccepted by remember { mutableStateOf(false) }
    var requestID by remember { mutableStateOf("") }
    var senderUsername by remember { mutableStateOf("") }
    var gameID by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("account/${uniqueID}/${gameTag}")}) {
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
            FloatingActionButton(onClick = {navController.navigate("search/${gameTag}")},
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

                Button(onClick = { navController.navigate("localGame/${uniqueID}/${gameTag}") }) {
                    Text("Local Game")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("GameRequestBox/${gameTag}")}){
                    Text("Game Requests")
                }
            }
        }
    )
    DisposableEffect(Unit){
        val listener = db.collection("GameRequest")
            .whereEqualTo("Sender", gameTag)
            .whereEqualTo("Status", "Accepted")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    println("Error listening for game request: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshots?.documents?.isNotEmpty() == true) {
                    requestAccepted = true
                    requestID = snapshots.documents.first().id
                    senderUsername = snapshots.documents.first().getString("Sender") ?: ""
                    gameID = snapshots.documents.first().getString("GameID") ?: ""
                }
            }
        onDispose {
            listener.remove()
        }
    }

    if(requestAccepted){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ){
            Text(
                text = "Game Request Accepted",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row{
                Button(onClick = {
                    db.collection("game")
                        .whereEqualTo("GameID", gameID)
                        .get()
                        .addOnSuccessListener{querySnapshot ->
                            for(document in querySnapshot.documents){
                                db.collection("game").document(document.id).delete()
                            }
                        }
                        .addOnFailureListener{error ->
                            println("Error deleting game document: ${error.message}")
                        }

                    db.collection("GameRequest").document(requestID).delete()
                    navController.navigate("lobby/${uniqueID}/${gameTag}")
                }){
                    Text("Cancel Game")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    db.collection("game")
                        .whereEqualTo("GameID", gameID)
                        .get()
                        .addOnSuccessListener{querySnapshot ->
                            for(document in querySnapshot.documents){
                                db.collection("game").document(document.id).update("Player2", gameTag)
                            }
                        }
                    navController.navigate("onlineGame/${uniqueID}/${gameTag}")}
                ){
                    Text("Join Game")
                }
            }
        }
    }
}

