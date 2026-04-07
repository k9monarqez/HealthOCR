package com.example.swagaapp.ocr

import android.graphics.ImageFormat
import android.media.Image
import android.view.Surface
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Offset
import org.opencv.core.Core
import org.opencv.core.Core.flip
import org.opencv.core.Core.rotate
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer


@OptIn(ExperimentalGetImage::class)
fun cropImageProxy(imageProxy: ImageProxy, cropRect: org.opencv.core.Rect, previewSize: Offset): Mat{
    try {
        imageProxy.image?.let {
            if (it.format == ImageFormat.JPEG) {
                val rgbaMat = it.jpegToRgba()
                val coefficient = rgbaMat.height() / previewSize.y

                val roi = org.opencv.core.Rect(
                    Point(
                        ((rgbaMat.width() - cropRect.width * coefficient) / 2f).toDouble(),
                        cropRect.y.toDouble() * coefficient
                    ),
                    Point(
                        ((rgbaMat.width() + cropRect.width * coefficient) / 2f).toDouble(),
                        (cropRect.y + cropRect.height).toDouble() * coefficient
                    )
                )

                val croppedImage = rgbaMat.submat(roi)

                return croppedImage
            }
            else{
                println(it.format)
            }
        }
    } catch (ise: IllegalStateException) {
        ise.printStackTrace()
    }

    return Mat()
}

fun Image.jpegToRgba(): Mat {
    val buffer: ByteBuffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val jpegData = MatOfByte(*bytes)

    val mat = Imgcodecs.imdecode(jpegData, IMREAD_COLOR)
    val rgbaMat = Mat()
    Imgproc.cvtColor(mat, rgbaMat, Imgproc.COLOR_BGR2RGBA)
    mat.release()

    return rgbaMat
}