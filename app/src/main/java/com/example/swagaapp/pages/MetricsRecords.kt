package com.example.swagaapp.pages

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import com.example.swagaapp.AppViewModel
import com.example.swagaapp.R
import com.example.swagaapp.storage.repositories.DeviceParameters
import com.example.swagaapp.storage.repositories.Session

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MetricsRecords(viewModel: AppViewModel, deviceType: String?){
    val sessions by viewModel.sessions.collectAsState()
    val bitmaps by viewModel.bitmaps.collectAsState()
    val devices by viewModel.devices.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDevices(true)
        viewModel.loadSessionsWithMetrics(deviceType, true)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val rowHeight = pageHeight * 0.1f

            sessions.forEachIndexed { i, session ->
                val currentDevice = devices.first { it.id == session.session.deviceID }
                RecordRow(
                    bitmap = (bitmaps[session.session.deviceID]?.value ?: BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus)).asImageBitmap(),
                    session = session,
                    device = currentDevice,
                    onClick = {},
                    modifier = Modifier
                        .height(rowHeight)
                        .then(
                            if(i != sessions.size - 1){
                                Modifier.drawBehind {
                                    val y = size.height

                                    drawLine(
                                        Color.LightGray,
                                        Offset(0f, y),
                                        Offset(size.width, y),
                                        3f
                                    )
                                }
                            }
                            else{
                                Modifier
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun RecordRow(modifier: Modifier = Modifier, bitmap: ImageBitmap, device: DeviceParameters, session: Session, onClick: () -> Unit ){
    val sheetPadding = 15.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(modifier)
    ){
        Row(
            modifier = Modifier
                .padding(sheetPadding),
            horizontalArrangement = Arrangement.spacedBy(sheetPadding, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                bitmap = bitmap,
                contentDescription = "",
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
            )

            Text(device.deviceName)
        }
    }
}