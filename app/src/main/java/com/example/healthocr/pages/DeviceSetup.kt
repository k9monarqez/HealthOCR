package com.example.healthocr.pages

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.example.healthocr.AppViewModel
import com.example.healthocr.ocr.DeviceImageProcessing
import com.example.healthocr.ocr.processingStages.ParamController
import com.example.healthocr.ocr.devices.Tonometer
import com.example.healthocr.ocr.processingStages.DigitsErosion
import com.example.healthocr.pages.camera.Camera
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Rect
import java.io.File
import kotlin.math.min

@Composable
fun DeviceSetup(viewModel: AppViewModel, toCamera: () -> Unit){
    val context = LocalContext.current
    val mat by viewModel.mat.collectAsState()

    if(mat.empty()){
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize(),
        ){
            Camera(viewModel, {}, {}, false)
            val viewWidth = maxWidth
            val viewHeight = maxHeight
            Box(modifier = Modifier
                .displayCutoutPadding()
                .size(viewWidth, viewHeight * 0.1f)
                .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ){
                Box(
                    modifier = Modifier
                        .size(viewWidth * 0.9f, viewHeight * 0.1f * 0.7f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        "Сделайте снимок устройства, полностью поместив его в рамку",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    else{
        val deviceClass by remember { mutableStateOf(Tonometer()) }
        var currentProcessingStageIndex by remember { mutableStateOf(0) }
        var currentProcessingStage by remember(currentProcessingStageIndex) {
            mutableStateOf(deviceClass.pipeline[currentProcessingStageIndex])
        }
        var imgBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var currentMat by remember{ mutableStateOf(Mat()) }
        var extractedText by remember { mutableStateOf("") }

        var showNameInput by remember { mutableStateOf(false) }

        LaunchedEffect(currentProcessingStage.params) {
            if(deviceClass.pipeline.first().mat.empty()) deviceClass.pipeline.first().mat = mat.clone()
            currentMat = currentProcessingStage.process()
            imgBitmap = createBitmap(currentMat.width(), currentMat.height(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(currentMat, imgBitmap)
            imgBitmap?.let{
                val bitmap = imgBitmap
                if(currentProcessingStage is DigitsErosion) {
                    extractedText = DeviceImageProcessing.extractText(
                        bitmap!!,
                        File(context.filesDir, "tesseract").absolutePath
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .displayCutoutPadding()
                .navigationBarsPadding()
                .background(Color.White)
        ) {
            val pageWidth = maxWidth
            val pageHeight = maxHeight

            val imageAreaHeight = pageHeight * 0.35f
            val stageDescriptionAreaHeight = pageHeight * 0.15f
            val slidersAreaHeight = pageHeight * 0.4f
            val bottomButtonsHeight = pageHeight * 0.1f

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                if(!showNameInput) {
                    Box(
                        modifier = Modifier
                            .height(imageAreaHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        imgBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "",
                                modifier = Modifier.fillMaxHeight()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(stageDescriptionAreaHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .dropShadow(
                                    shape = RoundedCornerShape(5.dp),
                                    shadow = Shadow(
                                        radius = 5.dp,
                                        spread = 3.dp,
                                        color = Color(0x40000000),
                                        offset = DpOffset(x = 0.dp, 4.dp)
                                    )
                                )
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ){
                            Text(currentProcessingStage.description, textAlign = TextAlign.Center)
                        }
                    }

                    val controllersScrollState = ScrollState(0)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(slidersAreaHeight)
                            .verticalScroll(controllersScrollState)
                            .padding(24.dp)
                    ) {
                        for (param in currentProcessingStage.getParamsAsControllers()) {
                            when (param) {
                                is ParamController.Slider -> {
                                    Text(text = "${param.label}: ${param.currentValue}")
                                    Slider(
                                        value = param.currentValue.toFloat(),
                                        onValueChange = {
                                            param.onValueChanged(it.toDouble()); println(
                                            param.currentValue
                                        )
                                        },
                                        valueRange = param.range.start.toFloat()..param.range.endInclusive.toFloat(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                is ParamController.RangeSlider -> {
                                    Text(text = "${param.label}: ${param.currentMin} - ${param.currentMax}")
                                    RangeSlider(
                                        value = param.currentMin.toFloat()..param.currentMax.toFloat(),
                                        onValueChange = { rangeSliderState ->
                                            param.onRangeChanged(rangeSliderState.start.toDouble()..rangeSliderState.endInclusive.toDouble())
                                        },
                                        valueRange = param.range.start.toFloat()..param.range.endInclusive.toFloat(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                is ParamController.Checkbox -> {

                                }
                            }
                        }
                        when (currentProcessingStage) {
                            is DigitsErosion -> {
                                Text(extractedText)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .height(bottomButtonsHeight),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                currentProcessingStageIndex--
                            },
                            enabled = currentProcessingStageIndex > 0
                        ) {
                            Text("Назад")
                        }

                        Button(
                            onClick = {
                                if(currentProcessingStageIndex < deviceClass.pipeline.size - 1){
                                    deviceClass.pipeline[++currentProcessingStageIndex].mat = currentMat.clone()
                                }
                                else{
                                    showNameInput = true
                                }
                            },
                            enabled = currentProcessingStageIndex < deviceClass.pipeline.size
                        ) {
                            Text("Вперёд")
                        }
                    }
                }
                else{
                    var deviceName by remember { mutableStateOf("") }

                    TextField(deviceName, onValueChange = {deviceName = it})
                    Button(
                        onClick = {
                            showNameInput = false
                        },
                        enabled = currentProcessingStageIndex > 0
                    ) {
                        Text("Назад")
                    }

                    Button(
                        onClick = {
                            val croppedMat = mat.clone()
                            val size = min(croppedMat.width(), croppedMat.height())
                            val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)

                            Utils.matToBitmap(
                                croppedMat.submat(Rect(0, 0, size, size)),
                                bitmap
                            )
                            viewModel.addDevice(deviceClass, deviceName, bitmap)
                            viewModel.clearMat()
                            toCamera()
                        },
                        enabled = currentProcessingStageIndex < deviceClass.pipeline.size
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}