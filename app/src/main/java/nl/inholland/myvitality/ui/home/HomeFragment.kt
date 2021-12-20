package nl.inholland.myvitality.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import nl.inholland.myvitality.data.adapters.CurrentChallengeAdapter
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragment
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.ExploreChallengeAdapter
import nl.inholland.myvitality.data.entities.Challenge
import nl.inholland.myvitality.data.entities.ChallengeProgress
import nl.inholland.myvitality.data.entities.SimpleUser
import nl.inholland.myvitality.data.entities.User
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.util.TextViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors
import javax.inject.Inject

class HomeFragment : BaseFragment() {
    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper

    @BindView(R.id.home_curr_chl_recyclerview)
    lateinit var currentActivitiesRecyclerView: RecyclerView
    @BindView(R.id.home_exp_chl_recyclerview)
    lateinit var exploreActivitiesRecyclerView: RecyclerView


    var crtLayoutManager: LinearLayoutManager? = null
    var expLayoutManager: LinearLayoutManager? = null
    var crtChlAdapter: CurrentChallengeAdapter? = null
    var expChlAdapter: ExploreChallengeAdapter? = null
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null


    override fun layoutResourceId(): Int {
        return R.layout.fragment_home
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Set greeting message
        val greetingTextView = view.findViewById<TextView>(R.id.home_header_greeting)
        greetingTextView.append(TextViewUtils.getGreetingMessage(requireActivity()))

        loadCurrentUser()

        return view
    }

    override fun onAttach(context: Context) {
        (requireActivity().application as VitalityApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()

        setupRecyclerViews()
        setupSkeleton()

        Handler(Looper.getMainLooper()).postDelayed({
            tryLoadChallenges()
        }, 1000)
    }

    fun loadCurrentUser(){
        sharedPrefs.accessToken?.let {
            apiClient.getUser("Bearer $it").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if(response.isSuccessful && response.body() != null){
                        response.body()?.let { user ->
                            // Set greeting message
                            val nameTextView = view?.findViewById<TextView>(R.id.home_header_name)
                            nameTextView?.text = ""
                            nameTextView?.append("${user.firstName} ${user.lastName}")

                            // Set greeting message
                            val profileImage = view?.findViewById<ImageView>(R.id.home_header_profile_image)
                            profileImage?.load(user.profilePicture)

                            // Set greeting message
                            val points = view?.findViewById<TextView>(R.id.home_header_points)
                            points?.text = getString(R.string.home_points_text, (user.points ?: 0).toString())
                        }
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(requireActivity(), t.message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun setupSkeleton(){
        skeletonScreen = Skeleton.bind(exploreActivitiesRecyclerView)
            .adapter(expChlAdapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.challenge_skeleton_view_item)
            .show()
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
        sharedPrefs.accessToken?.let {
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

                    skeletonScreen?.hide()
                }

                override fun onFailure(call: Call<List<Challenge>>, t: Throwable) {
                    Toast.makeText(requireContext(), getString(R.string.api_error), Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}