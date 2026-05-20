package com.example.healthocr.pages.acceptWindows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthocr.AppViewModel

@Composable
fun DeleteDeviceWindow(viewModel: AppViewModel){
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight

        Box(
            modifier = Modifier
                .height(pageHeight * 0.3f)
                .padding(24.dp)
                .fillMaxSize()
                .background(Color.White, RoundedCornerShape(10)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Вы уверены, что хотите удалить устройство?",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    Button(
                        onClick = {
                            viewModel.clearAcceptWindow()
                        }
                    ) {
                        Text("Нет")
                    }
                    Button(
                        onClick = {
                            val deviceID = (viewModel.currentWindow.value as AcceptWindow.DeleteDevice).deviceID
                            viewModel.deleteDevice(deviceID)
                            viewModel.clearAcceptWindow()
                        },
                        colors = ButtonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Red,
                            disabledContentColor = Color.Red
                        )
                    ) {
                        Text("Да")
                    }
                }
            }
        }
    }
}