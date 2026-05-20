package com.example.healthocr.storage.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.healthocr.db.AppDAO
import com.example.healthocr.db.DBDevice
import com.example.healthocr.ocr.devices.Device
import com.example.healthocr.ocr.processingStages.StageParams
import com.example.healthocr.storage.MoshiSingleton
import com.squareup.moshi.adapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class DeviceParameters(
    val id: Long,
    val deviceType: String,
    val deviceName: String,
    val deviceImageURI: String,
    val stages: Map<String, StageParams>
)

class DeviceRepository(private val context: Context, private val appDAO: AppDAO) {
    private val devicesImagesDir: File
        get() = File(context.filesDir, "devicesImages").also {
            if (!it.exists()) it.mkdirs()
        }

    val moshi = MoshiSingleton.moshi

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun getDevices(withDeleted: Boolean = false): List<DeviceParameters>{
        return withContext(Dispatchers.IO){
            try {
                val dbDevicesList = appDAO.getDevices()
                val devicesList = mutableListOf<DeviceParameters>()
                val adapter = moshi.adapter<Map<String, StageParams>>()
                dbDevicesList.forEach { dbDevice ->
                    val stages = adapter.fromJson(dbDevice.stages)
                    stages?.let {
                        if(!withDeleted && dbDevice.isDeleted == 0L || withDeleted){
                            devicesList.add(
                                DeviceParameters(
                                    id = dbDevice.id,
                                    deviceType = dbDevice.type,
                                    deviceName = dbDevice.name,
                                    deviceImageURI = dbDevice.imagePath,
                                    stages = it
                                )
                            )
                        }
                    }
                }

                return@withContext devicesList
            } catch (e: Exception){
                Log.e("DeviceRepository", "Error: DeviceRepository.getDevices()\n${e.message}")
                return@withContext emptyList()
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun addDevice(device: Device, deviceName: String, deviceBitmap: Bitmap){
        withContext(Dispatchers.IO){
            val stagesParamsMap = device.pipeline.associateBy(
                {it.name.name}, {it.params}
            )
            val adapter = moshi.adapter<Map<String, StageParams>>()
            appDAO.addDevice(
                DBDevice(
                    id = 0,
                    type = device.type.name,
                    name = deviceName,
                    stages = adapter.toJson(stagesParamsMap),
                    imagePath = storeDeviceBitmap(deviceBitmap),
                    isDeleted = 0
                )
            )
        }
    }

    suspend fun deleteDevice(deviceID: Long){
        withContext(Dispatchers.IO){
            appDAO.deleteDevice(deviceID)
        }
    }

    suspend fun storeDeviceBitmap(bitmap: Bitmap): String{
        return withContext(Dispatchers.IO){
            val imageName = "${System.currentTimeMillis()}.png"
            val file = File(devicesImagesDir, imageName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            file.absolutePath
        }
    }

    suspend fun loadDeviceBitmap(path: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(path)
            } else {
                null
            }
        }
    }
}