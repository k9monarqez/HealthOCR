package com.example.healthocr

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.healthocr.db.AppRoomDatabase
import com.example.healthocr.db.Metric
import com.example.healthocr.db.SessionInfo
import com.example.healthocr.db.SessionWithMetrics
import com.example.healthocr.nav.NavRoutes
import com.example.healthocr.ocr.devices.Device
import com.example.healthocr.ocr.devices.Tonometer
import com.example.healthocr.pages.ExportPeriod
import com.example.healthocr.pages.ExportPeriodData
import com.example.healthocr.pages.acceptWindows.AcceptWindow
import com.example.healthocr.pages.statistics.ChartPeriod
import com.example.healthocr.storage.repositories.DeviceParameters
import com.example.healthocr.storage.repositories.DeviceRepository
import com.example.healthocr.storage.Metrics
import com.example.healthocr.storage.repositories.MetricsRepository
import com.example.healthocr.storage.repositories.SessionRepository
import com.example.healthocr.storage.repositories.getEndOfDate
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import org.opencv.core.Mat
import java.io.File
import java.io.FileWriter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppViewModel(
    application: Application
): AndroidViewModel(application) {
    val showBottomNavBar = mutableStateOf(false)
    var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val _darkBackground = MutableStateFlow(false)
    var darkBackground: StateFlow<Boolean> = _darkBackground.asStateFlow()

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

    private val _bitmaps = MutableStateFlow<MutableMap<Long, MutableState<Bitmap?>>>(mutableMapOf())
    val bitmaps: StateFlow<Map<Long, MutableState<Bitmap?>>> = _bitmaps.asStateFlow()

    private val _selectedDevice = MutableStateFlow<DeviceParameters?>(null)
    val selectedDevice: StateFlow<DeviceParameters?> = _selectedDevice.asStateFlow()

    private val _deviceClass = MutableStateFlow<Device>(Tonometer())
    val deviceClass: StateFlow<Device> = _deviceClass.asStateFlow()

    fun setDeviceClass(device: Device){
        _deviceClass.value = device
    }

    private val _sessions = MutableStateFlow<List<SessionWithMetrics>>(emptyList())
    val sessions: StateFlow<List<SessionWithMetrics>> = _sessions.asStateFlow()

    private val _selectedSession = MutableStateFlow<SessionWithMetrics?>(null)
    val selectedSession: StateFlow<SessionWithMetrics?> = _selectedSession.asStateFlow()

    private val _newestMetrics = MutableStateFlow<Map<Metrics, String>>(mapOf())
    val newestMetrics: StateFlow<Map<Metrics, String>> = _newestMetrics.asStateFlow()

    fun loadNewestMetrics(){
        viewModelScope.launch(Dispatchers.IO){
            _newestMetrics.value = metricsRepository.getNewestMetricsOfEveryType()
        }
    }

    fun loadSession(sessionID: Long){
        viewModelScope.launch(Dispatchers.IO) {
            _selectedSession.value = sessionRepository.getSession(sessionID)
        }
    }

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
                metricsRepository.addMetrics(metrics, sessionID)
            }
        }
    }

    fun loadSessions(){
        viewModelScope.launch(Dispatchers.IO){
            _sessions.value = sessionRepository.getAllSessions( _sortDescending.value)
        }
    }

    fun deleteSession(session: SessionInfo){
        viewModelScope.launch(Dispatchers.IO){
            sessionRepository.deleteSessions(session)
            loadSessions()
        }
    }

    fun updateSession(session: SessionWithMetrics){
        viewModelScope.launch(Dispatchers.IO){
            sessionRepository.updateSessionWithMetrics(session)
        }
    }

    private val _sortDescending = MutableStateFlow(true)
    val sortDescending: StateFlow<Boolean> = _sortDescending.asStateFlow()

    fun flipSortingOrder(){
        _sortDescending.value = !_sortDescending.value
    }

    private val _currentWindow = MutableStateFlow<AcceptWindow>(AcceptWindow.None)
    val currentWindow: StateFlow<AcceptWindow> = _currentWindow.asStateFlow()

    fun setAcceptWindow(window: AcceptWindow){
        _darkBackground.value = true
        _currentWindow.value = window
    }

    fun clearAcceptWindow(){
        _darkBackground.value = false
        _currentWindow.value = AcceptWindow.None
    }

    private val _metricsValues = MutableStateFlow<List<Metric>>(listOf())
    val metricsValues: StateFlow<List<Metric>> = _metricsValues.asStateFlow()

    fun loadMetricsByChartPeriod(metrics: List<Metrics>, current: LocalDateTime){
        viewModelScope.launch(Dispatchers.IO){
            _metricsValues.value = metricsRepository.getMetricsByChartPeriod(metrics, current, _chartPeriod.value)
        }
    }

    suspend fun loadMetrics(metrics: List<Metrics>, start: Long, end: Long) {
        _metricsValues.value = withContext(Dispatchers.IO) {
            metricsRepository.getMetrics(metrics, start, end)
        }
    }

    private val _chartPeriod = MutableStateFlow(ChartPeriod.DAY)
    val chartPeriod: StateFlow<ChartPeriod> = _chartPeriod.asStateFlow()

    fun setChartPeriod(period: ChartPeriod){
        _chartPeriod.value = period
    }

    fun deleteDevice(deviceID: Long){
        viewModelScope.launch(Dispatchers.IO){
            deviceRepository.deleteDevice(deviceID)
        }
    }

    private val _exportPeriodData = MutableStateFlow<ExportPeriodData>(ExportPeriodData.DayData(LocalDateTime.now()))
    val exportPeriodData: StateFlow<ExportPeriodData> = _exportPeriodData.asStateFlow()

    fun setExportPeriodData(exportPeriodData: ExportPeriodData){
        _exportPeriodData.value = exportPeriodData
    }

    fun exportMetricsByExportPeriod(exportPeriod: ExportPeriod, fileName: String){
        viewModelScope.launch(Dispatchers.IO){
            when(exportPeriod){
                ExportPeriod.DAY -> {
                    val day = (_exportPeriodData.value as ExportPeriodData.DayData).day
                        .toLocalDate()

                    val start = day
                        .atStartOfDay(ZoneId.systemDefault())
                        .toEpochSecond()

                    val end = day
                        .atTime(LocalTime.MAX)
                        .atZone(ZoneId.systemDefault())
                        .toEpochSecond()
                    loadMetrics(Metrics.entries, start, end)
                    println(_metricsValues.value.size)
                    exportToCSV(fileName)
                }
                ExportPeriod.LAST_7_DAYS -> {
                    val end = LocalDate.now()
                        .atTime(LocalTime.MAX)
                        .atZone(ZoneId.systemDefault())
                        .toEpochSecond()

                    val start = LocalDate.now()
                        .minusDays(7)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toEpochSecond()
                    loadMetrics(Metrics.entries, start, end)
                    println(_metricsValues.value.size)
                    exportToCSV(fileName)
                }
                ExportPeriod.LAST_30_DAYS -> {
                    val start = LocalDate.now()
                        .minusDays(30)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toEpochSecond()

                    val end = LocalDate.now()
                        .atTime(LocalTime.MAX)
                        .atZone(ZoneId.systemDefault())
                        .toEpochSecond()

                    loadMetrics(Metrics.entries, start, end)
                    println(_metricsValues.value.size)
                    exportToCSV(fileName)
                }
                ExportPeriod.ANY_PERIOD -> {
                    val period = (_exportPeriodData.value as ExportPeriodData.PeriodData).period

                    val start = period.first.toLocalDate()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toEpochSecond()

                    val end = period.second.toLocalDate()
                        .atTime(LocalTime.MAX)
                        .atZone(ZoneId.systemDefault())
                        .toEpochSecond()

                    loadMetrics(Metrics.entries, start, end)
                    println(_metricsValues.value.size)
                    exportToCSV(fileName)
                }
            }
        }
    }

    private fun exportToCSV(fileName: String){
        val context = application.applicationContext
        val fileName = "metrics_${System.currentTimeMillis()}.csv"
        val csv = File(context.getExternalFilesDir(null), fileName)

        CSVWriter(FileWriter(csv)).use { writer ->
            writer.writeNext(arrayOf("date", "metric", "value"))
            _metricsValues.value.forEach { metric ->
                writer.writeNext(arrayOf(
                    LocalDateTime.ofEpochSecond(metric.created, 0, ZoneOffset.UTC).toString(),
                    metric.type,
                    metric.value
                ))
            }
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        ) ?: return

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            csv.inputStream().use { it.copyTo(outputStream) }
        }

        csv.delete()
    }
}