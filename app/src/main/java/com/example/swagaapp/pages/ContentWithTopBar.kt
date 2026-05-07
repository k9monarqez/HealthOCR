package com.example.swagaapp.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.swagaapp.ui.theme.BarColor

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ContentWithTopBar(name: String, paddingValues: PaddingValues, content: @Composable (() -> Unit)){
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            val topBarHeight = pageHeight * 0.1f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight)
                    .background(BarColor),
                contentAlignment = Alignment.BottomCenter
            ){
                //val displayCutoutPadding = with(LocalDensity.current){ WindowInsets.displayCutout.asPaddingValues().calculateTopPadding().toPx() }
                Box(
                    modifier = Modifier
                        .displayCutoutPadding()
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        name,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            content()
        }
    }
}