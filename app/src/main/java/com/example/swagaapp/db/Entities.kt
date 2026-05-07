package com.example.swagaapp.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "numeric_metrics",
    indices = [Index("id")],
    foreignKeys = [
        ForeignKey(
            entity = SessionInfo::class,
            parentColumns = ["id"],
            childColumns = ["session_id"]
        )
    ]
)
data class NumericMetric(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: String,
    val value: Long,
    @ColumnInfo(name = "session_id") val sessionID: Long
)

@Entity(
    tableName = "string_metrics",
    indices = [Index("id")],
    foreignKeys = [
        ForeignKey(
            entity = SessionInfo::class,
            parentColumns = ["id"],
            childColumns = ["session_id"]
        )
    ]
)
data class StringMetric(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: String,
    val value: String,
    @ColumnInfo(name = "session_id") val sessionID: Long
)

@Entity(
    tableName = "devices",
    indices = [Index("id")]
)
data class DBDevice(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: String,
    val model: String,
    val stages: String,
    @ColumnInfo(name = "image_path") val imagePath: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Long
)

@Entity(
    tableName = "sessions",
    indices = [Index("id")],
    foreignKeys = [
        ForeignKey(
            entity = DBDevice::class,
            parentColumns = ["id"],
            childColumns = ["device_id"]
        )
    ]
)
data class SessionInfo(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "device_id") val deviceID: Long,
    val timestamp: Long
)

data class SessionWithMetrics(
    @Embedded
    val session: SessionInfo,

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id"
    )
    val numericMetrics: List<NumericMetric>,

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id"
    )
    val stringMetrics: List<StringMetric>
)