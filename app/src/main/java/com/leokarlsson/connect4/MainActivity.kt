package com.leokarlsson.connect4

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.leokarlsson.connect4.ui.theme.Connect4Theme
import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import com.leokarlsson.connect4.lobbyView.CreatePlayerScreen
import com.leokarlsson.connect4.lobbyView.LobbyScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            Connect4Theme{
                Surface(
                    modifier = Modifier.fillMaxSize(), color  = MaterialTheme.colorScheme.background
                ){
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "createPlayer"){
                        composable("createPlayer"){
                            CreatePlayerScreen(navController = navController)
                        }
                        composable("lobby"){
                            LobbyScreen(navController = navController)
                        }
                        composable("menu"){

                        }
                        composable("search"){

                        }
                    }
                }
            }
        }
    }
}


