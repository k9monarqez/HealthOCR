package com.example.swagaapp.pages

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
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
import com.example.swagaapp.AppViewModel
import com.example.swagaapp.R
import com.example.swagaapp.ocr.cropImageProxy
import com.googlecode.leptonica.android.WriteFile
import com.googlecode.tesseract.android.TessBaseAPI
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.CropShape
import com.tanishranjan.cropkit.ImageCropper
import com.tanishranjan.cropkit.rememberCropController
import org.opencv.android.Utils
import org.opencv.core.Point
import kotlin.math.roundToInt

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Camera(
    viewModel: AppViewModel,
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

    var extractedText by remember { mutableStateOf("") }
    var bmp by remember { mutableStateOf<Bitmap?>(null) }

    var cropRectPosition = org.opencv.core.Rect()
    val cropRectTopLeft = remember { mutableStateOf(Point(0.0, 0.0)) }
    val cropRectSize = remember { mutableStateOf(Offset.Zero) }

    var imgBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
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

    LaunchedEffect(Unit) {
        previewView.controller = cameraController
    }

    fun capturePhoto() {
        cameraController.takePicture(
            viewModel.cameraExecutor,
            object: ImageCapture.OnImageCapturedCallback() {
                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(image: ImageProxy) {
                    val mat = cropImageProxy(
                        image,
                        cropRectPosition,
                        Offset(previewView.width.toFloat(), previewView.height.toFloat())
                    )

                    bmp = createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mat, bmp)

//                    val tess = TessBaseAPI()
//                    val dataPath = File(context.filesDir, "tesseract").absolutePath
//                    if (!tess.init(dataPath, "eng")) {
//                        tess.recycle();
//                        return;
//                    }
//
//
//
//                    tess.setVariable("tessedit_char_whitelist", "0123456789.")
//                    tess.setImage(bmp);
//                    bmp = WriteFile.writeBitmap(tess.thresholdedImage)
//                    extractedText = tess.utF8Text
//                    tess.recycle()
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

        if(imgBitmap == null){
            if(bmp == null) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxSize()
                )

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
                                Button(
                                    onClick = {}
                                ) {
                                    Text("Device")
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
                                                cropRectPosition = org.opencv.core.Rect(
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
            val backgroundColor = averageColor(imgBitmap!!)
            val cropController = rememberCropController(
                bitmap = imgBitmap!!,
                cropOptions = CropDefaults.cropOptions(
                    cropShape = CropShape.FreeForm
                ),
                cropColors = CropDefaults.cropColors(
//                    cropRectangle = invertedBGColor.copy(0.5f),
                    gridlines = Color(backgroundColor).copy(0.5f),
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(backgroundColor))
                    .displayCutoutPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                ImageCropper(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    cropController = cropController
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
                            val croppedBitmap = cropController.crop()
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

@SuppressLint("UnusedBoxWithConstraintsScope")
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

        BoxWithConstraints(
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

@SuppressLint("UnusedBoxWithConstraintsScope")
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

//
//@Composable
//fun CropRectOld(
//    modifier: Modifier = Modifier,
//    contentAlignment: Alignment = Alignment.TopStart,
//    min: Offset = Offset(1f, 1f),
//    max: Offset = Offset(1f, 1f),
//    size: MutableState<Offset> = mutableStateOf(Offset.Zero),
//    topLeft: MutableState<Point> = mutableStateOf(Point())
//){
//    BoxWithConstraints(
//        modifier = modifier
//            .fillMaxSize(),
//        contentAlignment = contentAlignment
//    ) {
//        val minRectWidth = maxWidth * min.x
//        val minRectHeight = maxHeight * min.y
//
//        val maxRectWidth = maxWidth * max.x
//        val maxRectHeight = maxHeight * max.y
//
//        var rectWidth by remember { mutableStateOf(maxRectWidth) }
//        var rectHeight by remember { mutableStateOf(maxRectHeight) }
//
//        var firstDrag by remember {mutableStateOf(Offset.Zero)}
//        var firstRectWidth by remember { mutableStateOf(0.dp) }
//        var firstRectHeight by remember { mutableStateOf(0.dp) }
//
//        size.value = Offset(
//            with(LocalDensity.current){ rectWidth.toPx() },
//            with(LocalDensity.current){ rectHeight.toPx() }
//        )
//
//        BoxWithConstraints(
//            modifier = Modifier
//                .size(rectWidth, rectHeight)
//                .onGloballyPositioned(
//                    { coordinates ->
//                        val coordinates = coordinates.positionInRoot()
//                        topLeft.value = Point(coordinates.x.toDouble(), coordinates.y.toDouble())
//                    }
//                )
//                .pointerInput(Unit) {
//                    detectDragGestures(
//                        onDragStart = { offset ->
//                            firstDrag = offset
//                            firstRectWidth = rectWidth
//                            firstRectHeight = rectHeight
//                            println("$firstDrag ${size.value.x / 2f} ${(size.value.y / 2f)}")
//                        },
//                        onDrag = { change, dragAmount ->
//                            var vectorX = dragAmount.x.toDp()
//                            var vectorY = dragAmount.y.toDp()
//
//                            if (firstDrag.x in 0f..<(firstRectWidth.toPx() / 2f)) {
//                                vectorX = -vectorX
//                            }
//                            if (firstDrag.y in 0f..<(firstRectHeight.toPx() / 2f)) {
//                                vectorY = -vectorY
//                            }
//
//                            val res = DpOffset(rectWidth + vectorX, rectHeight + vectorY)
//
//                            rectWidth =
//                                if (res.x < minRectWidth) minRectWidth
//                                else if (res.x > maxRectWidth) maxRectWidth
//                                else res.x
//
//                            rectHeight =
//                                if (res.y < minRectHeight) minRectHeight
//                                else if (res.y > maxRectHeight) maxRectHeight
//                                else res.y
//                        }
//                    )
//                }
//                .border(
//                    width = 1.dp,
//                    color = Color.Red
//                )
//        ){
//            Canvas(
//                modifier = Modifier
//            ){
//                drawLine(
//                    color = Color.Red,
//                    start = Offset((rectWidth / 2 - maxRectWidth * 0.05f).toPx(), (rectHeight / 2).toPx()),
//                    end = Offset((rectWidth / 2 + maxRectWidth * 0.05f).toPx(), (rectHeight / 2).toPx()),
//                    strokeWidth = 1.dp.toPx()
//                )
//                drawLine(
//                    color = Color.Red,
//                    start = Offset((rectWidth / 2).toPx(), (rectHeight / 2 - maxRectWidth * 0.05f).toPx()),
//                    end = Offset((rectWidth / 2).toPx(), (rectHeight / 2 + maxRectWidth * 0.05f).toPx()),
//                    strokeWidth = 1.dp.toPx()
//                )
//            }
//        }
//    }
//}