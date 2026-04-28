package com.example.swagaapp.storage.repositories

import android.content.Context
import com.example.swagaapp.db.AppDAO
import com.example.swagaapp.db.NumericMetric
import com.example.swagaapp.db.StringMetric
import com.example.swagaapp.storage.Metrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetricsRepository(private val context: Context, private val appDAO: AppDAO) {
    suspend fun addMetrics(metrics: Map<Metrics, String>, sessionID: Long) {
        withContext(Dispatchers.IO) {
            val numericMetrics = mutableListOf<NumericMetric>()
            val stringMetrics = mutableListOf<StringMetric>()

            metrics.forEach { metric ->
                val metricInfo = metric.key
                val metricValue = metric.value
                if (metricInfo.isNumeric) {
                    numericMetrics.add(
                        NumericMetric(
                            id = 0,
                            type = metricInfo.metricCode,
                            value = metricValue.toLong(),
                            sessionID = sessionID,
                        )
                    )
                }
                else {
                    stringMetrics.add(
                        StringMetric(
                            id = 0,
                            type = metricInfo.metricCode,
                            value = metricValue,
                            sessionID = sessionID,
                        )
                    )
                }
            }

            appDAO.addNumericMetrics(numericMetrics)
            appDAO.addStringMetrics(stringMetrics)
        }
    }
}