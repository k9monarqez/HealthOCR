package com.example.swagaapp

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swagaapp.db.AppRoomDatabase
import com.example.swagaapp.ocr.devices.Device
import com.example.swagaapp.storage.repositories.DeviceParameters
import com.example.swagaapp.storage.repositories.DeviceRepository
import com.example.swagaapp.storage.Metrics
import com.example.swagaapp.storage.repositories.MetricsRepository
import com.example.swagaapp.storage.repositories.Session
import com.example.swagaapp.storage.repositories.SessionRepository
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
    private val metricsRepository: MetricsRepository

    private val _devices = MutableStateFlow<List<DeviceParameters>>(emptyList())
    val devices: StateFlow<List<DeviceParameters>> = _devices.asStateFlow()

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
        metricsRepository = MetricsRepository(application.applicationContext, appDAO)

        viewModelScope.launch(Dispatchers.IO) {
            _devices.value = deviceRepository.getDevices()
            _selectedDevice.value = if(!_devices.value.isEmpty()) _devices.value[0] else null
        }
    }

    fun loadDevices(){
        viewModelScope.launch(Dispatchers.IO) {
            _devices.value = deviceRepository.getDevices()
            println(_devices.value.size)
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
                metricsRepository.addMetrics(metrics, sessionID)
            }
        }
    }

    fun loadAllSessionsWithMetrics(descendingSort: Boolean){
        viewModelScope.launch(Dispatchers.IO){
            _sessions.value = sessionRepository.getAllSessionsWithMetrics(descendingSort)
        }
    }
}