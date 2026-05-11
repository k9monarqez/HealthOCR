package com.example.healthocr.storage.repositories

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.healthocr.db.AppDAO
import com.example.healthocr.db.Metric
import com.example.healthocr.storage.Metrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.forEach

class MetricsRepository(private val context: Context, private val appDAO: AppDAO){
    suspend fun getMetrics(type: String? = null, sortDescending: Boolean = true){
        withContext(Dispatchers.IO){
            val sortingOrder = if(sortDescending) "DESC" else "ASC"

            val query = if(type != null){
                SimpleSQLiteQuery(
                    """
                    SELECT *
                    FROM metrics
                    WHERE type = '$type'
                    ORDER BY created $sortingOrder
                """
                )
            }
            else {
                SimpleSQLiteQuery(
                    """
                        SELECT * 
                        FROM metrics 
                        ORDER BY created $sortingOrder
                """
                )
            }

            val metrics = appDAO.getMetrics(query)
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