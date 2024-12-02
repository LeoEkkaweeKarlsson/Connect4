package com.leokarlsson.connect4

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.leokarlsson.connect4.ui.theme.Connect4Theme
import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.leokarlsson.connect4.lobbyView.CreatePlayerScreen
import com.leokarlsson.connect4.lobbyView.LobbyScreen
import com.leokarlsson.connect4.lobbyView.AccountScreen
import com.leokarlsson.connect4.lobbyView.DeleteAccount
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.leokarlsson.connect4.lobbyView.SearchBar
import com.leokarlsson.connect4.gameEngine.Connect4LogicLocal
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.leokarlsson.connect4.gameEngine.GameRequestBox
import com.leokarlsson.connect4.gameEngine.OnlineGameInit
import com.leokarlsson.connect4.gameEngine.OnlineGame


class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

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
                        composable("account"){
                            AccountScreen(navController = navController)
                        }
                        composable("DeleteAccount"){
                            DeleteAccount(navController = navController)
                        }
                        composable("search"){
                            SearchBar(navController = navController)
                        }
                        composable("localGame/{uniqueID}/{gameTag}"){
                            Connect4LogicLocal(navController = navController
                                , uniqueID = it.arguments?.getString("uniqueID")?:""
                                , gameTag = it.arguments?.getString("gameTag")?:""
                                , uniqueGameID = ""
                            )
                        }
                        composable("GameRequestBox"){
                            GameRequestBox(navController = navController)
                        }
                        composable("onlineGameLoading"){
                            OnlineGameInit(navController = navController)
                        }
                        composable("onlineGame"){
                            OnlineGame(navController = navController)
                        }
                    }
                }
            }
        }
    }
}


