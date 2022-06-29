package vn.app.qrcode.di.module

import android.content.ComponentName
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.media.MusicService
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val musicConnectionModule = module {
    single {
        MusicServiceConnection.getInstance(
            androidApplication(),
            ComponentName(androidApplication(), MusicService::class.java)
        )
    }
}
