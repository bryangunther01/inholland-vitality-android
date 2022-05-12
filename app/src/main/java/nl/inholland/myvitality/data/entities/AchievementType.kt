package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

enum class AchievementType(val id: Int) {
    @Json(name = "0") POINTS(0),
    @Json(name = "1") ACTIVITY(1),
}