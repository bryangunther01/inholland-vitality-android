package nl.inholland.myvitality.data.entities.requestbody

import com.squareup.moshi.Json

data class RegisterRequest (
    @Json(name = "Email") val emailAddress : String,
    @Json(name = "AzureADToken") val azureToken : String,
    @Json(name = "Firstname") val firstname : String,
    @Json(name = "Lastname") val lastname : String
)