package com.example.healthocr.storage.repositories

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.healthocr.db.AppDAO
import com.example.healthocr.db.SessionInfo
import com.example.healthocr.db.SessionWithMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset

class SessionRepository(private val context: Context, private val appDAO: AppDAO) {
    suspend fun getSession(id: Long): SessionWithMetrics{
        return withContext(Dispatchers.IO) {
            appDAO.getSessionWithMetrics(id)
        }
    }

    suspend fun addSession(time: LocalDateTime, deviceID: Long): Long {
        return withContext(Dispatchers.IO) {
            appDAO.addSession(
                SessionInfo(
                    id = 0,
                    created = time.toEpochSecond(ZoneOffset.UTC),
                    deviceID = deviceID,
                    updated = null
                )
            )
        }
    }

    suspend fun getAllSessions(sortDescending: Boolean = true, limit: Int? = null): List<SessionWithMetrics>{
        return withContext(Dispatchers.IO){
            val sortingOrder = if(sortDescending) "DESC" else "ASC"
            val query = SimpleSQLiteQuery(
                """
                    SELECT * 
                    FROM sessions 
                    ORDER BY created $sortingOrder
                    ${limit?.let { "LIMIT $it" } ?: ""}
            """
            )

            appDAO.getAllSessions(query)
        }
    }

    suspend fun deleteSessions(session: SessionInfo){
        withContext(Dispatchers.IO){
            appDAO.deleteSession(session)
        }
    }
    
    suspend fun updateSessionWithMetrics(session: SessionWithMetrics){
        withContext(Dispatchers.IO){
            appDAO.updateSession(session.sessionInfo)
            appDAO.updateMetrics(session.metrics)
        }
    }
}