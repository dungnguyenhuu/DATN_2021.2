package com.base.common.base.application

import android.app.Application
import com.base.common.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

open class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
//            androidLogger(Level.DEBUG)
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@BaseApplication)
        }
    }
}