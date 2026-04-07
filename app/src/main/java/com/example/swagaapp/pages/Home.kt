package com.example.swagaapp.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.googlecode.leptonica.android.WriteFile
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File

@Composable
fun Home(

){
    val context = LocalContext.current
    val tess = TessBaseAPI()
    val dataPath = File(context.filesDir, "tesseract").absolutePath
    var extractedText by remember {mutableStateOf("")}
    var bmp by remember { mutableStateOf<Bitmap?>(null) }

    if (!tess.init(dataPath, "eng+ssd")) {
        tess.recycle();
        return;
    }


    val image = File(context.filesDir, "tesseract/tessdata/ton.jpg")
    val bitmap = BitmapFactory.decodeFile(image.absolutePath)

    //tess.setVariable("tessedit_char_whitelist", "0123456789.")

    tess.setImage(bitmap);
    extractedText = tess.utF8Text
    Column() {
        Image(WriteFile.writeBitmap(tess.thresholdedImage).asImageBitmap(), contentDescription = "")
        Spacer(modifier = Modifier.padding(30.dp))
        Text(extractedText)
    }
    tess.recycle()

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}