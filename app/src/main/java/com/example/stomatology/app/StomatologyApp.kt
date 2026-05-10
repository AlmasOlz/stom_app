package com.example.stomatology.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StomatologyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("kk"))
    }
}