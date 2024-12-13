package com.leokarlsson.connect4.lobbyView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(navController: NavController ){
    val firestore = FirebaseFirestore.getInstance()
    val userInfo = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<UserInfo>("userInfo")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Account")},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }){
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {navController.navigate("DeleteAccount")}){
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Account",
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
            )
        }
    ){padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column{
                Text(text = "GameTag: ${userInfo?.gameTag}", modifier = Modifier.padding(8.dp))
                Text(text = "PlayerID: ${userInfo?.uniqueID}", modifier = Modifier.padding(8.dp))
            }
                Button(onClick = {
                    firestore.collection("User")
                        .document(userInfo?.uniqueID?: "")
                        .update("status", "Offline")

                    navController.navigate("createPlayer")},
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ){
                    Text(text = "Log out")
                }
            }
        }
    }
}

@Composable
fun DeleteAccount(navController: NavController){
    val userInfo = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<UserInfo>("userInfo")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ){
            Text(
                text = "Are you sure you want to delete your account?",
                modifier = Modifier.padding(16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Button(onClick = {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("User").document(userInfo?.uniqueID?: "").delete()
                    navController.navigate("createPlayer"){
                        popUpTo("createPlayer"){ inclusive = true}
                    }
                }){
                    Text(text = "Yes")
                }
                Button(onClick = {
                    navController.popBackStack()
                }){
                    Text(text = "No")
                }
            }
        }
    }
}