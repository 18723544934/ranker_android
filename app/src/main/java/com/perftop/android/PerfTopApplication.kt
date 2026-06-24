package com.perftop.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PerfTopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
