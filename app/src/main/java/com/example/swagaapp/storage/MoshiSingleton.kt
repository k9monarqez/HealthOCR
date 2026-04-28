package com.example.swagaapp.storage

import com.example.swagaapp.ocr.processingStages.DigitsErosionParams
import com.example.swagaapp.ocr.processingStages.DisplaySearchParams
import com.example.swagaapp.ocr.processingStages.SSDSearchParams
import com.example.swagaapp.ocr.processingStages.StageParams
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiSingleton {
    val moshi = Moshi.Builder()
        .add(DoublePairAdapter())
        .add(
            PolymorphicJsonAdapterFactory.of(StageParams::class.java, "type")
                .withSubtype(DigitsErosionParams::class.java, "DigitsErosionParams")
                .withSubtype(DisplaySearchParams::class.java, "DisplaySearchParams")
                .withSubtype(SSDSearchParams::class.java, "SSDSearchParams")
        )
        .add(KotlinJsonAdapterFactory())
        .build()
}

