package com.grandfatherpikhto.lessonble04.models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grandfatherpikhto.lessonble04.LessonBle04App
import java.lang.Exception

class BleScanViewModelProviderFactory constructor(private val application: Application): ViewModelProvider.Factory {
    private val logTag = this.javaClass.simpleName
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        (application.applicationContext as LessonBle04App).bleManager?.let {
            return ScanViewModel(it) as T
        }
        throw Exception("Не создан объект BleScanManager")
    }
}