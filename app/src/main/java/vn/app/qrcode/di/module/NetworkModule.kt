package vn.app.qrcode.di.module

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.annotation.FloatRange
import com.base.common.BuildConfig
import com.base.common.utils.rx.ApplicationSchedulerProvider
import com.base.common.utils.rx.SchedulerProvider
import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import vn.app.qrcode.data.network.TokenInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { buildOkHttpClient(androidContext()) }

    single { provideRetrofit(get(), get()) }

    single<SchedulerProvider> { ApplicationSchedulerProvider() }
}


fun provideRetrofit(
    okHttpClient: OkHttpClient,
    gson: Gson
): Retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create(gson))
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .build()

fun buildOkHttpClient(app: Context): OkHttpClient {
    val httpLoggingBodyInterceptor = HttpLoggingInterceptor()
    httpLoggingBodyInterceptor.level = HttpLoggingInterceptor.Level.BODY
    return OkHttpClient.Builder()
        .connectTimeout(60L, TimeUnit.SECONDS)
        .writeTimeout(60L, TimeUnit.SECONDS)
        .readTimeout(60L, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .followSslRedirects(true)
        .cache(
            Cache(
                File(app.cacheDir, "OkCache"),
                calcCacheSize(app, .25f)
            )
        ).apply {
            interceptors().add(TokenInterceptor())
            interceptors().add(httpLoggingBodyInterceptor)
//            interceptors().add(StethoInterceptor())
        }
        .build()

}

private fun calcCacheSize(context: Context, @FloatRange(from = 0.01, to = 1.0) size: Float): Long {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val largeHeap = context.applicationInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP != 0
    val memoryClass = if (largeHeap) am.largeMemoryClass else am.memoryClass
    return (memoryClass * 1024L * 1024L * size).toLong()
}
