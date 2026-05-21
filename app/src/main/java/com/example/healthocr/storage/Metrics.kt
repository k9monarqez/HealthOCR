package com.example.healthocr.storage

enum class Metrics(
    val metricCode: String,
    val metricName: String,
    val unit: String,
    val isNumeric: Boolean,
    val allowedSymbols: String,
    val range: ClosedFloatingPointRange<Double>?
) {
    SYSTOLIC_PRESSURE("SYS", "Давление верхнее", "mmHg", true, "0123456789", 0.0..300.0),
    DIASTOLIC_PRESSURE("DIA", "Давление нижнее", "mmhg", true, "0123456789", 0.0..300.0),
    PULSE("PUL", "Пульс","bpm", true, "0123456789",0.0..300.0),
    INTERNATIONAL_NORMALIZED_RATIO("INR", "МНО", "", true, "0123456789.", 0.0..10.0)
    ;

    companion object {
        fun getTypeByMetricCode(code: String): Metrics{
            return Metrics.entries.first { metric ->
                metric.metricCode == code
            }
        }
    }
}