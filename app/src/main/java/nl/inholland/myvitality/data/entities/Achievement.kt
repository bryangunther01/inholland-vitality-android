package nl.inholland.myvitality.data.entities

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Achievement(
    @Json(name = "achievementId") val achievementId: String,
    @Json(name = "achievementType") val achievementType: AchievementType,
    @Json(name = "count") var count: Int,
    @Json(name = "achievementDate") var achievementDate: String,
    @Json(name = "userPercentage") var userPercentage: Int,
): Parcelable