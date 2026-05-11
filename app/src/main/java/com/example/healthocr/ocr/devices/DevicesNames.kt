package com.example.healthocr.ocr.devices

import com.example.healthocr.storage.repositories.DeviceParameters

enum class DevicesNames(val ru: String){
    Tonometer(ru = "Тонометр"),
    BloodAnalyzer(ru = "Анализатор крови"),
    UrineAnalyzer(ru = "Анализатор мочи"),
    Coagulometer(ru = "Коагулометр"),
    PulseOxymeter(ru = "Пульсоксиметр")
}

fun toDeviceClass(deviceParameters: DeviceParameters): Device?{
    return when(deviceParameters.deviceType){
        DevicesNames.Tonometer.name -> Tonometer(deviceParameters.stages)

        else -> null
    }
}