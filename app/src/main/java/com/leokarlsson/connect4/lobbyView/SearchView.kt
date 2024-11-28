package com.leokarlsson.connect4.lobbyView

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.IconButton
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.util.UUID



@Composable
fun SearchScreen(onSearch: (String) -> Unit){
    var searchPlayers by remember{ mutableStateOf("") }

    OutlinedTextField(
        value = searchPlayers,
        onValueChange = {newSearch ->
            searchPlayers = newSearch
            onSearch(newSearch)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon"
            )
        },
        placeholder = { Text("Search for Players...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(navController: NavController, gameTag: String){
    var searchResults by remember { mutableStateOf<List<Map<String, Any>>>(emptyList())}

    Column{
        TopAppBar(
            title = {Text("Search Players")},
            navigationIcon = {
                IconButton(onClick = {navController.popBackStack()}){
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
        )

        SearchScreen { query ->
            if(query.isNotBlank()){
                searchForPlayers(query){ results ->
                    searchResults = results
                }
            } else {
                searchResults = emptyList()
            }
        }

        Column(modifier = Modifier.padding(16.dp)){
            if(searchResults.isEmpty()){
                Text("No Players Found.")
            } else {
                searchResults.forEach { player ->
                    val username = player["Username"] as? String ?: "Unknown"
                    OtherPlayerInfo(receiverName = username, senderName = gameTag, navController)
                }
            }
        }
    }
}

@Composable
fun OtherPlayerInfo(receiverName: String, senderName: String, navController: NavController){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ){
        Text(
            text = receiverName,
            modifier = Modifier.padding(end = 8.dp)
        )
        IconButton(
            onClick = {
                navController.navigate("onlinePlayersAccount")},
            modifier = Modifier.padding(start = 8.dp)
        ){
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Player Account for $receiverName"
            )
        }
        Spacer(modifier = Modifier.padding(10.dp))
        Button(onClick = {
            val db = FirebaseFirestore.getInstance()
            val gameID = UUID.randomUUID().toString()
            val request = mapOf(
                "GameID" to gameID,
                "Sender" to senderName,
                "Receiver" to receiverName,
                "Status" to "Pending",
            )
            db.collection("GameRequest").add(request)
                .addOnSuccessListener{
                    println("Game request sent successfully")
                }
                .addOnFailureListener{
                    println("Failed to send game request")
                }
        }){
            Text("Challenge")
        }
    }
}

fun searchForPlayers(query: String, onResults: (List<Map<String, Any>>) -> Unit){
    val db = FirebaseFirestore.getInstance()

    db.collection("User")
        .whereGreaterThanOrEqualTo("Username", query)
        .whereLessThanOrEqualTo("Username", query + "\uf8ff")
        .get()
        .addOnSuccessListener { result ->
            val players = result.documents.map { it.data ?: emptyMap<String, Any>()}
            onResults(players)
        }
        .addOnFailureListener { exception ->
            println("Error searching for players: ${exception.message}")
            onResults(emptyList())
        }
}