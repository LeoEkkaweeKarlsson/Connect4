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
            .fillMaxWidth()
    ){
        board.forEachIndexed{ rowIndex, row ->
            BoardRow(row, onCellClick)
        }
    }
}

@Composable
fun BoardRow(row: IntArray, onCellClick: (Int) -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center
    ){
        row.forEachIndexed{columnIndex, cellValue ->
            GameCell(
                color = when(cellValue){
                    0 -> Color.Red
                    1 -> Color.Blue
                    else -> Color.LightGray
                },
                onClick = {
                    if (cellValue == -1) onCellClick(columnIndex)},
                enabled = cellValue == -1
            )
        }
    }
}

@Composable
fun GameCell(color: Color, onClick: () -> Unit, enabled: Boolean = true){
    Box(
        modifier = Modifier
            .size(50.dp)
            .padding(4.dp)
            .clickable(enabled = enabled){onClick()},
        contentAlignment = Alignment.Center
    ){
        //Content of the cell
        Canvas(modifier = Modifier.size(40.dp)){
            drawCircle(color = color)
        }
    }
}
