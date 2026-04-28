package com.example.swagaapp.storage

enum class Metrics(
    val metricCode: String,
    val metricName: String,
    val unit: String,
    val isNumeric: Boolean,
    val allowedSymbols: String
) {
    SYSTOLIC_PRESSURE("SYS", "Давление верхнее", "mmHg", true, "0123456789"),
    DIASTOLIC_PRESSURE("DIA", "Давление нижнее", "mmhg", true, "0123456789"),
    PULSE("PUL", "Пульс","/min", true, "0123456789");

    companion object {
        fun getTypeByMetricCode(code: String): Metrics{
            return Metrics.entries.first { metric ->
                metric.metricCode == code
            }
        }
    }
}