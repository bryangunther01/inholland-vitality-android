package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json
import nl.inholland.myvitality.util.DateUtils

data class Interest (
    @Json(name = "interestId") val interestId: String,
    @Json(name = "name") val name: String,
)