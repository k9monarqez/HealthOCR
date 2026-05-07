package com.example.swagaapp.ocr.devices

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import com.example.swagaapp.ocr.processingStages.ProcessingStage
import com.example.swagaapp.storage.Metrics
import org.opencv.core.Mat
import java.time.LocalDateTime

interface Device {
    val type: DevicesNames
    val pipeline: List<ProcessingStage<*>>
    fun process(sourceMat: Mat, bitmap: MutableState<Bitmap>, dataPath: String): Boolean
    fun getMappedData(): Map<Metrics, String?>
    fun getSessionTime(): LocalDateTime

    companion object {
        private val registeredDevices: Map<String, List<Metrics>> = mapOf(
            DevicesNames.Tonometer.name to Tonometer.metrics
        )
    }
}