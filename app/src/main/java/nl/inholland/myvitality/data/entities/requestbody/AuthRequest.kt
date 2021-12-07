package nl.inholland.myvitality.data.entities.requestbody

import com.squareup.moshi.Json


data class AuthRequest (
    @Json(name = "Email") val emailAddress : String,
    @Json(name = "Password") val password : String
)