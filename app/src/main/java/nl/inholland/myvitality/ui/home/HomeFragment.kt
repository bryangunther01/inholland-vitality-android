package nl.inholland.myvitality.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragment
import nl.inholland.myvitality.data.adapters.ActivityCategoryAdapter
import nl.inholland.myvitality.data.entities.ActivityCategory
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.ui.scoreboard.ScoreboardActivity
import nl.inholland.myvitality.util.TextViewUtils
import javax.inject.Inject

class HomeFragment : BaseFragment() {
    @BindView(R.id.activity_recyclerview)
    lateinit var currentChallengesRecyclerView: RecyclerView

    @Inject
    lateinit var factory: HomeViewModelFactory
    lateinit var viewModel: HomeViewModel

    private var layoutManager: StaggeredGridLayoutManager? = null
    private var adapter: ActivityCategoryAdapter? = null

    // TODO: Fix this in new layout
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()

        // TODO: Fix this in new layout
//        setupSkeleton()

        initResponseHandler()
        initUser()
    }

    @OnClick(value = [R.id.home_header_points, R.id.home_header_scoreboard])
    fun onClickViewScoreboard(){
        startActivity(Intent(requireContext(), ScoreboardActivity::class.java))
    }

    // TODO: Fix this in new layout
//    private fun setupSkeleton() {
//        skeletonScreen = Skeleton.bind(exploreChallengesRecyclerView)
//            .adapter(expChlAdapter)
//            .frozen(true)
//            .duration(2400)
//            .count(10)
//            .load(R.layout.challenge_skeleton_view_item)
//            .show()
//    }

    private fun setupRecyclerViews() {
        layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = ActivityCategoryAdapter(requireActivity())

        currentChallengesRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }
    }

    private fun initUser(){
        if(view == null) return

        viewModel.getLoggedInUser()
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            initCategories()

            val nameTextView = view?.findViewById<TextView>(R.id.home_header_name)
            nameTextView?.text = ""
            nameTextView?.append("${user.firstName} ${user.lastName}")

            // Set greeting message
            val profileImage = view?.findViewById<ImageView>(R.id.home_header_profile_image)

            profileImage?.let {
                Glide.with(this)
                    .load(user.profilePicture)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(profileImage)
            }

            // Set greeting message
            val points = view?.findViewById<TextView>(R.id.home_header_points)
            points?.text = getString(R.string.home_points_text, (user.points ?: 0).toString())
        }
    }

    private fun initCategories(){
        if(view == null) return

        adapter?.addItem(ActivityCategory("", getString(R.string.home_my_activities_text), "", "", true))

        viewModel.getActivityCategories()
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter?.addItems(categories)
        }
    }

    private fun initResponseHandler(){
        if(view == null) return

        viewModel.apiResponse.observe(viewLifecycleOwner) { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> Toast.makeText(
                    requireContext(),
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                else -> {
                }
            }
        }
    }
}