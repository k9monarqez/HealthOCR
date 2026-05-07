package com.example.swagaapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
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

    @Insert
    fun addSession(session: SessionInfo): Long

    @Insert
    fun addNumericMetrics(metrics: List<NumericMetric>)

    @Insert
    fun addStringMetrics(metrics: List<StringMetric>)

    @Transaction
    @RawQuery
    suspend fun getAllSessionsWithMetrics(query: SupportSQLiteQuery): List<SessionWithMetrics>

    @Query(
        """
            SELECT *
            FROM devices
            WHERE type IN (:ids)
        """
    )
    fun getDevicesByIndexes(ids: List<Long>): List<DBDevice>
}