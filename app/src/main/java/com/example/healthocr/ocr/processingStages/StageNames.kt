package com.example.healthocr.ocr.processingStages

enum class StageNames {
    DigitsErosion,
    DisplaySearch,
    SSDSearch,
    Binarization
}

fun toStageClasses(stageParams: StageParams): ProcessingStage<*>{
    val stage = when(stageParams){
        is DisplaySearchParams -> DisplaySearch(stageParams)
        is DigitsErosionParams -> DigitsErosion(stageParams)
        is SSDSearchParams -> SSDSearch(stageParams)
        is AdaptiveBinarizationWithoutNoisesParams -> AdaptiveBinarizationWithoutNoises(stageParams)
    }
    return stage
}