package com.example.healthocr.storage.repositories

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.healthocr.db.AppDAO
import com.example.healthocr.db.NumericMetric
import com.example.healthocr.db.SessionInfo
import com.example.healthocr.db.StringMetric
import com.example.healthocr.storage.Metrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset

data class Session(
    val sessionInfo: SessionInfo,
    val metrics: Map<Metrics, String>,
    val metricsID: List<Long>
)

class SessionRepository(private val context: Context, private val appDAO: AppDAO) {
    suspend fun getSession(id: Long): Session{
        return withContext(Dispatchers.IO) {
            val rawSession = appDAO.getSession(id)
            val mappedMetrics = mutableMapOf<Metrics, String>()
            val metricsID: MutableList<Long> = mutableListOf()
            rawSession.numericMetrics.forEach {
                mappedMetrics[Metrics.getTypeByMetricCode(it.type)] = it.value.toString()
                metricsID.add(it.id)
            }
            rawSession.stringMetrics.forEach {
                mappedMetrics[Metrics.getTypeByMetricCode(it.type)] = it.value
                metricsID.add(it.id)
            }

            return@withContext Session(
                sessionInfo = rawSession.session,
                mappedMetrics,
                metricsID
            )
        }
    }
    suspend fun addSession(time: LocalDateTime, deviceID: Long): Long {
        return withContext(Dispatchers.IO) {
            appDAO.addSession(
                SessionInfo(
                    id = 0,
                    created = time.toEpochSecond(ZoneOffset.UTC),
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
                    ORDER BY created $sortingOrder
                """
                )
            }
            else {
                SimpleSQLiteQuery(
                    """
                        SELECT * 
                        FROM sessions 
                        ORDER BY created $sortingOrder
                """
                )
            }

            val rawSessions = appDAO.getAllSessionsWithMetrics(query)
            val sessions = mutableListOf<Session>()

            rawSessions.forEach { dbSession ->
                val mappedMetrics = mutableMapOf<Metrics, String>()
                val metricsID: MutableList<Long> = mutableListOf()
                dbSession.numericMetrics.forEach {
                    mappedMetrics[Metrics.getTypeByMetricCode(it.type)] = it.value.toString()
                    metricsID.add(it.id)
                }
                dbSession.stringMetrics.forEach {
                    mappedMetrics[Metrics.getTypeByMetricCode(it.type)] = it.value
                    metricsID.add(it.id)
                }

                sessions.add(
                    Session(
                        sessionInfo = dbSession.session,
                        metrics = mappedMetrics,
                        metricsID = metricsID
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

    suspend fun deleteSessions(sessions: List<SessionInfo>){
        withContext(Dispatchers.IO){
            appDAO.deleteSessions(sessions)
        }
    }
    
    suspend fun updateSessionWithMetrics(session: Session){
        withContext(Dispatchers.IO){
            val sessionInfo = session.sessionInfo
            val metricsMap = session.metrics
            val metricsID = session.metricsID

            val numericMetrics: MutableList<NumericMetric> = mutableListOf()
            val stringMetrics: MutableList<StringMetric> = mutableListOf()
            
            metricsMap.toList().forEachIndexed { i, pair ->
                val metricType = pair.first
                val value = pair.second
                if(metricType.isNumeric){
                    numericMetrics.add(
                        NumericMetric(
                            id = metricsID[i],
                            type = metricType.metricCode,
                            value = value.toLong(),
                            sessionID = sessionInfo.id
                        )
                    )
                }
                else{
                    stringMetrics.add(
                        StringMetric(
                            id = metricsID[i],
                            type = metricType.metricCode,
                            value = value,
                            sessionID = sessionInfo.id
                        )
                    )
                }
            }

            appDAO.updateSession(sessionInfo)
            appDAO.updateNumericMetrics(numericMetrics)
            appDAO.updateStringMetrics(stringMetrics)
        }
    }
}