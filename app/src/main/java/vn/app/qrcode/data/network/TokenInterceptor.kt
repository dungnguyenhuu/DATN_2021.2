package vn.app.qrcode.data.network

import com.base.common.BuildConfig
import com.base.common.constant.AppConstant
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vn.app.qrcode.AppApplication
import vn.app.qrcode.data.usermanager.UserManager
import java.io.IOException

class TokenInterceptor : Interceptor, KoinComponent {
    private val userManager: UserManager by inject()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var currentUrl = chain.request().url.toString()
        if(!AppApplication.getBaseUrl().isBlank()){
            currentUrl = currentUrl.replace(BuildConfig.BASE_URL,AppApplication.getBaseUrl())
        }
        val request = chain.request().newBuilder()
            .url(currentUrl)
            .addHeader(AppConstant.HEADER_ACCEPT, AppConstant.HEADER_ACCEPT_VALUE)
            .addHeader(AppConstant.HEADER_CONTENT_TYPE, AppConstant.HEADER_ACCEPT_VALUE)
            .addHeader(AppConstant.HEADER_ACCEPT_LANGUAGE, "vi")
            .addHeader(AppConstant.HEADER_AUTHORIZATION, userManager.getUserToken()!!).build()

        return chain.proceed(request)
    }

}