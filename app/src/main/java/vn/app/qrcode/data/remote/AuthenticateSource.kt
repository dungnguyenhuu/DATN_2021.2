package vn.app.qrcode.data.remote

import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.http.Query
import vn.app.qrcode.data.model.CheckVersionResponse

interface AuthenticateSource {
    @POST("/api/check-version-new")
    fun checkVersion(@Query("version") version: String, @Query("type") type: String): Single<CheckVersionResponse>
}