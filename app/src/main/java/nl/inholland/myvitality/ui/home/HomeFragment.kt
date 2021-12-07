package nl.inholland.myvitality.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import coil.load
import coil.transform.RoundedCornersTransformation
import nl.inholland.myvitality.data.adapters.CurrentChallengeAdapter
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.ExploreChallengeAdapter
import nl.inholland.myvitality.data.entities.Challenge
import nl.inholland.myvitality.data.entities.ChallengeProgress
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors
import javax.inject.Inject

class HomeFragment : Fragment() {
    @Inject
    lateinit var apiClient: ApiClient
    @BindView(R.id.home_curr_chl_recyclerview)
    lateinit var currentActivitiesRecyclerView: RecyclerView
    @BindView(R.id.home_exp_chl_recyclerview)
    lateinit var exploreActivitiesRecyclerView: RecyclerView

    private var sharedPrefs: SharedPreferenceHelper? = null

    var crtLayoutManager: LinearLayoutManager? = null
    var expLayoutManager: LinearLayoutManager? = null
    var crtChlAdapter: CurrentChallengeAdapter? = null
    var expChlAdapter: ExploreChallengeAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        ButterKnife.bind(this, view)

        sharedPrefs = SharedPreferenceHelper(requireActivity())

        // Set greeting message
        val greetingTextView = view.findViewById<TextView>(R.id.home_header_greeting)
        greetingTextView.append(TextViewUtils.getGreetingMessage(requireActivity()))

        // Set greeting message
        val nameTextView = view.findViewById<TextView>(R.id.home_header_name)
        nameTextView.append(sharedPrefs?.userFullName ?: "Hoe gaat het?")

        // Set greeting message
        val profileImage = view.findViewById<ImageView>(R.id.home_header_profile_image)
        profileImage.load(sharedPrefs?.userProfileImageUrl) {
            fallback(R.drawable.person_placeholder)
            transformations(RoundedCornersTransformation(20f))
        }

        return view
    }

    override fun onAttach(context: Context) {
        (requireActivity().application as VitalityApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()

        setupRecyclerViews()
        tryLoadChallenges()

    }

    private fun setupRecyclerViews(){
        crtLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        crtChlAdapter = CurrentChallengeAdapter(requireActivity())

        currentActivitiesRecyclerView.let {
            it.adapter = crtChlAdapter
            it.layoutManager = crtLayoutManager
        }

        expLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        expChlAdapter = ExploreChallengeAdapter(requireActivity())

        exploreActivitiesRecyclerView.let {
            it.adapter = expChlAdapter
            it.layoutManager = expLayoutManager
        }
    }

    private fun tryLoadChallenges(){
        sharedPrefs?.accessToken?.let {
            apiClient.getChallenges("Bearer $it", 20 , 0).enqueue(object : Callback<List<Challenge>> {
                override fun onResponse(call: Call<List<Challenge>>, response: Response<List<Challenge>>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { result ->
                            val currentChallenges = result.stream().filter { o -> o.challengeProgress == ChallengeProgress.IN_PROGRESS}.collect(Collectors.toList())
                            val exploreChallenges = result.stream().filter { o -> o.challengeProgress == ChallengeProgress.NOT_SUBSCRIBED}.collect(Collectors.toList())

                            crtChlAdapter?.addItems(currentChallenges)
                            expChlAdapter?.addItems(exploreChallenges)
                        }
                    } else {
                        if(response.code() == 401){
                            startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        }
                    }
                }

                override fun onFailure(call: Call<List<Challenge>>, t: Throwable) {
                    Toast.makeText(requireContext(), getString(R.string.api_error), Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}