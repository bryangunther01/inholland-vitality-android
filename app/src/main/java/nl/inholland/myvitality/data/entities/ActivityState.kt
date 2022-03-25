package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

enum class ActivityState(val id: Int) {
    @Json(name = "0") USER(0),
    @Json(name = "1") AVAILABLE(1),
    @Json(name = "2") UPCOMING(2),
    @Json(name = "3") RECOMMENDED(3),
}