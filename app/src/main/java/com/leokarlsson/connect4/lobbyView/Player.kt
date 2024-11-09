package com.leokarlsson.connect4.lobbyView

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.navigation.NavController
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlayerScreen(navController:NavController){
    var title by remember {mutableStateOf("")}
    val isTitleValid = title.length > 3

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("createPlayer")}){
                Icon(Icons.Filled.Add, "Create Player")
            }
        }
    ){padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ){
            OutlinedTextField(
                value = title,
                onValueChange = { newTitle ->
                    title = newTitle
                },
                label = {Text("Title")},
                isError = !isTitleValid,
            )
            Button(onClick = {
                if(title.isNotBlank() && isTitleValid){
                    navController.popBackStack()
                }
            }){
                Text("Create Player")
            }
        }
    }
}
