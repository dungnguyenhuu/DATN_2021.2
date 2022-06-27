package vn.app.qrcode.di.module


import com.google.gson.*
import org.koin.dsl.module
import vn.app.qrcode.data.usermanager.UserManager
import java.util.*

val appModule = module {

    single { provideGson() }

    single { UserManager() }

}

fun provideGson(): Gson {
    val builder = GsonBuilder()

    builder.registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ ->
        json?.asJsonPrimitive?.asLong?.let {
            return@JsonDeserializer Date(it)
        }
    })

    builder.registerTypeAdapter(Date::class.java, JsonSerializer<Date> { date, _, _ ->
        JsonPrimitive(date.time)
    })

    return builder.create()
}