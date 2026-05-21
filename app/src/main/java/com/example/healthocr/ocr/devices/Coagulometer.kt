package com.example.healthocr.ocr.devices

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.core.graphics.createBitmap
import com.example.healthocr.ocr.processingStages.ProcessingStage
import com.example.healthocr.ocr.processingStages.DisplaySearch
import com.example.healthocr.ocr.processingStages.StageParams
import com.example.healthocr.ocr.processingStages.toStageClasses
import com.example.healthocr.storage.Metrics
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.time.LocalDateTime

class Coagulometer: Device {
    override var pipeline: List<ProcessingStage<*>>

    constructor(){
        pipeline = listOf(
            DisplaySearch()
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
            Metrics.INTERNATIONAL_NORMALIZED_RATIO
        )
    }

    override val type = DevicesNames.Coagulometer
    private var metricsMap = metrics.zip(List<String?>(metrics.size){ null }).toMap().toMutableMap()
    private lateinit var time: LocalDateTime

    override fun process(sourceMat: Mat, bitmap: MutableState<Bitmap>, dataPath: String): Boolean{
        val tess = TessBaseAPI()
        if (tess.init(dataPath, "eng")) {
            var mat = sourceMat.clone()

            // Process image with prepared pipeline
            for(stage in pipeline){
                stage.mat = mat.clone()
                mat = stage.process()
            }
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.threshold(mat, mat, 0.0, 255.0, Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU)

            // Extract digits from mat with Tesseract
            bitmap.value = createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(mat, bitmap.value)

            val targetWords = listOf(Metrics.INTERNATIONAL_NORMALIZED_RATIO.allowedSymbols, Metrics.INTERNATIONAL_NORMALIZED_RATIO.metricCode)
            tess.setVariable("tessedit_char_whitelist", targetWords.joinToString() + targetWords.joinToString().lowercase())
            tess.setImage(bitmap.value)
            var blocks = mutableListOf<Pair<String, Rect>>()

            val utF8Text = tess.utF8Text

            val iterator = tess.resultIterator
            val level = TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE

            iterator.begin()
            do {
                val word = iterator.getUTF8Text(level)
                val boundRect = iterator.getBoundingRect(level)

                val topLeft = Point(boundRect.left.toDouble(), boundRect.top.toDouble())
                val topRight = Point(boundRect.right.toDouble(), boundRect.top.toDouble())
                val bottomRight = Point(boundRect.right.toDouble(), boundRect.bottom.toDouble())
                val bottomLeft = Point(boundRect.left.toDouble(), boundRect.bottom.toDouble())

                val mop = MatOfPoint(topLeft, topRight, bottomRight, bottomLeft)
                //Imgproc.drawContours(mat, listOf(mop), -1, Scalar(0.0, 255.0, 0.0))
                if(word != null)
                    blocks.add(Pair(word, Imgproc.boundingRect(mop)))
            } while(iterator.next(level))

            val blocksWithMetrics = blocks.filter { block ->
                metrics.map { it.metricCode }.any { block.first.contains(it) }
            }.map { pair ->
                pair.first
            }
            println(blocksWithMetrics)

            val mappedBlocks = blocksWithMetrics.mapNotNull { block ->
                val metricName = Metrics.entries.find { block.contains(it.metricCode) }
                metricName?.let {
                    it to block
                        .replace(it.metricCode, "")
                        .trim()
                        .replace(" ", "")
                }
            }.toMap()

            mappedBlocks.forEach {
                if (it.key.isNumeric && it.value.toDoubleOrNull() != null || !it.key.isNumeric) {
                        metricsMap[it.key] = it.value
                    }
            }

            //Utils.matToBitmap(mat, bitmap.value)

            time = LocalDateTime.now()

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