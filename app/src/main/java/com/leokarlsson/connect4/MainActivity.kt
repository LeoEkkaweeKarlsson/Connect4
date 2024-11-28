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
import com.leokarlsson.connect4.lobbyView.AccountStatus
import com.leokarlsson.connect4.lobbyView.SearchBar
import com.leokarlsson.connect4.gameEngine.Connect4LogicLocal
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.leokarlsson.connect4.gameEngine.GameRequestListener
import com.leokarlsson.connect4.gameEngine.OnlineGameInit


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
                        composable("lobby/{uniqueID}/{gameTag}"){
                            LobbyScreen(navController = navController
                                , uniqueID = it.arguments?.getString("uniqueID")?:""
                                , gameTag = it.arguments?.getString("gameTag")?:""
                            )
                        }
                        composable("account/{uniqueID}/{gameTag}"){ backStackEntry ->
                            val uniqueID = backStackEntry.arguments?.getString("uniqueID")?:""
                            val account = AccountStatus(wins = 0, loss = 0, draws = 0, gamesPlayed = 0)
                            AccountScreen(navController = navController
                                , uniqueID = uniqueID
                                , account = account
                                , gameTag = backStackEntry.arguments?.getString("gameTag")?:""
                            )
                        }
                        composable("DeleteAccount/{uniqueID}"){
                            val uniqueID = it.arguments?.getString("uniqueID")?:""
                            DeleteAccount(navController = navController, uniqueID = uniqueID)
                        }
                        composable("search/{gameTag}"){
                            SearchBar(navController = navController, gameTag = it.arguments?.getString("gameTag")?:"")
                        }
                        composable("localGame/{uniqueID}/{gameTag}"){
                            Connect4LogicLocal(navController = navController
                                , uniqueID = it.arguments?.getString("uniqueID")?:""
                                , gameTag = it.arguments?.getString("gameTag")?:""
                                , uniqueGameID = ""
                            )
                        }
                        composable("GameRequestBox/{gameTag}"){
                            GameRequestListener(currentUsername = it.arguments?.getString("gameTag")?:"", navController = navController)
                        }
                        composable("onlineGame/{currentUsername}/{senderUsername}"){
                            OnlineGameInit(player1 = it.arguments?.getString("currentUsername")?:"",
                                player2 = it.arguments?.getString("senderUsername")?:"",
                                navController = navController,
                                gameTag = ""
                            )
                        }
                    }
                }
            }
        }
    }
}


