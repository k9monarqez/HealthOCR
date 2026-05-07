package com.example.swagaapp.ocr.processingStages

import org.opencv.core.Mat

interface ProcessingStage<T: StageParams> {
    val description: String
    val params: T
    val name: StageNames
    var mat: Mat
    fun process(): Mat
    fun getParamsAsControllers(): List<ParamController>
}

sealed class ParamController {
    data class Slider(
        val label: String,
        val currentValue: Double,
        val range: ClosedFloatingPointRange<Double>,
        val onValueChanged: (Double) -> Unit
    ) : ParamController()

    data class RangeSlider(
        val label: String,
        val currentMin: Double,
        val currentMax: Double,
        val range: ClosedFloatingPointRange<Double>,
        val onRangeChanged: (ClosedFloatingPointRange<Double>) -> Unit
    ) : ParamController()

    data class Checkbox(
        val label: String,
        val isChecked: Boolean,
        val onCheckChanged: (Boolean) -> Unit
    ) : ParamController()
}