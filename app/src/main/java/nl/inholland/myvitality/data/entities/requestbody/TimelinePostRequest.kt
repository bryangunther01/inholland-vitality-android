package nl.inholland.myvitality.data.entities.requestbody

import com.squareup.moshi.Json


data class TimelinePostRequest (
    @Json(name = "text") val text : String,
)