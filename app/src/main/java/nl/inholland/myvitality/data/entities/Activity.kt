package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json
import nl.inholland.myvitality.util.DateUtils

data class Activity (
    @Json(name = "activityId") val activityId: String,
    @Json(name = "category") val category: ActivityCategory,
    @Json(name = "title") var title : String,
    @Json(name = "activityType") var activityType : ActivityType,
    @Json(name = "description") var description : String,
    @Json(name = "imageLink") var imageLink : String?,
    @Json(name = "videoLink") var videoLink : String?,
    @Json(name = "startDate") var startDate : String,
    @Json(name = "endDate") var endDate : String,
    @Json(name = "url") var url : String?,
    @Json(name = "location") var location : String,
    @Json(name = "points") var points : Int,
    @Json(name = "activityProgress") var activityProgress : ActivityProgress?,
    @Json(name = "totalSubscribers") var totalSubscribers : Int?,
    val hasStarted: Boolean = DateUtils.isInPast(startDate),
    val hasEnded: Boolean = DateUtils.isInPast(endDate),
    val signUpOpen: Boolean = DateUtils.isWithinAWeek(startDate)
)