package com.example.healthocr

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthocr.db.AppRoomDatabase
import com.example.healthocr.ocr.devices.Device
import com.example.healthocr.storage.repositories.DeviceParameters
import com.example.healthocr.storage.repositories.DeviceRepository
import com.example.healthocr.storage.Metrics
import com.example.healthocr.storage.repositories.Session
import com.example.healthocr.storage.repositories.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import org.opencv.core.Mat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppViewModel(
    application: Application
): AndroidViewModel(application) {
    val showBottomNavBar = mutableStateOf(false)
    var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }

    val _mat = MutableStateFlow(Mat())
    var mat: StateFlow<Mat> = _mat.asStateFlow()

    fun setMat(value: Mat){
        clearMat()
        _mat.value = value
    }

    fun clearMat(){
        _mat.value.release()
    }

    private val deviceRepository: DeviceRepository
    private val sessionRepository: SessionRepository

    private val _devices = MutableStateFlow<List<DeviceParameters>>(emptyList())
    val devices: StateFlow<List<DeviceParameters>> = _devices.asStateFlow()

    private val _bitmaps = MutableStateFlow<MutableMap<Long, MutableState<Bitmap?>>>(mutableMapOf())
    val bitmaps: StateFlow<Map<Long, MutableState<Bitmap?>>> = _bitmaps.asStateFlow()

    private val _selectedDevice = MutableStateFlow<DeviceParameters?>(null)
    val selectedDevice: StateFlow<DeviceParameters?> = _selectedDevice.asStateFlow()

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()

    fun setSelectedDevice(device: DeviceParameters){
        _selectedDevice.value = device
    }

    init {
        val db = AppRoomDatabase.getInstance(application)
        val appDAO = db.getAppDAO()
        deviceRepository = DeviceRepository(application.applicationContext, appDAO)
        sessionRepository = SessionRepository(application.applicationContext, appDAO)

        viewModelScope.launch(Dispatchers.IO) {
            _devices.value = deviceRepository.getDevices()
            _selectedDevice.value = if(!_devices.value.isEmpty()) _devices.value[0] else null
            _devices.value.forEach { device ->
                _bitmaps.value[device.id] = mutableStateOf(null)
                _bitmaps.value[device.id]?.let{
                    loadDeviceBitmap(device.deviceImageURI, it)
                }
            }
        }
    }

    fun loadDevices(withDeleted: Boolean = false){
        viewModelScope.launch(Dispatchers.IO) {
            _devices.value = deviceRepository.getDevices(withDeleted)
            _devices.value.forEach { device ->
                _bitmaps.value[device.id] = mutableStateOf(null)
                _bitmaps.value[device.id]?.let{
                    loadDeviceBitmap(device.deviceImageURI, it)
                }
            }
        }
    }

    fun loadDeviceBitmap(path: String, bitmap: MutableState<Bitmap?>){
        viewModelScope.launch(Dispatchers.IO) {
            bitmap.value = deviceRepository.loadDeviceBitmap(path)
        }
    }

    fun addDevice(device: Device, deviceName: String, deviceBitmap: Bitmap){
        viewModelScope.launch(Dispatchers.IO) {
            deviceRepository.addDevice(device, deviceName, deviceBitmap)
            loadDevices()
        }
    }

    fun addSessionWithMetrics(metrics: Map<Metrics, String>, sessionTime: LocalDateTime){
        viewModelScope.launch(Dispatchers.IO){
            selectedDevice.value?.let { device ->
                val sessionID = sessionRepository.addSession(sessionTime, device.id)
                sessionRepository.addMetrics(metrics, sessionID)
            }
        }
    }

    fun loadSessionsWithMetrics(type: String? = null, descendingSort: Boolean){
        viewModelScope.launch(Dispatchers.IO){
            _sessions.value = sessionRepository.getSessionsWithMetrics(type, descendingSort)
        }
    }
}