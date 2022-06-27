package vn.app.qrcode.di.module

import org.koin.dsl.module
import vn.app.qrcode.data.remote.usecase.*

val useCaseModule = module {
    factory { CheckVersionUseCase(get(), get()) }

}