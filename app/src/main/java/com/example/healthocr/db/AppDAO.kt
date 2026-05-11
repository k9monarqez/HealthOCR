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
    fun getSession(id: Long): SessionWithMetrics

    @Delete
    fun deleteSessions(ids: List<SessionInfo>)

    @Insert
    fun addSession(session: SessionInfo): Long

    @Insert
    fun addNumericMetrics(metrics: List<NumericMetric>)

    @Insert
    fun addStringMetrics(metrics: List<StringMetric>)

    @Transaction
    @RawQuery
    suspend fun getAllSessionsWithMetrics(query: SupportSQLiteQuery): List<SessionWithMetrics>

    @Update
    fun updateSession(sessionInfo: SessionInfo)

    @Update
    fun updateNumericMetrics(numericMetrics: List<NumericMetric>)

    @Update
    fun updateStringMetrics(stringMetrics: List<StringMetric>)
}