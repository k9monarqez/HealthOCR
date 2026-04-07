package com.example.swagaapp

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.swagaapp.nav.AppNavigation
import com.example.swagaapp.nav.NavRoutes
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (OpenCVLoader.initLocal()) {
            Log.i("LOADED", "OpenCV loaded successfully");
        } else {
            Log.e("LOADED", "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        copyTrainedData(applicationContext)
        setContent {
            MainComponent(viewModel)
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cameraExecutor.shutdown()
    }
}

@Composable
fun MainComponent(viewModel: AppViewModel){
    val navController = rememberNavController()
    val startDestination = NavRoutes.Home.route
    val selectedDestination = remember { mutableStateOf(startDestination) }

    Surface(
        //bottomBar = { BottomNavigationBar(navController, selectedDestination) }
    ) {
        AppNavigation(
            modifier = Modifier,
            navController = navController,
            viewModel = viewModel,
        )
    }
}

fun copyTrainedData(context: Context) {
    val tessDataDir = File(context.filesDir, "tesseract/tessdata")
    if (!tessDataDir.exists()) {
        tessDataDir.mkdirs()
    }

    val assetManager = context.assets
    try {
        val fileList = assetManager.list("tessdata") ?: return
        fileList.forEach { fileName ->
            val outFile = File(tessDataDir, fileName)
            if (!outFile.exists()) {
                assetManager.open("tessdata/$fileName").use { inputStream ->
                    FileOutputStream(outFile).use { outputStream ->
                        val buffer = ByteArray(1024)
                        var read: Int
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                        }
                    }
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}