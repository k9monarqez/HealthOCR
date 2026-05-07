package com.example.healthocr.ocr

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.ui.geometry.Offset
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.core.CvException
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer

object DeviceImageProcessing {
    fun getDisplayArea(mat: Mat, kernelSize: Size, blockSize: Int, C: Double, minDisplaySize: Double): Mat{
        val mainMat = mat.clone()
        Imgproc.cvtColor(mainMat, mainMat, Imgproc.COLOR_RGBA2GRAY)

        val thresh = Mat(mainMat.height(), mainMat.width(), mainMat.type())
        Imgproc.adaptiveThreshold(mainMat, thresh, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY, blockSize, C)

        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize)
        Imgproc.morphologyEx(thresh, thresh, Imgproc.MORPH_CLOSE, kernel)
        Imgproc.morphologyEx(thresh, thresh, Imgproc.MORPH_OPEN, kernel)

        val matArea = thresh.height() * thresh.width()
        val boxes = findContoursRectangles(thresh, matArea * minDisplaySize..matArea * 1.0, Mat())

        val displayBox = boxes.minByOrNull { box ->
            Imgproc.contourArea(box)
        }

        val dm = if(displayBox != null){
            try{
                mat.submat(
                    Imgproc.boundingRect(
                        displayBox
                    )
                )
            } catch(e: CvException){
                mat
            }
        }
        else mat

        return dm
    }

    fun getSevenSegmentDigitsMat(mat: Mat, kernelSize: Size, blockSize: Int, C: Double, digitsSizeRange: ClosedRange<Double>): Mat{
        val displayMat = mat.clone()
        Imgproc.cvtColor(displayMat, displayMat, Imgproc.COLOR_RGBA2GRAY)
        val kernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            kernelSize
        )

        Imgproc.adaptiveThreshold(displayMat, displayMat, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY, blockSize, C)
        Imgproc.dilate(displayMat, displayMat, kernel)
        Imgproc.erode(displayMat, displayMat, kernel)
        Imgproc.erode(displayMat, displayMat, kernel)

        val matArea = displayMat.height() * displayMat.width()
        println(displayMat.size())
        val boxes = findContoursRectangles(displayMat, matArea * digitsSizeRange.start ..matArea * digitsSizeRange.endInclusive, Mat())
        val digitsMat = Mat(displayMat.height(), displayMat.width(), displayMat.type()).setTo(Scalar(255.0))

        for(box in boxes){
            val roi = Imgproc.boundingRect(box)
            try{
                displayMat.submat(roi).copyTo(digitsMat.submat(roi))
            } catch(e: Exception){}
        }

        return digitsMat
    }

    fun erodeDigits(mat: Mat, kernelSize: Size): Mat{
        val displayMat = mat.clone()
        if(mat.channels() != 1) Imgproc.cvtColor(displayMat, displayMat, Imgproc.COLOR_RGBA2GRAY)
        val kernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            kernelSize
        )

        Imgproc.erode(displayMat, displayMat, kernel)
        return displayMat
    }

    fun extractText(bitmap: Bitmap, dataPath: String): String{
        val tess = TessBaseAPI()
        if (tess.init(dataPath, "ssd")) {
            val targetWords = listOf("0123456789")
            tess.setVariable(
                "tessedit_char_whitelist",
                targetWords.joinToString() + targetWords.joinToString().lowercase()
            )
            tess.setImage(bitmap)
            var blocks = mutableListOf<Pair<String, Rect>>()

            val utF8Text = tess.utF8Text

            val iterator = tess.resultIterator
            val level = TessBaseAPI.PageIteratorLevel.RIL_WORD

            return utF8Text
        }
        return ""
    }

    private fun findContoursRectangles(mat: Mat, sizeRange: ClosedRange<Double>, hierarchy: Mat = Mat()): List<MatOfPoint>{
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        val boxes = mutableListOf<MatOfPoint>()
        for(cnt in contours){
            val area = Imgproc.contourArea(cnt)
            if(area !in sizeRange) continue

            val rect = Imgproc.minAreaRect(MatOfPoint2f(*cnt.toArray()))
            val box = Array(4) { Point() }
            rect.points(box)
            val boxMat = MatOfPoint(*box)

            boxes.add(boxMat)
        }

        return boxes
    }

    @OptIn(ExperimentalGetImage::class)
    fun cropImage(image: Image?, cropRect: org.opencv.core.Rect, previewSize: Offset): Mat{
        try {
            image?.let {
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

    private fun Image.jpegToRgba(): Mat {
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
}