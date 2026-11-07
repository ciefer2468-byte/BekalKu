package com.unimal.uas_240170129_muhammad_raihann_azhari

import android.app.Application
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.ThemeManager

class BekalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.applySaved(this)
    }
}
