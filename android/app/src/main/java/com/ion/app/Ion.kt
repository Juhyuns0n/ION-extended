package com.ion.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.kakao.sdk.v2.auth.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class Ion : Application() {
    override fun onCreate() {
        super.onCreate()

        initTimber()
        setNightMode()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    private fun setNightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

}
