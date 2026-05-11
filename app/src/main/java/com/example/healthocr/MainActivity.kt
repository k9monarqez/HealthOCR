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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.healthocr.nav.AppNavigation
import com.example.healthocr.nav.BottomNavigationBar
import com.example.healthocr.nav.NavRoutes
import com.example.healthocr.pages.sessionHistory.AcceptDeletionWindow
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

    DisposableEffect(Unit) {
        onDispose {
            viewModel.hideDarkBG()
        }
    }

    Scaffold(
        bottomBar = {
            if(viewModel.showBottomNavBar.value){
                BottomNavigationBar(navController, selectedDestination)
            }
        },
    ) { paddingValues ->
        AppNavigation(
            modifier = Modifier,
            navController = navController,
            startDestination = startDestination,
            viewModel = viewModel,
            scaffoldPaddingValues = paddingValues
        )
    }

    val darkBG by viewModel.darkBackground.collectAsState()
    if(darkBG){
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ){
            val bgWidth = maxWidth
            val bgHeight = maxHeight
            AcceptDeletionWindow(viewModel, Modifier.height(bgHeight * 0.3f).padding(24.dp))
        }
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