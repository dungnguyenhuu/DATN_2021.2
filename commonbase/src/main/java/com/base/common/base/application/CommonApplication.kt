package com.base.common.base.application

import android.app.Application
//import com.facebook.stetho.Stetho

abstract class CommonApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        app = this
//        Stetho.initializeWithDefaults(this)
    }

    companion object{
        private var app: CommonApplication? = null
        val instance: Application?
            get() = app
    }
}