package vn.app.qrcode

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.base.common.base.application.CommonApplication
import org.koin.core.context.loadKoinModules
import timber.log.Timber
import vn.app.qrcode.di.module.*

class AppApplication : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        app = this
        loadKoinModules(appModule)
        Timber.plant(Timber.DebugTree())
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        private var app: AppApplication? = null
        private var baseUrl: String = ""

        val instance: Application?
            get() = app

        fun getBaseUrl() = baseUrl
        fun setBaseUrl(url: String) {
            baseUrl = url
        }
    }
}