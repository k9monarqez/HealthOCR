package com.example.healthocr.ocr.processingStages

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.healthocr.ocr.DeviceImageProcessing
import com.squareup.moshi.JsonClass
import org.opencv.core.Mat
import org.opencv.core.Size

@JsonClass(generateAdapter = true)
data class DisplaySearchParams(
    val kernelSize: Double = 7.0,
    val blockSize: Int = 51,
    val C: Double = 5.0,
    val minDisplaySize: Double = 0.1    // The only parameter that changes
): StageParams

class DisplaySearch: ProcessingStage<DisplaySearchParams> {
    private var _params: MutableState<DisplaySearchParams>
    override val params: DisplaySearchParams
        get() = _params.value

    constructor(){
        _params = mutableStateOf(DisplaySearchParams())
    }

    constructor(params: DisplaySearchParams){
        _params = mutableStateOf(params)
    }

    override val name: StageNames = StageNames.DisplaySearch
    override val description: String = "Выделите цифровой экран устройства, двигая ползунок"
    override var mat = Mat()
    override fun process(): Mat {
        return DeviceImageProcessing.getDisplayArea(
            mat = mat,
            kernelSize = Size(params.kernelSize, params.kernelSize),
            blockSize = params.blockSize,
            C = params.C,
            minDisplaySize = params.minDisplaySize
        )
    }
    override fun getParamsAsControllers(): List<ParamController> {
        return listOf(
            ParamController.Slider(
                label = "Размер дисплея",
                currentValue = params.minDisplaySize,
                range = 0.0..1.0,
                onValueChanged = { setMinDisplaySize(it) }
            )
        )
    }

    fun setKernelSize(kernelSize: Double){
        _params.value = _params.value.copy(kernelSize = kernelSize)
    }

    fun setBlockSize(blockSize: Double){
        _params.value = _params.value.copy(blockSize = blockSize.toInt())
    }

    fun setC(C: Double){
        _params.value = _params.value.copy(C = C)
    }

    fun setMinDisplaySize(minDisplaySize: Double){
        _params.value = _params.value.copy(minDisplaySize = minDisplaySize)
    }
}