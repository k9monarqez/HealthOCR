package com.example.healthocr.pages

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.example.healthocr.AppViewModel
import com.example.healthocr.ocr.devices.toDeviceClass
import com.example.healthocr.storage.Metrics
import java.io.File

@Composable
fun AnalyzedImage(viewModel: AppViewModel) {
    val context = LocalContext.current
    val deviceParams by viewModel.selectedDevice.collectAsState()
    val deviceClass = toDeviceClass(deviceParams!!)
    val mat by viewModel.mat.collectAsState()

    var extractedData by remember { mutableStateOf(mapOf<Metrics, MutableState<String?>>()) }
    val bitmap = remember {
        mutableStateOf(
            createBitmap(
                mat.width(),
                mat.height(),
                Bitmap.Config.ARGB_8888
            )
        )
    }

    var loading by remember { mutableStateOf(true) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .displayCutoutPadding()
            .navigationBarsPadding()
            .padding(5.dp),
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(maxWidth * 0.3f)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val bitmapHeight = pageHeight * 0.4f
                val metricsHeight = pageHeight * 0.5f
                val navigationButtonsHeight = pageHeight * 0.1f
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Black, RoundedCornerShape(5.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    deviceClass?.let { device ->
                        Image(
                            bitmap = bitmap.value.asImageBitmap(),
                            contentDescription = "",
                            modifier = Modifier
                                .height(bitmapHeight)
                        )

                        val metricsColumnSize = 6
                        val metricHeight = metricsHeight / (metricsColumnSize)
                        val metricWidth = pageWidth / 2f
                        val chunkedPairs = extractedData.toList().chunked(metricsColumnSize)
                        Row(
                            modifier = Modifier
                                .height(metricsHeight)
                        ) {
                            for (chunk in chunkedPairs) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(metricWidth)
                                        .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    for (pair in chunk) {
                                        Row(
                                            modifier = Modifier
                                                .height(metricHeight)
                                                .fillMaxWidth()
                                                .border(1.dp, Color.Black),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            val textAreaWidth = metricWidth * 0.4f
                                            val textFieldWidth = metricWidth - textAreaWidth
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.Black)
                                                    .width(textAreaWidth)
                                                    .fillMaxHeight(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    pair.first.metricCode,
                                                    modifier = Modifier,
                                                    fontSize = 20.sp,
                                                    color = Color.White,
                                                )
                                            }
                                            OutlinedTextField(
                                                value = pair.second.value.toString(),
                                                onValueChange = { change ->
                                                    pair.second.value =
                                                        change.filter { it.isDigit() }.take(3)
                                                },
                                                singleLine = true,
                                                textStyle = TextStyle(
                                                    textAlign = TextAlign.Center,
                                                    fontSize = 20.sp
                                                ),
                                                modifier = Modifier
                                                    .width(textFieldWidth)
                                                    .fillMaxHeight(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color.Transparent,
                                                    unfocusedBorderColor = Color.Transparent
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(navigationButtonsHeight),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Button(
                            onClick = {

                            }
                        ) {
                            Text("Отменить")
                        }

                        Button(
                            onClick = {
                                val mappedMetrics = mutableMapOf<Metrics, String>()
                                extractedData.forEach { pair ->
                                    pair.value.value?.let{
                                        mappedMetrics[pair.key] = it
                                    }
                                }
                                deviceClass?.let {
                                    viewModel.addSessionWithMetrics(mappedMetrics, it.getSessionTime())
                                }
                            }
                        ) {
                            Text("Сохранить")
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        deviceClass?.let {
            deviceClass.process(mat, bitmap, File(context.filesDir, "tesseract").absolutePath)
            extractedData = deviceClass.getMappedData().mapValues { (key, value) ->
                mutableStateOf(value)
            }
        }
        loading = false
    }
}