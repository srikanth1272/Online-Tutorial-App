package com.multipurposeapp

import android.app.Application
import live.videosdk.rtc.android.VideoSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        VideoSDK.initialize(applicationContext)
    }
}