package com.cet.secure360

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Custom Size Dp",
    widthDp = 2560,
    heightDp = 1600
)
@Composable
fun HomeMainScreen(modifier: Modifier = Modifier) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A021A))
    ) {




    }

}