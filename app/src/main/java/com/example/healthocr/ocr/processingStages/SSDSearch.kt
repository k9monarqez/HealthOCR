package com.example.healthocr.ocr.processingStages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.healthocr.ocr.DeviceImageProcessing
import com.squareup.moshi.JsonClass
import org.opencv.core.Mat
import org.opencv.core.Size

@JsonClass(generateAdapter = true)
data class SSDSearchParams(
    val kernelSize: Double = 7.0,
    val blockSize: Int = 51,
    val C: Double = 5.0,
    val digitsSizeRange: Pair<Double, Double> = Pair(0.0, 0.1)
): StageParams

class SSDSearch: ProcessingStage<SSDSearchParams> {
    private var _params: MutableState<SSDSearchParams>
    override val params: SSDSearchParams
        get() = _params.value

    constructor(){
        _params = mutableStateOf(SSDSearchParams())
    }

    constructor(params: SSDSearchParams){
        _params = mutableStateOf(params)
    }

    override val name: StageNames = StageNames.SSDSearch
    override val description: String = "Измените параметры, пока на изображении не останутся только числа"
    override var mat = Mat()
    override fun process(): Mat {
        return DeviceImageProcessing.getSevenSegmentDigitsMat(
            mat = mat,
            digitsSizeRange = params.digitsSizeRange.first..params.digitsSizeRange.second
        )
    }
    override fun getParamsAsControllers(): List<ParamController> {
        return listOf(
            ParamController.RangeSlider(
                label = "Размер числа",
                currentMin = params.digitsSizeRange.first,
                currentMax = params.digitsSizeRange.second,
                range = 0.0..0.1,
                onRangeChanged = { setDigitsSizeRange(Pair(it.start, it.endInclusive)) },
            ),
        )
    }

    fun setDigitsSizeRange(digitsSizeRange:  Pair<Double, Double>){
        _params.value = _params.value.copy(digitsSizeRange = digitsSizeRange)
    }
}