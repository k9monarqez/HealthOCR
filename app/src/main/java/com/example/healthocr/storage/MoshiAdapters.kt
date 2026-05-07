package com.example.healthocr.storage

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class DoublePairAdapter {

    @ToJson
    fun toJson(pair: Pair<Double, Double>): String {
        return "${pair.first},${pair.second}"
    }

    @FromJson
    fun fromJson(json: String): Pair<Double, Double> {
        val parts = json.split(",")
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid Pair format: $json")
        }
        return Pair(parts[0].toDouble(), parts[1].toDouble())
    }
}