package nl.inholland.myvitality.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.inholland.myvitality.R
import nl.inholland.myvitality.data.adapters.AdvantageAdapter

/**
 * End fragment of the tutorial
 */
class TutorialEndFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.tutorial_end_fragment, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.advantage_recycler_view)
        val adapter = AdvantageAdapter(requireContext())

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.addItems(listOf(
            getString(R.string.tutorial_advantage_1),
            getString(R.string.tutorial_advantage_2),
            getString(R.string.tutorial_advantage_3),
            getString(R.string.tutorial_advantage_4),
            getString(R.string.tutorial_advantage_5)
        ))

        return view
    }
}