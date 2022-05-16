package nl.inholland.myvitality.data.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import nl.inholland.myvitality.R
import nl.inholland.myvitality.architecture.base.BaseRecyclerAdapter
import nl.inholland.myvitality.data.entities.Achievement
import nl.inholland.myvitality.data.entities.AchievementType
import nl.inholland.myvitality.data.entities.Activity
import nl.inholland.myvitality.ui.activity.detail.ActivityDetailActivity
import nl.inholland.myvitality.util.DateUtils
import nl.inholland.myvitality.util.StringUtils.toHtmlSpan

class AchievementAdapter(context: Context) :
    BaseRecyclerAdapter<Achievement, AchievementAdapter.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.achievement_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = items[position]

        // Set the challenge type
        when (achievement.achievementType) {
            AchievementType.POINTS -> {
                holder.achievementImage.setImageResource(R.drawable.ic_medal_points)
                holder.achievementTitle.text = context.getString(R.string.achievement_points_profile_text, achievement.count).toHtmlSpan()
            }
            AchievementType.ACTIVITY -> {
                if(achievement.count == 1) {
                    holder.achievementImage.setImageResource(R.drawable.ic_medal)
                    holder.achievementTitle.text = context.getString(R.string.achievement_first_activity_profile_text).toHtmlSpan()
                } else {
                    holder.achievementImage.setImageResource(R.drawable.ic_medal_activities)
                    holder.achievementTitle.text = context.getString(R.string.achievement_activities_profile_text, achievement.count).toHtmlSpan()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val achievementImage: ImageView = itemView.findViewById(R.id.achievement_image)
        internal val achievementTitle: TextView = itemView.findViewById(R.id.achievement_title)
    }
}