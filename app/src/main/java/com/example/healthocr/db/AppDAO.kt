package com.example.healthocr.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface AppDAO {
    @Query(
        """
            SELECT *
            FROM devices
        """
    )
    fun getDevices(): List<DBDevice>

    @Insert
    fun addDevice(device: DBDevice): Long

    @Query(
"""
            UPDATE devices
            SET is_deleted = 1
            WHERE id = :deviceID
        """
    )
    fun deleteDevice(deviceID: Long)

    @Query(
        """
            SELECT *
            FROM sessions
            WHERE id = :id
        """
    )
    fun getSessionWithMetrics(id: Long): SessionWithMetrics

    @Insert
    fun addSession(session: SessionInfo): Long

    @Delete
    fun deleteSessions(ids: List<SessionInfo>)

    @Transaction
    @RawQuery
    suspend fun getMetrics(query: SupportSQLiteQuery): List<Metric>

    @Insert
    fun addMetric(metric: Metric)

    @Insert
    fun addMetrics(metrics: List<Metric>)

    @Update
    fun updateMetrics(metrics: List<Metric>)

    @Transaction
    @RawQuery
    suspend fun getAllSessions(query: SupportSQLiteQuery): List<SessionWithMetrics>

    @Update
    fun updateSession(sessionInfo: SessionInfo)
}