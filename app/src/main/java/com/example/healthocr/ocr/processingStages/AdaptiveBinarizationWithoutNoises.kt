package com.example.healthocr.ocr.processingStages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.healthocr.ocr.DeviceImageProcessing
import com.squareup.moshi.JsonClass
import org.opencv.core.Mat
import org.opencv.core.Size

@JsonClass(generateAdapter = true)
data class AdaptiveBinarizationWithoutNoisesParams(
    val kernelSize: Double = 7.0,
    val blockSize: Int = 51,
    val C: Double = 5.0,
): StageParams

class AdaptiveBinarizationWithoutNoises: ProcessingStage<AdaptiveBinarizationWithoutNoisesParams> {
    private var _params: MutableState<AdaptiveBinarizationWithoutNoisesParams>
    override val params: AdaptiveBinarizationWithoutNoisesParams
        get() = _params.value

    constructor(){
        _params = mutableStateOf(AdaptiveBinarizationWithoutNoisesParams())
    }

    constructor(params: AdaptiveBinarizationWithoutNoisesParams) {
        _params = mutableStateOf(params)
    }

    override val name: StageNames = StageNames.DigitsErosion
    override val description: String = "Увеличьте толщину цифр, двигая ползунок, пока числа корректно не определятся"
    override var mat = Mat()
    override fun process(): Mat {
        return DeviceImageProcessing.adaptiveBinarizationWithoutNoises(
            mat,
            Size(params.kernelSize, params.kernelSize),
            params.blockSize,
            params.C
        )
    }
    override fun getParamsAsControllers(): List<ParamController> {
        return listOf(
            ParamController.Slider(
                label = "Размер ядра",
                currentValue = params.kernelSize,
                range = 1.0..15.0,
                onValueChanged = { setKernelSize(it) }
            ),
            ParamController.Slider(
                label = "Block size",
                currentValue = params.blockSize.toDouble(),
                range = 11.0..100.00,
                onValueChanged = { setBlockSize(it) }
            ),
            ParamController.Slider(
                label = "C",
                currentValue = params.C,
                range = 0.1..10.0,
                onValueChanged = { setC(it) }
            )
        )
    }

    fun setKernelSize(kernelSize: Double){
        _params.value = _params.value.copy(kernelSize = kernelSize)
    }

    fun setBlockSize(blockSize: Double){
        if(blockSize.toInt() % 2 != 0) _params.value = _params.value.copy(blockSize = blockSize.toInt())
    }

    fun setC(C: Double){
        _params.value = _params.value.copy(C = C)
    }
}