package com.example.healthocr.pages.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageView
import com.example.healthocr.AppViewModel
import com.example.healthocr.R
import com.example.healthocr.ocr.DeviceImageProcessing
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import kotlin.math.roundToInt
import org.opencv.core.Rect

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Camera(
    viewModel: AppViewModel,
    toAnalyzedImage: () -> Unit,
    toDeviceSetup: () -> Unit,
    showDeviceSelectors: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            isTapToFocusEnabled = false
            setEnabledUseCases(IMAGE_CAPTURE)
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val cropImageView = CropImageView(context).apply {}

    var bmp by remember { mutableStateOf<Bitmap?>(null) }

    var cropRectPosition = Rect()
    val cropRectTopLeft = remember { mutableStateOf(Point(0.0, 0.0)) }
    val cropRectSize = remember { mutableStateOf(Offset.Zero) }

    var imgBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            cropImageView.setImageUriAsync(uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                imgBitmap = ImageDecoder.decodeBitmap(source)
            } else {
                imgBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    val mat by viewModel.mat.collectAsState()

    LaunchedEffect(Unit, mat, viewModel.devices) {
        previewView.controller = cameraController
        if(!mat.empty()) toAnalyzedImage()
    }

    fun capturePhoto() {
        cameraController.takePicture(
            viewModel.cameraExecutor,
            object: ImageCapture.OnImageCapturedCallback() {
                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    viewModel.setMat(
                        DeviceImageProcessing.cropImage(
                            imageProxy.image,
                            cropRectPosition,
                            Offset(previewView.width.toFloat(), previewView.height.toFloat())
                        )
                    )
                }
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val viewWidth = maxWidth
        val viewHeight = maxHeight

        val topAreaHeight = viewHeight * 0.1f
        val cropRectAreaHeight = viewHeight * 0.6f
        val bottomAreaHeight = viewHeight * 0.3f
        val buttonCircleRadius = viewWidth * 0.2f
        val displayCutoutPaddingValue = with(LocalDensity.current){ WindowInsets.displayCutout.asPaddingValues().calculateTopPadding().toPx() }

        var showDeviceSelector = remember { mutableStateOf(false) }
        val deviceSelectorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        if(showDeviceSelectors){
            ModalSheets(
                viewModel,
                Pair(showDeviceSelector, deviceSelectorSheetState),
                toDeviceSetup
            )
        }

        if(imgBitmap == null){
            if(bmp == null) {
//                AndroidView(
//                    factory = { previewView },
//                    modifier = Modifier
//                        .fillMaxSize()
//                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val factory = previewView.meteringPointFactory
                                val autoFocusPoint = factory.createPoint(
                                    offset.x,
                                    offset.y
                                )

                                cameraController.cameraControl?.startFocusAndMetering(
                                    FocusMeteringAction.Builder(
                                        autoFocusPoint,
                                        FocusMeteringAction.FLAG_AF
                                    ).apply {
                                        disableAutoCancel()
                                    }.build()
                                )
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.DarkGray, Color.Transparent),
                                    startY = 0f,
                                    endY = with(LocalDensity.current) { topAreaHeight.toPx() + displayCutoutPaddingValue }
                                )
                            )
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.DarkGray, Color.Transparent),
                                    endY = with(LocalDensity.current) { (viewHeight - bottomAreaHeight).toPx() + displayCutoutPaddingValue },
                                    startY = Float.POSITIVE_INFINITY
                                )
                            )
                            .fillMaxSize()
                            .displayCutoutPadding()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(topAreaHeight),
                                contentAlignment = Alignment.Center
                            ) {
                                if(showDeviceSelectors){
                                    val buttonHeight = topAreaHeight * 0.7f
                                    val selectedDevice by viewModel.selectedDevice.collectAsState()
                                    val bitmaps by viewModel.bitmaps.collectAsState()
                                    val iconSize = buttonHeight
                                    Box(
                                        modifier = Modifier
                                            .size(viewWidth * 0.9f, buttonHeight)
                                            .clickable(
                                                onClick = { showDeviceSelector.value = true }
                                            )
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color.White)
                                    ){
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.Start)
                                        ){
                                            if(selectedDevice != null){
                                                val iconBitmap = if(selectedDevice != null){
                                                    (bitmaps[selectedDevice!!.id]?.value ?: BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus))
                                                        .asImageBitmap()
                                                }
                                                else{
                                                    BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus).asImageBitmap()
                                                }
                                                Icon(
                                                    bitmap = iconBitmap,
                                                    contentDescription = "Selected device",
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier
                                                        .size(iconSize)
                                                )
                                                Column(){
                                                    Text(deviceTypeToRu(selectedDevice!!.deviceType), fontWeight = FontWeight.Bold)
                                                    Text(selectedDevice!!.deviceName)
                                                }
                                            }
                                            else{
                                                Box(
                                                    modifier = Modifier
                                                        .size(iconSize),
                                                    contentAlignment = Alignment.Center
                                                ){
                                                    Icon(
                                                        bitmap = BitmapFactory.decodeResource(LocalResources.current, R.drawable.plus).asImageBitmap(),
                                                        contentDescription = "Selected device",
                                                        tint = Color.Unspecified,
                                                        modifier = Modifier
                                                            .size(30.dp)
                                                    )
                                                }
                                                Text("Добавить устройство")
                                            }
                                        }
                                        if(selectedDevice != null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(iconSize, iconSize)
                                                    .align(Alignment.CenterEnd),
                                                contentAlignment = Alignment.Center
                                            ){
                                                Icon(
                                                    painterResource(R.drawable.settings),
                                                    contentDescription = "",
                                                )
                                            }

                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(viewWidth, cropRectAreaHeight),
                            ) {

                                val cropRectModifier = Modifier
                                    .onGloballyPositioned { position ->
                                        val coordinates = position.positionInRoot()
                                        cropRectTopLeft.value = Point(
                                            coordinates.x.toDouble(),
                                            coordinates.y.toDouble()
                                        )

                                        cropRectSize.value = Offset(position.size.width.toFloat(), position.size.height.toFloat())
                                    }

                                CropRect(
                                    modifier = cropRectModifier,
                                    mode = CropRectMode.MIRROR
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(bottomAreaHeight),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = viewHeight * 0.01f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(buttonCircleRadius)
                                        ) {}

                                        CaptureButton(
                                            modifier = Modifier
                                                .size(buttonCircleRadius),
                                            onClick = {
                                                cropRectPosition = Rect(
                                                    cropRectTopLeft.value,
                                                    Point(
                                                        cropRectTopLeft.value.x + cropRectSize.value.x,
                                                        cropRectTopLeft.value.y + cropRectSize.value.y
                                                    )
                                                )

                                                capturePhoto()
                                            }
                                        )

                                        OutlinedButton(
                                            onClick = {
                                                pickMedia.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            modifier = Modifier
                                                .size(buttonCircleRadius),
                                            border = BorderStroke(0.dp, Color.Transparent),
                                            shape = RectangleShape,
                                            colors = ButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = Color.Black,
                                                disabledContainerColor = Color.Transparent,
                                                disabledContentColor = Color.Transparent
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.gallery),
                                                contentDescription = "photo picker",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .scale(2f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else{
                Image(
                    bitmap = bmp!!.asImageBitmap(),
                    "",
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
        else{
            viewModel.showBottomNavBar.value = false
            val backgroundColor = averageColor(imgBitmap!!)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(backgroundColor))
                    .displayCutoutPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { cropImageView },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = viewHeight * 0.025f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ){
                    Button(
                        modifier = Modifier,
                        onClick = {
                            viewModel.showBottomNavBar.value = true
                            imgBitmap = null
                        },
                        colors = ButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.6f),
                            contentColor = Color.White,
                            disabledContentColor = Color.Gray,
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text("Отменить", fontSize = 20.sp)
                    }

                    Button(
                        modifier = Modifier,
                        onClick = {
                            var croppedBitmap: Bitmap? = cropImageView.getCroppedImage()
                            croppedBitmap = if (croppedBitmap!!.config == Bitmap.Config.HARDWARE) {
                                croppedBitmap.copy(Bitmap.Config.ARGB_8888, false)
                            } else {
                                croppedBitmap
                            }

                            val bmp = Mat(croppedBitmap.height, croppedBitmap.width, CvType.CV_8UC4)
                            Utils.bitmapToMat(croppedBitmap, bmp)
                            viewModel.setMat(bmp)
                        },
                        colors = ButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            disabledContentColor = Color.Gray,
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text("Обрезать", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

fun averageColor(mBitmap: Bitmap): Int {
    val bitmap = if(mBitmap.config == Bitmap.Config.HARDWARE){
        mBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: mBitmap
    }
    else mBitmap

    var r = 0L
    var g = 0L
    var b = 0L
    var count = 0

    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    for (pixel in pixels) {
        if (pixel != android.graphics.Color.TRANSPARENT) {
            r += android.graphics.Color.red(pixel)
            g += android.graphics.Color.green(pixel)
            b += android.graphics.Color.blue(pixel)
            count++
        }
    }

    return if (count > 0) {
        android.graphics.Color.rgb(
            (r / count * 0.5f).toInt(),
            (g / count * 0.5f).toInt(),
            (b / count * 0.5f).toInt()
        )
    } else {
        android.graphics.Color.GRAY
    }
}

private data class CropRectProperties(
    val rectWidth: Dp = 0.dp,
    val rectHeight: Dp = 0.dp,
    val offsetX: Dp = 0.dp,
    val offsetY: Dp = 0.dp
)

enum class CropRectMode {
    FREE,
    MIRROR
}

@Composable
fun CropRect(
    modifier: Modifier = Modifier,
    min: Offset = Offset(0.3f, 0.3f),
    max: Offset = Offset(0.9f, 1f),
    mode: CropRectMode = CropRectMode.FREE,
    uri: Uri? = null
){
    var imgOffsetX by remember { mutableStateOf(0f) }
    var imgOffsetY by remember { mutableStateOf(0f) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectDragGestures { change, dragAmount ->

                }
            },
        contentAlignment = if(mode == CropRectMode.MIRROR) Alignment.Center else Alignment.TopStart
    ) {
        val parentWidth = maxWidth
        val parentHeight = maxHeight
        val padding = DpOffset((parentWidth * (1 - max.x) / 2), (parentHeight * (1 - max.y) / 2))

        var imgSize by remember { mutableStateOf(IntSize(0, 0)) }
        val density = LocalDensity.current

        var rectWidth by remember(imgSize) {
            mutableStateOf(
                with(density) {
                    if (uri != null) (imgSize.width.toDp() * max.x).coerceAtMost(parentWidth * max.x)
                    else parentWidth * max.x
                }
            )
        }
        var rectHeight by remember(imgSize) {
            mutableStateOf(
                with(density) {
                    if(uri != null) (imgSize.height.toDp() * max.y).coerceAtMost(parentHeight * max.y)
                    else parentHeight * max.y
                }
            )
        }
        println("${rectWidth} ${rectHeight}")
        var offsetX by remember { mutableStateOf(0.dp) }
        var offsetY by remember { mutableStateOf(0.dp) }

        var firstDrag by remember { mutableStateOf(Offset.Zero) }
        var previous = CropRectProperties()

        if(uri != null){
            AsyncImage(
                model = uri,
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(imgOffsetX.roundToInt(), imgOffsetY.roundToInt()) }
                    .onGloballyPositioned { coordinates ->
                        imgSize = coordinates.size
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                imgOffsetX = (imgOffsetX + dragAmount.x)
                                    .coerceAtMost((offsetX + padding.x).toPx())
                                    .coerceAtLeast((offsetX - (imgSize.width.toDp() - rectWidth) + padding.x).toPx())
                                imgOffsetY = (imgOffsetY + dragAmount.y)
                                    .coerceAtMost((offsetY + padding.y).toPx())
                                    .coerceAtLeast((offsetY - (imgSize.height.toDp() - rectHeight) + padding.y).toPx())
                            }
                        )
                    }
            )
        }

        val pointerInputMode = when(mode){
            CropRectMode.FREE -> Modifier
                .pointerInput(Unit){
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            if(firstDrag.x.toDp() in 0.dp..<previous.rectWidth / 2){
                                val newOffsetX = (offsetX + dragAmount.x.toDp())
                                    .coerceAtLeast((imgOffsetX.toDp() - padding.x).coerceAtLeast(0.dp))
                                    .coerceAtMost(offsetX + rectWidth - parentWidth * min.x)
                                val diff = newOffsetX - offsetX

                                offsetX = newOffsetX
                                rectWidth -= diff

                            }
                            else{
                                val newWidth = (rectWidth + dragAmount.x.toDp())
                                    .coerceAtLeast(parentWidth * min.x)
                                    .coerceAtMost(
                                        (parentWidth * max.x - offsetX + imgOffsetX.toDp() + padding.x)
                                            .coerceAtMost(parentWidth * max.x - offsetX)
                                    )

                                rectWidth = newWidth
                            }

                            if(firstDrag.y.toDp() in 0.dp..<previous.rectHeight / 2){
                                val newOffsetY = (offsetY + dragAmount.y.toDp())
                                    .coerceAtLeast((imgOffsetY.toDp() - padding.y).coerceAtLeast(0.dp))
                                    .coerceAtMost(offsetY + rectHeight - parentHeight * min.y)

                                val diff = newOffsetY - offsetY

                                offsetY = newOffsetY
                                rectHeight -= diff
                            }
                            else{
                                val newHeight = (rectHeight + dragAmount.y.toDp())
                                    .coerceAtLeast(parentHeight * min.y)
                                    .coerceAtMost(
                                        (parentHeight * max.y - offsetY + imgOffsetY.toDp() + padding.y)
                                            .coerceAtMost(parentHeight * max.y - offsetY)
                                    )

                                rectHeight = newHeight
                            }
                        }
                    )
                }

            CropRectMode.MIRROR -> Modifier
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            var vectorX = dragAmount.x.toDp()
                            var vectorY = dragAmount.y.toDp()

                            if (firstDrag.x in 0f..<(previous.rectWidth.toPx() / 2f)) {
                                vectorX = -vectorX
                            }
                            if (firstDrag.y in 0f..<(previous.rectHeight.toPx() / 2f)) {
                                vectorY = -vectorY
                            }

                            val res = DpOffset(rectWidth + vectorX, rectHeight + vectorY)

                            rectWidth = res.x.coerceIn(parentWidth * min.x, parentWidth * max.x)

                            rectHeight = res.y.coerceIn(parentHeight * min.y, parentHeight * max.y)
                        }
                    )
                }

        }

        Box(
            modifier = modifier
                then(
                    if(mode == CropRectMode.FREE){
//                        if(uri == null) Modifier.offset(parentWidth * (1 - max.x) / 2, parentHeight * (1 - max.y) / 2)
//                        else Modifier.offset(
//                            parentWidth * (1 - max.x) / 2,
//                            with(density){(imgSize.height * (1 - max.y) / 2).toDp()}
//                        )
                        Modifier.offset(parentWidth * (1 - max.x) / 2, parentHeight * (1 - max.y) / 2)
                    }
                    else Modifier
                )
                .offset(offsetX, offsetY)
                .size(rectWidth, rectHeight)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val event = awaitFirstDown(pass = PointerEventPass.Initial)

                        firstDrag = event.position
                        previous = CropRectProperties(
                            rectWidth = rectWidth,
                            rectHeight = rectHeight,
                            offsetX = offsetX,
                            offsetY = offsetY
                        )
                    }
                }
                .then(pointerInputMode)
                .border(
                    width = 1.dp,
                    color = Color.Red
                )
        ){
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ){
                drawLine(
                    color = Color.Red,
                    start = Offset((rectWidth / 2 - parentWidth * 0.05f).toPx(), (rectHeight / 2).toPx()),
                    end = Offset((rectWidth / 2 + parentWidth * 0.05f).toPx(), (rectHeight / 2).toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.Red,
                    start = Offset((rectWidth / 2).toPx(), (rectHeight / 2 - parentWidth * 0.05f).toPx()),
                    end = Offset((rectWidth / 2).toPx(), (rectHeight / 2 + parentWidth * 0.05f).toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }

}

@Composable
fun CaptureButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isPressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    BoxWithConstraints(
        modifier = modifier
    ) {
        val buttonWidth = maxWidth
        val buttonHeight = maxHeight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = Color.Gray,
                    shape = CircleShape
                )
                .background(Color.Transparent)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(buttonWidth * 0.85f, buttonHeight * 0.85f)
                    .scale(scale)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed.value = true
                                tryAwaitRelease()
                                isPressed.value = false
                            },
                            onTap = {
                                onClick()
                            }
                        )
                    }
            ) {}
        }
    }
}