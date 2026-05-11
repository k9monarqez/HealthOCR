package com.example.healthocr.pages.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.healthocr.AppViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthocr.db.SessionInfo
import com.example.healthocr.storage.repositories.Session
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun Record(viewModel: AppViewModel, sessionID: Long){
    val session by viewModel.selectedSession.collectAsState()
    val devices by viewModel.devices.collectAsState()

    var isModifying by remember { mutableStateOf(false) }
    LaunchedEffect(isModifying) {
        viewModel.loadSession(sessionID)
    }

    session?.let { session ->
        val mutableMetrics = remember { session.metrics.map { it.key to mutableStateOf(it.value) }.toMap().toMutableMap() }
        val mutableMetricsBackup = remember { session.metrics.map { it.key to it.value }.toMap() }

        BoxWithConstraints(
            modifier = Modifier
                .padding(24.dp)
        ) {
            val pageWidth = maxWidth
            val pageHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val currentDevice = devices.firstOrNull { it.id == session.sessionInfo.deviceID }
                currentDevice?.let { currentDevice ->
                    Text(
                        "Устройство: ${currentDevice.deviceName}",
                        fontSize = 20.sp
                    )
                }
                val timeOfCreationString = timestampToString(
                    LocalDateTime.ofEpochSecond(
                        session.sessionInfo.created,
                        0,
                        ZoneOffset.UTC
                    )
                )
                Text(
                    "Создано: ${timeOfCreationString.first + ", " + timeOfCreationString.second}",
                    fontSize = 20.sp
                )
                val timeOfUpdatingString = null
                timeOfUpdatingString?.let {
                    Text(
                        "Обновлено: ${timeOfCreationString.first + ", " + timeOfCreationString.second}",
                        fontSize = 20.sp
                    )
                }

                mutableMetrics.forEach { metric ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var metricState by metric.value
                        Box(
                            modifier = Modifier
                                .width(pageWidth * 0.2f)
                        ) {
                            Text(
                                metric.key.metricCode,
                                fontSize = 20.sp
                            )
                        }

                        Box() {
                            TextField(
                                metricState,
                                onValueChange = {
                                    var newString = it
                                    metricState = if (metric.key.isNumeric) {
                                        if (metricState == "0") newString =
                                            newString.replace("0", "")
                                        if ('.' in metric.key.allowedSymbols) {
                                            (newString.filter { it.isDigit() || it == '.' }
                                                .trimStart('0').toDoubleOrNull() ?: 0.0)
                                                .coerceIn(metric.key.range!!).toString()
                                        } else {
                                            (newString.filter { it.isDigit() }.trimStart('0')
                                                .toDoubleOrNull() ?: 0.0)
                                                .coerceIn(metric.key.range!!).toInt().toString()
                                        }
                                    } else {
                                        newString.filter { it in metric.key.allowedSymbols }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (metric.key.isNumeric) KeyboardType.Decimal else KeyboardType.Text
                                ),
                                enabled = isModifying
                            )
                        }

                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.Center
            ) {
                if (isModifying) {
                    Button(
                        onClick = {
                            isModifying = false
                            mutableMetricsBackup.forEach { key, value ->
                                mutableMetrics[key]?.value = value
                            }
                        },
                    ) {
                        Text("Отменить")
                    }
                    Button(
                        onClick = {
                            viewModel.updateSessionWithMetrics(
                                Session(
                                    sessionInfo = session.sessionInfo.copy(updated = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)),
                                    metrics = mutableMetrics.map { it.key to it.value.value }.toMap(),
                                    metricsID = session.metricsID
                                )
                            )

                            isModifying = false
                        }
                    ) {
                        Text("Сохранить")
                    }
                } else {
                    Button(
                        onClick = {
                            isModifying = true
                        }
                    ) {
                        Text("Редактировать")
                    }
                }
            }
        }
    }
}