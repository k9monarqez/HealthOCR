package com.example.swagaapp.storage.repositories

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.swagaapp.db.AppDAO
import com.example.swagaapp.db.NumericMetric
import com.example.swagaapp.db.SessionInfo
import com.example.swagaapp.db.StringMetric
import com.example.swagaapp.storage.Metrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset

data class Session(
    val session: SessionInfo,
    val metrics: Map<Metrics, String>
)

class SessionRepository(private val context: Context, private val appDAO: AppDAO) {
    suspend fun addSession(time: LocalDateTime, deviceID: Long): Long {
        return withContext(Dispatchers.IO) {
            appDAO.addSession(
                SessionInfo(
                    id = 0,
                    timestamp = time.toEpochSecond(ZoneOffset.UTC),
                    deviceID = deviceID
                )
            )
        }
    }

    suspend fun getSessionsWithMetrics(type: String? = null, sortDescending: Boolean = true): List<Session>{
        return withContext(Dispatchers.IO){
            val sortingOrder = if(sortDescending) "DESC" else "ASC"

            val query = if(type != null){
                SimpleSQLiteQuery(
                    """
                    SELECT s.*
                    FROM sessions s 
                    JOIN devices d ON s.device_id = d.id
                    WHERE d.type = '$type'
                    ORDER BY timestamp $sortingOrder
                """
                )
            }
            else {
                SimpleSQLiteQuery(
                    """
                        SELECT * 
                        FROM sessions 
                        ORDER BY timestamp $sortingOrder
                """
                )
            }

            val rawSessions = appDAO.getAllSessionsWithMetrics(query)
            val sessions = mutableListOf<Session>()

            rawSessions.forEach { dbSession ->
                val mappedMetrics = mutableMapOf<Metrics, String>()
                dbSession.numericMetrics.forEach {
                    mappedMetrics[Metrics.getTypeByMetricCode(it.type)] = it.value.toString()
                }
                dbSession.stringMetrics.forEach {
                    mappedMetrics[Metrics.getTypeByMetricCode(it.type)] = it.value
                }

                sessions.add(
                    Session(
                        session = dbSession.session,
                        metrics = mappedMetrics
                    )
                )
            }

            return@withContext sessions
        }
    }

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