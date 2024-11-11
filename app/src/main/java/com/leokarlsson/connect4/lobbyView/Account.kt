package com.leokarlsson.connect4.lobbyView


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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


class AccountStatus(
    val wins:Int,
    val loss:Int,
    val draws:Int,
    val gamesPlayed:Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(navController: NavController, uniqueID:String?, account: AccountStatus){

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Account")},
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ){
            Text(text = "Wins: ${account.wins}", modifier = Modifier.padding(8.dp))
            Text(text = "Losses: ${account.loss}", modifier = Modifier.padding(8.dp))
            Text(text = "Draws: ${account.draws}", modifier = Modifier.padding(8.dp))
            Text(text = "Games Played: ${account.gamesPlayed}", modifier = Modifier.padding(8.dp))
            Text(text = "PlayerID: ${uniqueID ?: "Not Available"}", modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun DeleteAccount(navController: NavController){
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