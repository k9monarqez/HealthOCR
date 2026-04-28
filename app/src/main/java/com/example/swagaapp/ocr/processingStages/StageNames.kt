package com.example.swagaapp.ocr.processingStages

enum class StageNames {
    DigitsErosion,
    DisplaySearch,
    SSDSearch
}

fun toStageClasses(stageParams: StageParams): ProcessingStage<*>{
    val stage = when(stageParams){
        is DisplaySearchParams -> DisplaySearch(stageParams)
        is DigitsErosionParams -> DigitsErosion(stageParams)
        is SSDSearchParams -> SSDSearch(stageParams)
    }
    return stage
}