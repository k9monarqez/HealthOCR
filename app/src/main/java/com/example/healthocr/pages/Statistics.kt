package com.example.healthocr.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthocr.AppViewModel
import com.example.healthocr.R
import com.example.healthocr.storage.Metrics
import com.example.healthocr.ui.theme.BarColor

sealed class ChartMetadata(
    val name: String,
    val metricsList: List<Metrics>,
    val imgURI: String?
) {
    open fun metricsOverview(metrics: Map<Metrics, String>): String{
        val orderedMetrics = metricsList.mapNotNull { metrics[it] }
        return orderedMetrics.joinToString(", ")
    }

    object BloodPressure: ChartMetadata(
        name = "Давление",
        metricsList = listOf(Metrics.SYSTOLIC_PRESSURE, Metrics.DIASTOLIC_PRESSURE),
        null
    ) {
        override fun metricsOverview(metrics: Map<Metrics, String>): String {
            return (metrics[Metrics.SYSTOLIC_PRESSURE] ?: "?") + "/" + (metrics[Metrics.DIASTOLIC_PRESSURE] ?: "?") + " mmhg"
        }
    }
    object Pulse: ChartMetadata(
        name = "Пульс",
        metricsList = listOf(Metrics.PULSE),
        null
    ) {
        override fun metricsOverview(metrics: Map<Metrics, String>): String {
            return (metrics[Metrics.PULSE] ?: "?") + "bpm"
        }
    }
}

@Composable
fun Statistics(
    viewModel: AppViewModel,
    paddingValues: PaddingValues
){
    val chartsMetadata = listOf(
        ChartMetadata.BloodPressure,
        ChartMetadata.Pulse
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){
            chartsMetadata.forEach { chart ->
                ChartPreview(viewModel, chart, Modifier.height(pageHeight * 0.15f))
            }
        }
    }
}

@Composable
fun ChartPreview(viewModel: AppViewModel, chartMetadata: ChartMetadata, metrics: Map<Metrics, String>, modifier: Modifier = Modifier){
    Box(
        modifier = modifier
            .border(3.dp, Color(0xFFF44336), RoundedCornerShape(10.dp)),
    ){
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ){
            Box(
                modifier = Modifier
                    .padding(10.dp)
            ){
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = chartMetadata.name,
                    tint = Color.Unspecified
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
            ){
                Text(chartMetadata.metricsOverview(metrics))
            }
        }
    }
}