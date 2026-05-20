package com.example.healthocr.storage.repositories

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.healthocr.db.AppDAO
import com.example.healthocr.db.Metric
import com.example.healthocr.pages.statistics.ChartPeriod
import com.example.healthocr.storage.Metrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.collections.forEach

class MetricsRepository(private val context: Context, private val appDAO: AppDAO){
    suspend fun getMetricsByChartPeriod(types: List<Metrics>, current: LocalDateTime, chartPeriod: ChartPeriod): List<Metric>{
        return withContext(Dispatchers.IO){
            val startDate = getStartOfDate(current, chartPeriod)

            val endDate = getEndOfDate(current, chartPeriod)

            val metrics = appDAO.getMetrics(startDate, endDate, types.map { it.metricCode })
            return@withContext metrics
        }
    }

    suspend fun getMetrics(types: List<Metrics>, start: Long, end: Long): List<Metric>{
        return withContext(Dispatchers.IO){
            appDAO.getMetrics(start, end, types.map { it.metricCode })
        }
    }

    suspend fun getNewestMetricsOfEveryType(): Map<Metrics, String>{
        return withContext(Dispatchers.IO){
            val metricsCodes = Metrics.entries.map { it.metricCode }

            appDAO.getNewestMetricsOfTypes(metricsCodes).associate {
                Metrics.getTypeByMetricCode(it.type) to it.value
            }
        }
    }

    suspend fun addMetrics(mappedMetrics: Map<Metrics, String>, sessionID: Long?) {
        withContext(Dispatchers.IO) {
            val metrics = mutableListOf<Metric>()

            mappedMetrics.forEach { metric ->
                val metricType = metric.key
                val metricValue = metric.value
                metrics.add(
                    Metric(
                        id = 0,
                        type = metricType.metricCode,
                        value = metricValue,
                        sessionID = sessionID,
                        created = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                        updated = null,
                        isDeleted = 0L
                    )
                )
            }

            appDAO.addMetrics(metrics)
        }
    }

    suspend fun addMetric(mappedMetric: Map.Entry<Metrics, String>, sessionID: Long?) {
        withContext(Dispatchers.IO) {
            val metricType = mappedMetric.key
            val metricValue = mappedMetric.value

            appDAO.addMetric(
                Metric(
                    id = 0,
                    type = metricType.metricCode,
                    value = metricValue,
                    sessionID = sessionID,
                    created = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    updated = null,
                    isDeleted = 0L
                )
            )
        }
    }
}

fun getStartOfDate(current: LocalDateTime = LocalDateTime.now(), chartPeriod: ChartPeriod): Long{
    val date = current.toLocalDate()
    return when(chartPeriod){
        ChartPeriod.DAY -> {
            date
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() / 1000
        }
        ChartPeriod.WEEK -> {
            date
                .with(DayOfWeek.MONDAY)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() / 1000
        }
        ChartPeriod.MONTH -> {
            date
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() / 1000
        }
    }
}

fun getEndOfDate(current: LocalDateTime = LocalDateTime.now(), chartPeriod: ChartPeriod): Long {
    val date = current.toLocalDate()
    return when(chartPeriod){
        ChartPeriod.DAY -> {
            date
                .atTime(LocalTime.MAX)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() / 1000
        }
        ChartPeriod.WEEK -> {
            date
                .with(DayOfWeek.SUNDAY)
                .atTime(LocalTime.MAX)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() / 1000
        }
        ChartPeriod.MONTH -> {
            date
                .withDayOfMonth(date.lengthOfMonth())
                .atTime(LocalTime.MAX)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() / 1000
        }
    }
}