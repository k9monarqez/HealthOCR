package com.example.healthocr.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.healthocr.AppViewModel
import com.example.healthocr.ui.theme.BarColor

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Statistics(
    viewModel: AppViewModel,
    paddingValues: PaddingValues
){
    val descendingSort by remember { mutableStateOf(false) }
    val sessions by viewModel.sessions.collectAsState()
    val selectedType by remember {mutableStateOf("")}

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
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
                        "Статистика",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ){

            }
        }
    }
//    LaunchedEffect(sessions) {
//        viewModel.loadAllSessionsWithMetrics(descendingSort)
//    }
//
//    BoxWithConstraints(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Column(
//            modifier = Modifier
//                .verticalScroll(rememberScrollState())
//        ){
//            sessions.forEach { session ->
//                Column(
//                    modifier = Modifier
//                        .border(1.dp, Color.Black)
//                        .padding(5.dp)
//                ){
//                    session.metrics.forEach { metric ->
//                        Text("${metric.key.metricCode}: ${metric.value}")
//                    }
//                }
//            }
//        }
//
//
//    }
}