package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class Interest (
    @Json(name = "interestId") val interestId: String,
    @Json(name = "name") val name: String,
)