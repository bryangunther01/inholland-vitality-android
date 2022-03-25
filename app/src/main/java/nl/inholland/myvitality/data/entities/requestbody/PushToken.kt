package nl.inholland.myvitality.data.entities.requestbody

import com.squareup.moshi.Json

data class PushToken (
    @Json(name = "PushToken") val pushToken : String,
    @Json(name = "DeviceType") val deviceType : Int = 0
)