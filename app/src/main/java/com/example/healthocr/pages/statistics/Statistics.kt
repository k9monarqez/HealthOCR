package com.example.healthocr.pages.statistics

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthocr.AppViewModel
import com.example.healthocr.R
import com.example.healthocr.storage.Metrics

sealed class ChartMetadata(
    val name: String,
    val metricsList: List<Metrics>,
    val imgURI: Int?
) {
    open fun metricsOverview(metrics: Map<Metrics, String?>): String{
        val orderedMetrics = metricsList.map { metrics[it] ?: "?" }
        return orderedMetrics.joinToString(", ")
    }

    object BloodPressure: ChartMetadata(
        name = "Артериальное Давление",
        metricsList = listOf(Metrics.SYSTOLIC_PRESSURE, Metrics.DIASTOLIC_PRESSURE),
        R.drawable.tonometer
    ) {
        override fun metricsOverview(metrics: Map<Metrics, String?>): String {
            return (metrics[Metrics.SYSTOLIC_PRESSURE] ?: "?") + "/" + (metrics[Metrics.DIASTOLIC_PRESSURE] ?: "?") + " ${Metrics.SYSTOLIC_PRESSURE.unit}"
        }
    }
    object Pulse: ChartMetadata(
        name = "Пульс",
        metricsList = listOf(Metrics.PULSE),
        R.drawable.pulse
    ) {
        override fun metricsOverview(metrics: Map<Metrics, String?>): String {
            return (metrics[Metrics.PULSE] ?: "?") + " ${Metrics.PULSE.unit}"
        }
    }
}

@Composable
fun Statistics(
    viewModel: AppViewModel,
    toMetricsPage: (String) -> Unit
){
    val chartsMetadata = listOf(
        ChartMetadata.BloodPressure,
        ChartMetadata.Pulse
    )

    val newestMetrics by viewModel.newestMetrics.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNewestMetrics()
    }

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
                val mappedMetrics = mutableMapOf<Metrics, String?>()
                chart.metricsList.forEach {
                    mappedMetrics[it] = newestMetrics[it]
                }
                ChartPreview(
                    viewModel,
                    chart,
                    mappedMetrics,
                    modifier = Modifier
                        .height(pageHeight * 0.15f)
                        .clickable(
                            onClick = { toMetricsPage(chart.metricsList.joinToString(",") { it.metricCode }) }
                        )
                )
            }
        }
    }
}

@Composable
fun ChartPreview(viewModel: AppViewModel, chartMetadata: ChartMetadata, metrics: Map<Metrics, String?>, modifier: Modifier = Modifier){
    Box(
        modifier = modifier
            .border(3.dp, Color(0xFFF44336), RoundedCornerShape(10.dp))
            .padding(10.dp),
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
                    painter = painterResource(chartMetadata.imgURI ?: R.drawable.ic_launcher_background),
                    contentDescription = chartMetadata.name,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .scale(2f)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ){
                Text(chartMetadata.metricsOverview(metrics), fontSize = 20.sp)
                Text(chartMetadata.name, fontWeight = FontWeight.Bold)
            }
        }
    }
}