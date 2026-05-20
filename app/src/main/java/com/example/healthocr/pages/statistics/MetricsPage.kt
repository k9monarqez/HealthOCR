package com.example.healthocr.pages.statistics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthocr.AppViewModel
import com.example.healthocr.R
import com.example.healthocr.db.Metric
import com.example.healthocr.storage.Metrics
import com.example.healthocr.storage.repositories.getStartOfDate
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.IndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.LineProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import java.time.*

@Composable
fun MetricsPage(viewModel: AppViewModel, metrics: List<Metrics>){
    val chartPeriod by viewModel.chartPeriod.collectAsState()
    val selectedDate = remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        viewModel.setChartPeriod(ChartPeriod.DAY)
    }
    LaunchedEffect(chartPeriod, selectedDate.value) {
        viewModel.loadMetricsByChartPeriod(metrics, selectedDate.value)
    }
    LaunchedEffect(chartPeriod) {
        selectedDate.value = LocalDateTime.ofEpochSecond(getStartOfDate(chartPeriod = chartPeriod), 0, ZoneOffset.UTC)
        when(chartPeriod){
            ChartPeriod.DAY -> { selectedDate.value = selectedDate.value.plusDays(1) }
            ChartPeriod.WEEK -> { selectedDate.value = selectedDate.value.plusWeeks(1) }
            ChartPeriod.MONTH -> { selectedDate.value = selectedDate.value.plusMonths(1) }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            MetricsChart(
                viewModel,
                modifier = Modifier
                    .height(pageHeight * 0.5f)
                    .fillMaxWidth(),
                metrics
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(
                    { selectedDate.value = chartPeriod.onBack(selectedDate.value) }
                ){
                    Icon(painterResource(R.drawable.back), null)
                }
                Text(
                    chartPeriod.text(selectedDate.value)
                )
                IconButton(
                    { selectedDate.value = chartPeriod.onNext(selectedDate.value) }
                ){
                    Icon(painterResource(R.drawable.next), null)
                }
            }

            Row() {
                Button({ viewModel.setChartPeriod(ChartPeriod.DAY) }) {
                    Text("День")
                }
                Button({ viewModel.setChartPeriod(ChartPeriod.WEEK) }) {
                    Text("Неделя")
                }
                Button({ viewModel.setChartPeriod(ChartPeriod.MONTH) }) {
                    Text("Месяц")
                }
            }
        }
    }
}

@Composable
fun MetricsChart(viewModel: AppViewModel, modifier: Modifier = Modifier, metrics: List<Metrics>) {
    val metricsValues by viewModel.metricsValues.collectAsState()
    val chartPeriod by viewModel.chartPeriod.collectAsState()

    val colors = listOf(
        SolidColor(Color.Red),
        SolidColor(Color.Green),
        SolidColor(Color.Blue)
    )

    val groupedMetrics = remember(metricsValues, chartPeriod) {
        when (chartPeriod) {
            ChartPeriod.DAY -> {
                metricsValues.groupBy { it.type }
                    .map { (type, metrics) -> type to metrics }
            }
            ChartPeriod.WEEK -> {
                metricsValues.groupBy { it.type }
                    .map { (type, metrics) ->
                        val byDay = metrics.groupBy {
                            Instant.ofEpochMilli(it.created)
                                .atZone(ZoneId.systemDefault())
                                .dayOfWeek.value
                        }
                        type to (1..7).map { day ->
                            val dayMetrics = byDay[day] ?: emptyList()
                            val avgValue = if (dayMetrics.isNotEmpty()) {
                                dayMetrics.map { it.value.toDouble() }.average()
                            } else 0.0

                            Metric(
                                id = 0,
                                type = type,
                                value = avgValue.toInt().toString(),
                                sessionID = null,
                                created = 0,
                                updated = null,
                                isDeleted = 0
                            )
                        }
                    }
            }
            ChartPeriod.MONTH -> {
                metricsValues.groupBy { it.type }
                    .map { (type, metrics) ->
                        val byDay = metrics.groupBy {
                            Instant.ofEpochMilli(it.created)
                                .atZone(ZoneId.systemDefault())
                                .dayOfMonth
                        }
                        val daysInMonth = LocalDate.now().lengthOfMonth()
                        type to (1..daysInMonth).map { day ->
                            val dayMetrics = byDay[day] ?: emptyList()
                            val avgValue = if (dayMetrics.isNotEmpty()) {
                                dayMetrics.map { it.value.toDouble() }.average()
                            } else 0.0

                            Metric(
                                id = 0,
                                type = type,
                                value = avgValue.toInt().toString(),
                                sessionID = null,
                                created = 0,
                                updated = null,
                                isDeleted = 0
                            )
                        }
                    }
            }
        }
    }

    val metricLines = if(groupedMetrics.isNotEmpty()) groupedMetrics.mapIndexed { i, metric ->
        Line(
            label = "${metric.first}, ${Metrics.getTypeByMetricCode(metric.first).unit}",
            values = metric.second.map {
                it.value.toDouble()
            },
            colors[i],
            dotProperties = DotProperties(
                enabled = true,
                color = SolidColor(Color.White),
                strokeWidth = 1.dp,
                radius = 1.dp,
                strokeColor = colors[i]
            ),
        )
    }
    else metrics.mapIndexed { i, metric ->
        Line(
            label = "${metric.metricCode}, ${metric.unit}",
            values = listOf(),
            colors[i]
        )
    }

    val type = metricsValues.map { Metrics.getTypeByMetricCode(it.type) }.firstOrNull()
    val labels = when(chartPeriod){
        ChartPeriod.DAY -> {
            listOf("00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00")
        }
        ChartPeriod.WEEK -> {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        }
        ChartPeriod.MONTH -> {
            listOf("1", "5", "10", "15", "20", "25", "30")
        }
    }

    LineChart(
        data = metricLines,
        modifier = modifier
            .padding(16.dp),
        animationMode = AnimationMode.None,
        curvedEdges = false,
        labelProperties = LabelProperties(
            enabled = true,
            labels = labels,
            padding = 16.dp,
            textStyle = TextStyle(
                fontSize = 8.sp
            )
        ),
        minValue = type?.range?.start ?: 0.0,
        maxValue = type?.range?.endInclusive ?: 300.0,
        popupProperties = PopupProperties(
            mode = PopupProperties.Mode.PointMode(),
            containerColor = Color.White,
            contentBuilder = { popup ->
                val metricsList = groupedMetrics[popup.dataIndex]
                val metric = metricsList.second[popup.valueIndex]
                val time = LocalDateTime.ofEpochSecond(metric.created, 0, ZoneOffset.UTC)
                when(chartPeriod){
                    ChartPeriod.DAY -> {
                        """
                            ${metric.value}
                            ${("0" + time.hour).takeLast(2)}:${("0" + time.minute).takeLast(2)}
                        """.trimIndent()
                    }
                    ChartPeriod.WEEK -> {
                        """
                            ${metric.value}
                            ${("0" + time.dayOfMonth).takeLast(2)}:${("0" + time.monthValue).takeLast(2)}
                        """.trimIndent()
                    }
                    ChartPeriod.MONTH -> {
                        """
                            ${metric.value}
                            ${("0" + time.dayOfMonth).takeLast(2)}:${("0" + time.monthValue).takeLast(2)}
                        """.trimIndent()
                    }
                }
            }
        ),
        indicatorProperties = HorizontalIndicatorProperties(
            enabled = true,
            textStyle = MaterialTheme.typography.labelSmall,
            padding = 8.dp,
        )
    )
}

enum class ChartPeriod(
    val onBack: (LocalDateTime) -> LocalDateTime,
    val onNext: (LocalDateTime) -> LocalDateTime,
    val text: (LocalDateTime) -> String
) {
    DAY(
        onBack = { date ->
            date.minusDays(1)
        },
        onNext = { date ->
            date.plusDays(1)
        },
        text = { date ->
            "${("0" + date.dayOfMonth.toString()).takeLast(2)}.${("0" + date.monthValue).takeLast(2)}"
        }
    ),
    WEEK(
        onBack = { date ->
            date.minusWeeks(1)
        },
        onNext = { date ->
            date.plusWeeks(1)
        },
        text = { date ->
            val firstDateString = "${("0" + date.dayOfMonth.toString()).takeLast(2)}.${("0" + date.monthValue).takeLast(2)}"
            val secondDate = date.minusWeeks(1)
            val secondDateString = "${("0" + secondDate.dayOfMonth.toString()).takeLast(2)}.${("0" + secondDate.monthValue).takeLast(2)}"
            "$secondDateString - $firstDateString"
        }
    ),
    MONTH(
        onBack = { date ->
            date.minusMonths(1)
        },
        onNext = { date ->
            date.plusMonths(1)
        },
        text = { date ->
            when(date.monthValue){
                1 -> "Январь"
                2 -> "Февраль"
                3 -> "Март"
                4 -> "Апрель"
                5 -> "Май"
                6 -> "Июнь"
                7 -> "Июль"
                8 -> "Август"
                9 -> "Сентябрь"
                10 -> "Октябрь"
                11 -> "Ноябрь"
                12 -> "Декабрь"
                else -> "Unknown"
            } + " " + date.year
        }
    )
}