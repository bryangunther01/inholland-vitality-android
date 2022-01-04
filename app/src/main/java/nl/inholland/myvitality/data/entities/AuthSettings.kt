package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class AuthSettings (
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "tokenType") val tokenType: String,
    @Json(name = "expiresIn") val expiresIn: Long,
    @Json(name = "userType") val userType: String
)