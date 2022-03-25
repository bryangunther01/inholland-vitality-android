package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

enum class ActivityType(val id: Int) {
    @Json(name = "1") EXERCISE(1),
    @Json(name = "2") DIET(2),
    @Json(name = "3") MIND(3)
}