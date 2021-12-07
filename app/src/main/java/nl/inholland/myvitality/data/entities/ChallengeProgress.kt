package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

enum class ChallengeProgress(val id: Int) {
    @Json(name = "0") NOT_SUBSCRIBED(0),
    @Json(name = "1") IN_PROGRESS(1),
    @Json(name = "2") DONE(2),
    @Json(name = "3") CANCELLED(3)
}