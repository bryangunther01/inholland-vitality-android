package nl.inholland.myvitality.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.TutorialType

class TutorialTypeFragment(val type: TutorialType) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.tutorial_type_fragment, container, false)

        val image = view.findViewById<ImageView>(R.id.tutorial_detail_image)
        val description = view.findViewById<TextView>(R.id.achievement_text)

        // Set the challenge type
        when (type) {
            TutorialType.COMMUNITY -> {
                image.setImageResource(R.drawable.activity_category_group)
                description.text = getString(R.string.tutorial_description_community)
            }
            TutorialType.TIMELINE -> {
                image.setImageResource(R.drawable.timeline_group)
                description.text = getString(R.string.tutorial_description_timeline)
            }
            TutorialType.ACHIEVEMENTS -> {
                image.setImageResource(R.drawable.ic_medal)
                description.text = getString(R.string.tutorial_description_achievements)
            }
        }

        return view
    }
}