package nl.inholland.myvitality.ui.achievement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.Achievement
import nl.inholland.myvitality.data.entities.AchievementType
import nl.inholland.myvitality.util.StringUtils.toHtmlSpan

class AchievementFragment(private val achievement: Achievement) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.achievement_fragment, container, false)

        val image = view.findViewById<ImageView>(R.id.achievement_medal)
        val description = view.findViewById<TextView>(R.id.achievement_text)

        // Set the challenge type
        when (achievement.achievementType) {
            AchievementType.POINTS -> {
                image.setImageResource(R.drawable.ic_medal_points)
                description.text = getString(R.string.achievement_points_text, achievement.count, achievement.userPercentage).toHtmlSpan()
            }
            AchievementType.ACTIVITY -> {

                if(achievement.count == 1) {
                    image.setImageResource(R.drawable.ic_medal)
                    description.text = getString(R.string.achievement_first_activity_text, achievement.userPercentage).toHtmlSpan()
                } else {
                    image.setImageResource(R.drawable.ic_medal_activities)
                    description.text = getString(R.string.achievement_activities_text, achievement.count, achievement.userPercentage).toHtmlSpan()
                }
            }
        }

        return view
    }
}