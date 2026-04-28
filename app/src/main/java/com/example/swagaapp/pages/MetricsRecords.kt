package com.example.swagaapp.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.swagaapp.AppViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MetricsRecords(
    viewModel: AppViewModel
){
    val descendingSort by remember { mutableStateOf(false) }
    val sessions by viewModel.sessions.collectAsState()

    LaunchedEffect(sessions) {
        viewModel.loadAllSessionsWithMetrics(descendingSort)
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ){
            sessions.forEach { session ->
                Column(
                    modifier = Modifier
                        .border(1.dp, Color.Black)
                        .padding(5.dp)
                ){
                    session.metrics.forEach { metric ->
                        Text("${metric.key.metricCode}: ${metric.value}")
                    }
                }
            }
        }


    }
}