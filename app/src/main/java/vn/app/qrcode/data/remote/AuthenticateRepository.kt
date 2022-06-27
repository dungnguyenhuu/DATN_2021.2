package vn.app.qrcode.data.remote

import io.reactivex.Single
import vn.app.qrcode.data.model.CheckVersionResponse
import vn.app.qrcode.data.request.CheckVersionRequest

interface AuthenticateRepository {

    fun checkVersion(versionRequest: CheckVersionRequest): Single<CheckVersionResponse>
}

class AuthenticateRepositoryImpl(private val authenticateSource: AuthenticateSource) :
    AuthenticateRepository {

    override fun checkVersion(versionRequest: CheckVersionRequest): Single<CheckVersionResponse> {
        return authenticateSource.checkVersion(versionRequest.version, versionRequest.type)
    }
}