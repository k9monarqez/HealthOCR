package com.example.swagaapp.ocr.processingStages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.swagaapp.ocr.DeviceImageProcessing
import com.squareup.moshi.JsonClass
import org.opencv.core.Mat
import org.opencv.core.Size

@JsonClass(generateAdapter = true)
data class DigitsErosionParams(
    val kernelSize: Double = 7.0
): StageParams

class DigitsErosion: ProcessingStage<DigitsErosionParams> {
    private var _params: MutableState<DigitsErosionParams>
    override val params: DigitsErosionParams
        get() = _params.value

    constructor(){
        _params = mutableStateOf(DigitsErosionParams())
    }

    constructor(params: DigitsErosionParams) {
        _params = mutableStateOf(params)
    }

    override val name: StageNames = StageNames.DigitsErosion
    override val description: String = "Увеличьте толщину цифр, двигая ползунок, пока числа корректно не определятся"
    override var mat = Mat()
    override fun process(): Mat {
        return DeviceImageProcessing.erodeDigits(
            mat = mat,
            kernelSize = Size(params.kernelSize, params.kernelSize)
        )
    }
    override fun getParamsAsControllers(): List<ParamController> {
        return listOf(
            ParamController.Slider(
                label = "Размер ядра",
                currentValue = params.kernelSize,
                range = 1.0..15.0,
                onValueChanged = { setKernelSize(it) }
            )
        )
    }

    fun setKernelSize(kernelSize: Double){
        _params.value = _params.value.copy(kernelSize = kernelSize)
    }
}