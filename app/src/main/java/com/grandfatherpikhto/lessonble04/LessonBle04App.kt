package com.grandfatherpikhto.lessonble04

import android.app.Application
import com.grandfatherpikhto.blin.BleManager
import com.grandfatherpikhto.blin.BleManagerInterface

class LessonBle04App : Application() {
    var bleManager: BleManagerInterface? = null
}