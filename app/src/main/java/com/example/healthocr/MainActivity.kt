package com.example.healthocr

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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.healthocr.nav.AppNavigation
import com.example.healthocr.nav.BottomNavigationBar
import com.example.healthocr.nav.NavRoutes
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var viewModel: AppViewModel

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
            val owner = LocalViewModelStoreOwner.current

            owner?.let {
                viewModel = viewModel()
                MainComponent(viewModel)
            }
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
    val startDestination = NavRoutes.Camera.route
    val selectedDestination = remember { mutableStateOf(startDestination) }

    Scaffold(
        bottomBar = {
            if(viewModel.showBottomNavBar.value){
                BottomNavigationBar(navController, selectedDestination)
            }
        }
    ) { paddingValues ->
        AppNavigation(
            modifier = Modifier,
            navController = navController,
            startDestination = startDestination,
            viewModel = viewModel,
            scaffoldPaddingValues = paddingValues
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