package com.grandfatherpikhto.lessonble04.models

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grandfatherpikhto.lessonble04.LessonBle04App
import java.lang.Exception

@Suppress("UNCHECKED_CAST")
class BleScanViewModelProviderFactory constructor(private val application: Application): ViewModelProvider.Factory {
    @NonNull
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        (application.applicationContext as LessonBle04App).bleManager?.let {
            return ScanViewModel(it) as T
        }
        throw Exception("Не создан объект BleScanManager")
    }
}