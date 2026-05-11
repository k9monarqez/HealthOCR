package com.example.healthocr.pages.records

import android.graphics.BitmapFactory
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthocr.AppViewModel
import com.example.healthocr.R
import com.example.healthocr.pages.camera.deviceTypeToRu
import com.example.healthocr.storage.repositories.DeviceParameters
import com.example.healthocr.storage.repositories.Session
import com.example.healthocr.ui.theme.BarColor
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun MetricsRecordsList(viewModel: AppViewModel, deviceType: String, toRecord: (Long) -> Unit){
    val sessions by viewModel.sessions.collectAsState()
    val bitmaps by viewModel.bitmaps.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val selectedSessions by viewModel.selectedSessionsMask.collectAsState()
    val sortingOrder by viewModel.sortDescending.collectAsState()

    LaunchedEffect(sortingOrder) {
        viewModel.loadDevices(true)
        viewModel.loadSessionsWithMetrics(deviceType)
        viewModel.setSelectedDeviceType(deviceType)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight

        val rowHeight = pageHeight * 0.1f
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            sessions.forEachIndexed { i, session ->
                val currentDevice = devices.first { it.id == session.sessionInfo.deviceID }
                RecordRow(
                    bitmap = (bitmaps[session.sessionInfo.deviceID]?.value
                        ?: BitmapFactory.decodeResource(
                            LocalResources.current,
                            R.drawable.plus
                        )).asImageBitmap(),
                    session = session,
                    device = currentDevice,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {
                                if (selectedSessions.contains(true)) {
                                    selectedSessions[i] = !selectedSessions[i]
                                } else {
                                    toRecord(session.sessionInfo.id)
                                }
                            },
                            onLongClick = {
                                selectedSessions[i] = true
                            }
                        )
                        .height(rowHeight)
                        .then(
                            if (i != sessions.size - 1) {
                                Modifier.drawBehind {
                                    val y = size.height

                                    drawLine(
                                        Color.LightGray,
                                        Offset(0f, y),
                                        Offset(size.width, y),
                                        3f
                                    )
                                }
                            } else {
                                Modifier
                            }
                        )
                        .background(
                            if (selectedSessions[i]) Color.LightGray else Color.Transparent
                        )
                )
            }
        }
        if(selectedSessions.contains(true)){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .background(Color.LightGray)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.showDarkBG()
                    },
                    modifier = Modifier,
                    border = BorderStroke(0.dp, Color.Transparent),
                    shape = RectangleShape,
                    colors = ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Bottom)
                    ){
                        Icon(
                            painter = painterResource(R.drawable.trash),
                            contentDescription = "remove",
                            tint = Color.Black,
                            modifier = Modifier
                                .scale(1.5f)
                        )
                        Text("Удалить")
                    }
                }
            }
        }
    }
}

@Composable
fun AcceptDeletionWindow(viewModel: AppViewModel, modifier: Modifier){
    val sessions by viewModel.sessions.collectAsState()
    val selectedSessions by viewModel.selectedSessionsMask.collectAsState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(10)),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text("Вы уверены, что хотите удалить записи в количестве ${ selectedSessions.count { it } } штук?", fontSize = 15.sp, textAlign = TextAlign.Center)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ){
                Button(
                    onClick = {
                        viewModel.hideDarkBG()
                    }
                ) {
                    Text("Нет")
                }
                Button(
                    onClick = {
                        val filteredSessions = sessions.filterIndexed {i, session ->
                            selectedSessions[i]
                        }.map { it.sessionInfo }
                        viewModel.deleteSessions(filteredSessions)
                        viewModel.hideDarkBG()
                    },
                    colors = ButtonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Red,
                        disabledContentColor = Color.Red
                    )
                ){
                    Text("Да")
                }
            }
        }
    }
}

@Composable
fun RecordRow(modifier: Modifier = Modifier, bitmap: ImageBitmap, device: DeviceParameters, session: Session){
    val sheetPadding = 15.dp
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
            .padding(sheetPadding)
    ){
        val rowWidth = maxWidth
        val rowHeight = maxHeight
        Row(
            modifier = Modifier,
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

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(rowWidth * 0.5f)
            ){
                Text(
                    device.deviceName,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    session.metrics.entries.joinToString(", ") { "${it.key.metricCode}: ${it.value}" },
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    minLines = 2
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.TopEnd)
        ){
            val timeOfCreation = LocalDateTime.ofEpochSecond(session.sessionInfo.created, 0, ZoneOffset.UTC)
            val timeOfCreationString = timestampToString(timeOfCreation)
            Text(
                timeOfCreationString.first + ",\n" + timeOfCreationString.second,
                fontSize = 10.sp,
                textAlign = TextAlign.End
            )
        }
    }
}

fun timestampToString(timestamp: LocalDateTime): Pair<String, String>{
    return Pair("${timestamp.dayOfMonth}.${("0" + timestamp.monthValue).take(2)}.${timestamp.year}",
        "${("0" + timestamp.hour).take(2)}:${("0" + timestamp.minute).take(2)}:${("0" + timestamp.second).take(2)}"
    )
}