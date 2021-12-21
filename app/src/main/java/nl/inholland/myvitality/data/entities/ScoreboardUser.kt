package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class ScoreboardUser (
    @Json(name = "userId") val userId: String,
    @Json(name = "fullName") var fullName : String,
    @Json(name = "profilePicture") var profileImage : String?,
    @Json(name = "points") var points: Int,
)