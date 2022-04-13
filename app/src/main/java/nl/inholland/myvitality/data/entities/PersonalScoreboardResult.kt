package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class PersonalScoreboardResult (
    @Json(name = "activityId") val activityId: String,
    @Json(name = "imageLink") val imageLink: String? = null,
    @Json(name = "title") var title : String,
    @Json(name = "points") var points: Int,
)