package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class Challenge (
    @Json(name = "challengeId") val challengeId: String,
    @Json(name = "title") var title : String,
    @Json(name = "challengeType") var challengeType : ChallengeType,
    @Json(name = "description") var description : String,
    @Json(name = "imageLink") var imageLink : String?,
    @Json(name = "videoLink") var videoLink : String?,
    @Json(name = "startDate") var startDate : String,
    @Json(name = "endDate") var endDate : String,
    @Json(name = "location") var location : String,
    @Json(name = "points") var points : Int,
    @Json(name = "challengeProgress") var challengeProgress : ChallengeProgress?,
    @Json(name = "totalSubscribers") var totalSubscribers : Int?,
)