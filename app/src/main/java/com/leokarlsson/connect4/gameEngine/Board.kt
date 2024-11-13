package com.leokarlsson.connect4.gameEngine

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable

@Composable
fun BoardCreation(board: Array<IntArray>, onCellClick: (Int) -> Unit){
    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ){
        for(row in 0 until 6){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                for(column in 0 until 7){
                    GameCell(
                        color = when(board[row][column]){
                            0 -> Color.Red
                            1 -> Color.Blue
                            else -> Color.LightGray
                        },
                        onClick = {onCellClick(column)}
                    )
                }
            }
        }
    }
}

@Composable
fun GameCell(color: Color = Color.LightGray, onClick: () -> Unit){
    Box(
        modifier = Modifier
            .size(50.dp)
            .padding(4.dp)
            .clickable{ onClick() },
        contentAlignment = Alignment.Center
    ){
        //Content of the cell
        Canvas(modifier = Modifier.size(40.dp)){
            drawCircle(color = color)
        }
    }
}
