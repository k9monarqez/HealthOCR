package com.example.healthocr.pages

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.healthocr.AppViewModel
import com.example.healthocr.R
import com.example.healthocr.ocr.devices.DevicesNames
import com.example.healthocr.pages.acceptWindows.AcceptWindow
import com.example.healthocr.pages.sessionHistory.underline
import com.example.healthocr.storage.repositories.DeviceParameters

@Composable
fun DevicesList(
    viewModel: AppViewModel,
    toDeviceSetup: () -> Unit
){
    val devices by viewModel.devices.collectAsState()
    val bitmaps by viewModel.bitmaps.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDevices()
    }

    val showDeviceCreationSelector = remember { mutableStateOf(false) }
    if(showDeviceCreationSelector.value){
        DeviceCreationSelector(showDeviceCreationSelector, toDeviceSetup)
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
        ){
            devices.forEachIndexed { i, device ->
                DeviceCard(
                    modifier = Modifier
                        .height(pageHeight * 0.15f)
                        .then(
                            if (i != devices.size - 1) {
                                Modifier.underline()
                            } else {
                                Modifier
                            }
                        ),
                    viewModel,
                    device,
                    (bitmaps[device.id]?.value
                        ?: BitmapFactory.decodeResource(
                            LocalResources.current,
                            R.drawable.plus
                        )).asImageBitmap()
                    )
            }
        }

        FloatingActionButton(
            onClick = { showDeviceCreationSelector.value = true },
            containerColor = Color.White,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.BottomEnd)
                .offset((-25).dp, (-25).dp)
        ) {
            Icon(
                painterResource(R.drawable.plus),
                null
            )
        }
    }
}

@Composable
fun DeviceCard(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel,
    device: DeviceParameters,
    bitmap: ImageBitmap
){
    val paddingValue = 15.dp
    BoxWithConstraints(
        modifier = modifier
            .padding(paddingValue)
    ){
        val cardWidth = maxWidth
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(paddingValue)
        ){
            Image(bitmap, null)
            Column(
                modifier = Modifier
                    .width(cardWidth * 0.5f)
            ){
                Text(
                    DevicesNames.valueOf(device.deviceType).ru,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    device.deviceName
                )
            }
        }

        Text(
            "Удалить",
            modifier = Modifier
                .clickable(
                    onClick = {
                        viewModel.setAcceptWindow(AcceptWindow.DeleteDevice(device.id))
                    }
                )
                .align(Alignment.BottomEnd),
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold
        )
    }
}