package com.example.swagaapp.ocr.devices

import com.example.swagaapp.storage.repositories.DeviceParameters

enum class DevicesNames{
    Tonometer, BloodAnalyzer, UrineAnalyzer, Coagulometer, PulseOxymeter
}

fun toDeviceClass(deviceParameters: DeviceParameters): Device?{
    return when(deviceParameters.deviceType){
        DevicesNames.Tonometer.name -> Tonometer(deviceParameters.stages)

        else -> null
    }
}