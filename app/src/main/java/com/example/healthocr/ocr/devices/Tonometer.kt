package com.example.healthocr.ocr.devices

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.core.graphics.createBitmap
import com.example.healthocr.ocr.processingStages.ProcessingStage
import com.example.healthocr.ocr.processingStages.DigitsErosion
import com.example.healthocr.ocr.processingStages.DisplaySearch
import com.example.healthocr.ocr.processingStages.SSDSearch
import com.example.healthocr.ocr.processingStages.StageParams
import com.example.healthocr.ocr.processingStages.toStageClasses
import com.example.healthocr.storage.Metrics
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import java.time.LocalDateTime

class Tonometer: Device {
    override var pipeline: List<ProcessingStage<*>>

    constructor(){
        pipeline = listOf(
            DisplaySearch(),
            SSDSearch(),
            DigitsErosion()
        )
    }

    constructor(stages: Map<String, StageParams>){
        val mutablePipeline = mutableListOf<ProcessingStage<*>>()
        for(rawStage in stages.values){
            val stageClass = toStageClasses(rawStage)
            mutablePipeline.add(stageClass)
        }
        pipeline = mutablePipeline
    }

    companion object {
        val metrics: List<Metrics> = listOf(
            Metrics.SYSTOLIC_PRESSURE,
            Metrics.DIASTOLIC_PRESSURE,
            Metrics.PULSE
        )
    }

    override val type = DevicesNames.Tonometer
    private var metricsMap = metrics.zip(List<String?>(metrics.size){ null }).toMap().toMutableMap()
    private lateinit var time: LocalDateTime

    override fun process(sourceMat: Mat, bitmap: MutableState<Bitmap>, dataPath: String): Boolean{
        val tess = TessBaseAPI()
        if (tess.init(dataPath, "ssd")) {
            var mat = sourceMat.clone()

            // Process image with prepared pipeline
            for(stage in pipeline){
                stage.mat = mat.clone()
                mat = stage.process()
            }

            // Extract digits from mat with Tesseract
            bitmap.value = createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(mat, bitmap.value)

            val targetWords = listOf("0123456789")
            tess.setVariable("tessedit_char_whitelist", targetWords.joinToString() + targetWords.joinToString().lowercase())
            tess.setImage(bitmap.value)
            var blocks = mutableListOf<Pair<String, Rect>>()

            val utF8Text = tess.utF8Text

            val iterator = tess.resultIterator
            val level = TessBaseAPI.PageIteratorLevel.RIL_WORD

            iterator.begin()
            do {
                val word = iterator.getUTF8Text(level)
                val boundRect = iterator.getBoundingRect(level)

                val topLeft = Point(boundRect.left.toDouble(), boundRect.top.toDouble())
                val topRight = Point(boundRect.right.toDouble(), boundRect.top.toDouble())
                val bottomRight = Point(boundRect.right.toDouble(), boundRect.bottom.toDouble())
                val bottomLeft = Point(boundRect.left.toDouble(), boundRect.bottom.toDouble())

                val mop = MatOfPoint(topLeft, topRight, bottomRight, bottomLeft)

                if(word != null)
                    blocks.add(Pair(word, Imgproc.boundingRect(mop)))
            } while(iterator.next(level))

            val sortedBlocks = blocks.sortedWith(
                compareByDescending<Pair<String, Rect>> { it.second.height * it.second.width }
                    .thenBy { it.second.y }
            ).map{
                block -> block.first
            }.toMutableList<String?>()
            repeat(metrics.size - sortedBlocks.size){
                sortedBlocks.add(null)
            }

            sortedBlocks[0]?.let{ b0 ->
                sortedBlocks[1]?.let{ b1 ->
                    if(b1.toInt() > b0.toInt()) sortedBlocks[0] = sortedBlocks[1].also {sortedBlocks[1] = sortedBlocks[0]}
                }
            }

            metrics.forEachIndexed { i, metric ->
                sortedBlocks[i]?.let { value ->
                    if(metric.isNumeric && value.toIntOrNull() != null || !metric.isNumeric){
                        metricsMap[metric] = value
                    }
                }
            }

            time = LocalDateTime.now()
            println(time)

            tess.recycle()
            return true
        }
        else{
            tess.recycle()
            return false
        }
    }

    override fun getMappedData(): Map<Metrics, String?>{
        return metricsMap
    }

    override fun getSessionTime(): LocalDateTime {
        return time
    }
}