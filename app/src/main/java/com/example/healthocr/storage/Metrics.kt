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
    INTERNATIONAL_NORMALIZED_RATIO("INR", "МНО", "INR", true, "0123456789.", 0.0..10.0),

    UROBILINOGEN("URO", "Уробилиноген", "", false, listOf("Norm","+1","+2", ">=+3").joinToString { it }, null),
    BLOOD("BLD", "Кровь", "", false, listOf("-", "+-", "+1", "+2", "+3").joinToString { it }, null),
    BILIRUBIN("BIL", "Билирубин", "", false, listOf("-", "+1", "+2", "+3").joinToString { it }, null),
    KETONE("KET", "Кетоны", "", false,listOf("-", "+-", "+1", "+2", "+3").joinToString { it }, null),
    LEUCOCYTES("LEU", "Лейкоциты", "", false, listOf("-", "+-", "+1", "+2", "+3").joinToString { it }, null),
    GLUCOSE("GLU", "Глюкоза", "", false, listOf("-", "+-", "+1", "+2", "+3", "+4").joinToString { it }, null),
    PROTEIN("PRO", "Протеин", "", false, listOf("-", "+-", "+1", "+2", ">=+3").joinToString { it }, null),
    PH("PH", "Кислотность", "", false, listOf("5", "6", "7", "8", "9").joinToString { it }, null),
    NITRITE("NIT", "Нитриты", "", false, listOf("-", "+1").joinToString { it }, null),
    SPECIFIC_GRAVITY("SG", "Удельный вес", "", false, listOf("<=1.005", "1.010", "1.015", "1.020", "1.025", ">=1.030").joinToString { it }, null),
    ASCORBIC_ACID("VC", "Аскорбиновая кислота", "", false, listOf("-", "+-", "+1", "+2", "+3").joinToString { it }, null)
    ;

    companion object {
        fun getTypeByMetricCode(code: String): Metrics{
            return Metrics.entries.first { metric ->
                metric.metricCode == code
            }
        }
    }
}