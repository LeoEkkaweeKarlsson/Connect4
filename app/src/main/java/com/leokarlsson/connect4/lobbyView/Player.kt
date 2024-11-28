package com.leokarlsson.connect4.lobbyView

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.navigation.NavController
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.leokarlsson.connect4.gameEngine.GameRequestListener

@Composable
fun CreatePlayerScreen(navController:NavController){
    var gameTag by remember {mutableStateOf("")}
    val isTitleValid = gameTag.length > 3
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Scaffold{padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ){
            OutlinedTextField(
                value = gameTag,
                onValueChange = { newTitle ->
                    gameTag = newTitle
                },
                label = {Text("Name")},
                isError = !isTitleValid,
            )
            Button(onClick = {
                if(gameTag.isNotBlank() && isTitleValid){
                    val uniqueID = UUID.randomUUID().toString()
                    val playerData = hashMapOf(
                        "ID" to uniqueID,
                        "Username" to gameTag,
                        "Win" to 0,
                        "Loss" to 0,
                        "Draw" to 0,
                        "Games Played" to 0,
                        "LocalWin" to 0,
                        "Status" to "Online"
                    )
                    firestore.collection("User")
                        .document(uniqueID)
                        .set(playerData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Player Created!", Toast.LENGTH_SHORT).show()
                            navController.navigate("lobby/${uniqueID}/${gameTag}")
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to create player", Toast.LENGTH_SHORT).show()
                        }
                }
            }){
                Text("Create Player")
            }
            Button(onClick = {
                if (gameTag.isNotBlank() && isTitleValid) {
                    firestore.collection("User")
                        .whereEqualTo("Username", gameTag)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val document = querySnapshot.documents[0]
                                val uniqueID = document.getString("ID") ?: ""

                                firestore.collection("User")
                                    .document(document.id)
                                    .update("Status", "Online")
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("lobby/${uniqueID}/${gameTag}")
                                    }
                                    .addOnFailureListener({ exception ->
                                            Toast.makeText(context, "Failed to login: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    })
                            } else {
                                Toast.makeText(context, "Player not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Failed to login: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Invalid Username", Toast.LENGTH_SHORT).show()
                }
            }){
                Text("Login")
            }
        }
    }
}
