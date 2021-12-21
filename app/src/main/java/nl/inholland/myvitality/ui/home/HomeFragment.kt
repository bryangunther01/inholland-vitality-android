package nl.inholland.myvitality.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import coil.load
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import nl.inholland.myvitality.data.adapters.CurrentChallengeAdapter
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragment
import nl.inholland.myvitality.data.adapters.ExploreChallengeAdapter
import nl.inholland.myvitality.data.entities.*
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.scoreboard.ScoreboardActivity
import nl.inholland.myvitality.util.TextViewUtils
import javax.inject.Inject

class HomeFragment : BaseFragment() {
    @BindView(R.id.home_curr_chl_recyclerview)
    lateinit var currentChallengesRecyclerView: RecyclerView

    @BindView(R.id.home_exp_chl_recyclerview)
    lateinit var exploreChallengesRecyclerView: RecyclerView

    @Inject
    lateinit var factory: HomeViewModelFactory
    lateinit var viewModel: HomeViewModel

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

        viewModel = ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
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

        initResponseHandler()
        initUser()
        Handler(Looper.getMainLooper()).postDelayed({
            initChallenges()
        }, 100)
    }

    @OnClick(value = [R.id.home_header_points, R.id.home_header_scoreboard])
    fun onClickViewScoreboard(){
        startActivity(Intent(requireContext(), ScoreboardActivity::class.java))
    }

    private fun setupSkeleton() {
        skeletonScreen = Skeleton.bind(exploreChallengesRecyclerView)
            .adapter(expChlAdapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.challenge_skeleton_view_item)
            .show()
    }

    private fun setupRecyclerViews() {
        crtLayoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        crtChlAdapter = CurrentChallengeAdapter(requireActivity())

        currentChallengesRecyclerView.let {
            it.adapter = crtChlAdapter
            it.layoutManager = crtLayoutManager
        }

        expLayoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        expChlAdapter = ExploreChallengeAdapter(requireActivity())

        exploreChallengesRecyclerView.let {
            it.adapter = expChlAdapter
            it.layoutManager = expLayoutManager
        }
    }

    private fun initUser(){
        viewModel.getLoggedInUser()

        viewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            val nameTextView = view?.findViewById<TextView>(R.id.home_header_name)
            nameTextView?.text = ""
            nameTextView?.append("${user.firstName} ${user.lastName}")

            // Set greeting message
            val profileImage = view?.findViewById<ImageView>(R.id.home_header_profile_image)
            profileImage?.load(user.profilePicture)

            // Set greeting message
            val points = view?.findViewById<TextView>(R.id.home_header_points)
            points?.text = getString(R.string.home_points_text, (user.points ?: 0).toString())
        })
    }


    private fun initChallenges(){
        viewModel.getChallenges()

        viewModel.currentChallenges.observe(viewLifecycleOwner, { challenges ->
            crtChlAdapter?.addItems(challenges)
        })

        viewModel.explorableChallenges.observe(viewLifecycleOwner, { challenges ->
            expChlAdapter?.addItems(challenges)
            skeletonScreen?.hide()
        })
    }

    private fun initResponseHandler(){
        viewModel.apiResponse.observe(viewLifecycleOwner, { response ->
            when(response.status){
                ResponseStatus.UNAUTHORIZED -> startActivity(Intent(requireActivity(), LoginActivity::class.java))
                ResponseStatus.API_ERROR -> Toast.makeText(requireContext(), getString(R.string.api_error), Toast.LENGTH_LONG).show()
                ResponseStatus.UPDATED_VALUE -> {}
            }
        })
    }
}