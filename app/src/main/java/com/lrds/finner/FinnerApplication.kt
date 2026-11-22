package com.lrds.finner

import android.app.Application
import com.lrds.finner.di.AppContainer

class FinnerApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
