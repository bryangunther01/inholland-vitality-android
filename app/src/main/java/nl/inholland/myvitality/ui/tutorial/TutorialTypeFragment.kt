package nl.inholland.myvitality.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.entities.ChallengeType

class TutorialTypeFragment(val type: ChallengeType) : Fragment() {

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
            ChallengeType.EXERCISE -> {
                challengeType.text = getString(R.string.challenge_type_exercise)
                description.text = getString(R.string.tutorial_description_exercise)
            }
            ChallengeType.DIET -> {
                challengeType.text = getString(R.string.challenge_type_diet)
                description.text = getString(R.string.tutorial_description_diet)
            }
            ChallengeType.MIND -> {
                challengeType.text = getString(R.string.challenge_type_mind)
                description.text = getString(R.string.tutorial_description_mind)
            }
        }

        return view
    }
}