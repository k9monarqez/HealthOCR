package com.example.swagaapp

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppViewModel: ViewModel() {
    var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}