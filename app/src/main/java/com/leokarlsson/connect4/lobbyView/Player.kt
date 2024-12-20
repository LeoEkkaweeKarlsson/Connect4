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
import java.io.Serializable
import androidx.compose.runtime.LaunchedEffect


data class UserInfo(
    val gameTag: String? = null,
    val uniqueID: String? = null,
    val status: String? = null,
) : Serializable

@Composable
fun CreatePlayerScreen(navController:NavController, viewModel: GameViewModel){
    var gameTag by remember {mutableStateOf("")}
    val isTitleValid = gameTag.length > 3
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    LaunchedEffect(Unit){
        viewModel.listenUsers()
    }


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
                        "uniqueID" to uniqueID,
                        "gameTag" to gameTag,
                        "status" to "Online",
                    )
                    firestore.collection("User")
                        .document(uniqueID)
                        .set(playerData)
                        .addOnSuccessListener {
                            val userInfo = UserInfo(gameTag = gameTag, uniqueID = uniqueID, status = "Online")
                            Toast.makeText(context, "Player Created!", Toast.LENGTH_SHORT).show()
                            navController.navigate("lobby"){
                                navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
                            }
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
                        .whereEqualTo("gameTag", gameTag)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val document = querySnapshot.documents[0]
                                val uniqueID = document.getString("uniqueID") ?: ""

                                firestore.collection("User")
                                    .document(document.id)
                                    .update("status", "Online")
                                    .addOnSuccessListener {
                                        val userInfo = UserInfo(gameTag = gameTag, uniqueID = uniqueID, status = "Online")
                                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("lobby"){
                                            navController.currentBackStackEntry?.savedStateHandle?.set("userInfo", userInfo)
                                        }
                                    }
                                    .addOnFailureListener{ exception ->
                                            Toast.makeText(context, "Failed to login: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
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
