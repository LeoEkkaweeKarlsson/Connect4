package com.leokarlsson.connect4.gameEngine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.Button


@Composable
fun Connect4LogicLocal(){
    var board by remember { mutableStateOf(Array(6){IntArray(7){-1} }) }
    var currentPlayer by remember { mutableStateOf(0)}
    var winner by remember { mutableStateOf(-1)}

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        BoardCreation(board = board){column ->
            if(winner == -1){
                makeMove(board, column, currentPlayer){ isWin ->
                    if(isWin){
                        winner = currentPlayer
                    }else{
                        currentPlayer = 1 - currentPlayer
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if(winner != -1){
            GameOverView(winner){
                board = Array(6){IntArray(7){-1} }
                currentPlayer = 0
                winner = -1
            }
        }else{
            Text(text = "Player ${currentPlayer + 1}'s turn", color = Color.Black)
        }
    }
}

fun makeMove(board: Array<IntArray>, column: Int, player: Int, onMoveResult: (Boolean) -> Unit){
    for(row in 5 downTo 0){
        if(board[row][column] == -1){
            board[row][column] = player
            onMoveResult(checkForWin(board,row, column, player))
            return
        }
    }
}

fun checkForWin(board: Array<IntArray>, row: Int, column: Int, player: Int): Boolean{
    fun countConsecutive(dx: Int, dy: Int): Int{
        var count = 0
        var r = row + dx
        var c = column + dy
        while(r in 0..5 && c in 0..6 && board[r][c] == player){
            count++
            r += dx
            c += dy
        }
        return count
    }

    val directions = listOf(
        Pair(1,0), Pair(0,1), Pair(1,1), Pair(1,-1)
    )
    return directions.any{(dx,dy) ->
        1 + countConsecutive(dx, dy) + countConsecutive(-dx, -dy) >= 4
    }
}

@Composable
fun GameOverView(player: Int, onRestart: () -> Unit){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ){
            Text(
                text = "Player ${player + 1} wins!",
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = {
                    onRestart()}){
                Text("Restart Game")
            }
        }
    }
}
