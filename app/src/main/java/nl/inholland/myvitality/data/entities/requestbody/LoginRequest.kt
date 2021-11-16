package nl.inholland.myvitality.data.entities.requestbody

import com.squareup.moshi.Json


data class LoginRequest (
    @Json(name = "UserEmail") val emailAddress : String,
    @Json(name = "Password") val password : String
)