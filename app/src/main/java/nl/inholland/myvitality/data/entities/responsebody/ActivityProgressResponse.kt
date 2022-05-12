package nl.inholland.myvitality.data.entities.responsebody

import com.squareup.moshi.Json
import nl.inholland.myvitality.data.entities.Achievement
import nl.inholland.myvitality.data.entities.ActivityProgress

data class ActivityProgressResponse (
    @Json(name = "activityId") val activityId : String,
    @Json(name = "activityProgress") val activityProgress : ActivityProgress,
    @Json(name = "achievements") val achievements : List<Achievement>?
)