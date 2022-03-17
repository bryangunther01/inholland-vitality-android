package nl.inholland.myvitality.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.ActivityType

class TutorialTypeFragment(val type: ActivityType) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.tutorial_type_fragment, container, false)

        val challengeType = view.findViewById<TextView>(R.id.tutorial_challenge_type)
        val description = view.findViewById<TextView>(R.id.tutorial_description)

        // Set the challenge type
        when (type) {
            ActivityType.EXERCISE -> {
                challengeType.text = getString(R.string.activity_type_exercise)
                description.text = getString(R.string.tutorial_description_exercise)
            }
            ActivityType.DIET -> {
                challengeType.text = getString(R.string.activity_type_diet)
                description.text = getString(R.string.tutorial_description_diet)
            }
            ActivityType.MIND -> {
                challengeType.text = getString(R.string.activity_type_mind)
                description.text = getString(R.string.tutorial_description_mind)
            }
        }

        return view
    }
}