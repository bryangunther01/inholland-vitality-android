package nl.inholland.myvitality.data.entities

import com.squareup.moshi.Json

data class Notification (
    @Json(name = "notificationId") val notificationId: String,
    @Json(name = "toUser") var toUser : String?,
    @Json(name = "timelinePostId") var timelinePostId : String?,
    @Json(name = "challengeId") var challengeId : String?,
    @Json(name = "profileImage") var profileImage : String?,
    @Json(name = "fullName") var fullName : String?,
    @Json(name = "isFollowing") var isFollowing : Boolean?,
    @Json(name = "notificationType") var type : NotificationType,
    @Json(name = "timeOfNotification") var date : String,
)