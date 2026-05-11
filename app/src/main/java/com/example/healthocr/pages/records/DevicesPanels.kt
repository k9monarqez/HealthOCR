package com.example.healthocr.pages.records

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthocr.AppViewModel
import com.example.healthocr.ocr.devices.DevicesNames
import com.example.healthocr.ui.theme.BarColor
import kotlin.collections.chunked
import kotlin.collections.forEach

sealed class DevicePanelData(
    val type: DevicesNames,
    val text: String,
    val imgURI: String
) {
    object TonometerPanel: DevicePanelData(
        type = DevicesNames.Tonometer,
        text = "Тонометр",
        imgURI = ""
    )
    object CoagulometerPanel: DevicePanelData(
        type = DevicesNames.Coagulometer,
        text = "Коагулометр",
        imgURI = ""
    )
    object PulseOxymeterPanel: DevicePanelData(
        type = DevicesNames.PulseOxymeter,
        text = "Пульсоксиметр",
        imgURI = ""
    )
}

@Composable
fun DevicesPanels(
    viewModel: AppViewModel,
    paddingValues: PaddingValues,
    toMetricsRecords: (String) -> Unit
){
    val options = listOf(
        DevicePanelData.TonometerPanel,
        DevicePanelData.CoagulometerPanel,
        DevicePanelData.PulseOxymeterPanel
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        val chunkedOptions = options.chunked(2)
        chunkedOptions.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                it.forEach { option ->
                    DevicePanel(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        option,
                        toMetricsRecords
                    )
                }
                if(it.size < 2){
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)){}
                }
            }
        }
    }
}

@Composable
fun DevicePanel(modifier: Modifier = Modifier, devicePanelData: DevicePanelData, toMetricsRecords: (String) -> Unit){
    val isPressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(10))
            .pointerInput(Unit){
                detectTapGestures(
                    onPress = {
                        isPressed.value = true
                        tryAwaitRelease()
                        isPressed.value = false
                    },
                    onTap = {
                        toMetricsRecords(devicePanelData.type.name)
                    }

                )
            }
            .background(BarColor),
        contentAlignment = Alignment.Center
    ){
        Text(
            devicePanelData.text,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}