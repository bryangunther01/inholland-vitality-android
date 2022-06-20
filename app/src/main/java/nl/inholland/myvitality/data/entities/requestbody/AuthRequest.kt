package nl.inholland.myvitality.data.entities.requestbody

import com.squareup.moshi.Json


data class AuthRequest (
    @Json(name = "Email") val emailAddress : String,
    @Json(name = "AzureId") val azureId : String,
    @Json(name = "AccessToken") val accessToken : String
)