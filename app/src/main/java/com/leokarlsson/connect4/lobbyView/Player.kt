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

@Composable
fun CreatePlayerScreen(navController:NavController){
    var gameTag by remember {mutableStateOf("")}
    val isTitleValid = gameTag.length > 3

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
                    navController.navigate("lobby/${uniqueID}/${gameTag}")
                }
            }){
                Text("Create Player")
            }
        }
    }
}
