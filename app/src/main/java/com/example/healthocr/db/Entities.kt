package com.example.healthocr.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "metrics",
    indices = [Index("id")],
    foreignKeys = [
        ForeignKey(
            entity = SessionInfo::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Metric(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: String,
    val value: String,
    @ColumnInfo(name = "session_id") val sessionID: Long?,
    val created: Long,
    val updated: Long?,
    val isDeleted: Long
)

@Entity(
    tableName = "devices",
    indices = [Index("id")]
)
data class DBDevice(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: String,
    val name: String,
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
    val created: Long,
    val updated: Long?
)

data class SessionWithMetrics(
    @Embedded
    val sessionInfo: SessionInfo,

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id"
    )
    val metrics: List<Metric>
)