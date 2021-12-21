package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

enum class NotificationType(val id: Int) {
    @Json(name = "1") LIKE(1),
    @Json(name = "2") COMMENT(2),
    @Json(name = "3") FOLLOW(3),
    @Json(name = "4") OTHER(4)
}