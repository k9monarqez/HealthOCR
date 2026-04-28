package com.example.swagaapp.pages.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import com.example.swagaapp.AppViewModel
import com.example.swagaapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalSheets(viewModel: AppViewModel, deviceSelectorStates: Pair<MutableState<Boolean>, SheetState>, toDeviceSetup: () -> Unit){
    var showDeviceCreationSelector by remember { mutableStateOf(false) }
    val deviceCreationSelectorState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val devices by viewModel.devices.collectAsState()

    if(deviceSelectorStates.first.value){
        ModalBottomSheet(
            onDismissRequest = { deviceSelectorStates.first.value = false },
            sheetState = deviceSelectorStates.second,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier
                .displayCutoutPadding()
        ) {
            val sheetPadding = 15.dp
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = sheetPadding, bottom = sheetPadding)
            ){
                val bitmaps: List<MutableState<Bitmap?>> by remember { mutableStateOf(List(devices.size){mutableStateOf(null)}) }
                LaunchedEffect(devices) {
                    devices.forEachIndexed { i, device ->
                        viewModel.loadDeviceBitmap(device.deviceImageURI, bitmaps[i])
                    }
                }
                devices.forEachIndexed{ i, device ->
                    ModalSheetOption(
                        bitmap = (bitmaps[i].value ?: BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus)).asImageBitmap(),
                        contentDescription = "${deviceTypeToRu(device.deviceType)}\n${device.deviceName}",
                        onClick = {
                            deviceSelectorStates.first.value = false
                            viewModel.setSelectedDevice(device)
                        },
                        modifier = Modifier
                            .drawBehind {
                                val y = size.height

                                drawLine(
                                    Color.LightGray,
                                    Offset(0f, y),
                                    Offset(size.width, y),
                                    3f
                                )
                            }
                    )
                }

                ModalSheetOption(
                    bitmap = BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus)
                        .asImageBitmap(),
                    contentDescription = "Добавить устройство",
                    onClick = { showDeviceCreationSelector = true; viewModel.clearMat() },
                    modifier = Modifier
                )
            }
        }
    }

    if(showDeviceCreationSelector){
        ModalBottomSheet(
            onDismissRequest = { showDeviceCreationSelector = false },
            sheetState = deviceCreationSelectorState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier
                .displayCutoutPadding()
        ) {
            val sheetPadding = 15.dp
            Column(
                modifier = Modifier
                    .padding(top = sheetPadding, bottom = sheetPadding)
            ){
                ModalSheetOption(
                    bitmap = BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus)
                        .asImageBitmap(),
                    contentDescription = "Тонометр",
                    onClick = {
                        toDeviceSetup()
                    },
                    modifier = Modifier
                )
                ModalSheetOption(
                    bitmap = BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus)
                        .asImageBitmap(),
                    contentDescription = "Анализатор крови",
                    onClick = {},
                    modifier = Modifier
                )
                ModalSheetOption(
                    bitmap = BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus)
                        .asImageBitmap(),
                    contentDescription = "Анализатор мочи",
                    onClick = {},
                    modifier = Modifier
                )
                ModalSheetOption(
                    bitmap = BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus)
                        .asImageBitmap(),
                    contentDescription = "Коагулометр",
                    onClick = {},
                    modifier = Modifier
                )
                ModalSheetOption(
                    bitmap = BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus)
                        .asImageBitmap(),
                    contentDescription = "Пульсоксиметр",
                    onClick = {},
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun ModalSheetOption(bitmap: ImageBitmap, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier){
    val sheetPadding = 15.dp
    val optionHeight = 30.dp
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
                contentDescription = contentDescription,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(optionHeight)
            )

            Text(contentDescription)
        }
    }
}

fun deviceTypeToRu(deviceType: String): String{
    return when(deviceType){
        "Tonometer" -> {"Тонометр"}
        else -> {"Unknown"}
    }
}