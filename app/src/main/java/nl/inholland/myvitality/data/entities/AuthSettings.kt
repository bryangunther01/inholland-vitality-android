package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class AuthSettings (
    @Json(name = "AccessToken") val accessToken: String,
    @Json(name = "TokenType") val tokenType: String,
    @Json(name = "ExpiresIn") val expiresIn: Long,
    @Json(name = "UserType") val userType: String
)