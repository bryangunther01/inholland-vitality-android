package nl.inholland.myvitality.ui.timeline.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.OnClick
import coil.load
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton

import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragment
import nl.inholland.myvitality.data.adapters.TimelinePostAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.search.SearchActivity
import javax.inject.Inject

class TimelineOverviewFragment : BaseFragment() {
    @BindView(R.id.timeline_searchbar)
    lateinit var timelineSearchbar: EditText

    @BindView(R.id.timeline_refresh_layout)
    lateinit var refreshLayout: SwipeRefreshLayout

    @BindView(R.id.timeline_post_recyclerview)
    lateinit var timelineRecyclerView: RecyclerView

    @Inject
    lateinit var factory: TimelineOverviewViewModelFactory
    lateinit var viewModel: TimelineOverviewViewModel

    private var layoutManager: LinearLayoutManager? = null
    private var adapter: TimelinePostAdapter? = null
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null

    var isCalling: Boolean = false
    private var page = 0
    private var limit = 10

    override fun layoutResourceId(): Int {
        return R.layout.fragment_timeline
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProviders.of(this, factory).get(TimelineOverviewViewModel::class.java)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        (requireActivity().application as VitalityApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()

        initResponseHandler()

        setupRecyclerViews()
        setupSkeleton()

        initUser()
        Handler(Looper.getMainLooper()).postDelayed({
            initTimelinePosts()
        }, 100)
    }

    @OnClick(R.id.timeline_searchbar)
    fun onClickSearchbar() {
        startActivity(Intent(requireActivity(), SearchActivity::class.java))
    }

    private fun setupRecyclerViews() {
        layoutManager = LinearLayoutManager(requireActivity())
        adapter = TimelinePostAdapter(requireActivity())

        timelineRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        // Setup the refreshListener
        refreshLayout.setOnRefreshListener {
            adapter?.clearItems()
            tryLoadTimelinePosts(true)
            skeletonScreen?.show()
        }

        timelineRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
                    val visibleItemCount = layoutManager?.childCount ?: 0
                    val totalItemCount = layoutManager?.itemCount ?: 0
                    val pastVisibleItems = layoutManager?.findFirstVisibleItemPosition() ?: -1

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        tryLoadTimelinePosts()
                    }
                }
            }
        })
    }

    private fun setupSkeleton() {
        skeletonScreen = Skeleton.bind(timelineRecyclerView)
            .adapter(adapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.timeline_post_skeleton_view_item)
            .show()
    }

    private fun initUser() {
        viewModel.getLoggedInUser()

        viewModel.currentUser.observe(viewLifecycleOwner, { user ->
            // Set greeting message
            val profileImage = view?.findViewById<ImageView>(R.id.timeline_profile_image)
            profileImage?.load(user.profilePicture)
        })
    }

    private fun initTimelinePosts() {
        tryLoadTimelinePosts()
        viewModel.posts.observe(this, {
            adapter?.addItems(it)

            if (page == 0) skeletonScreen?.hide()
            if (it.isNotEmpty()) page += 1

            refreshLayout.isRefreshing = false
            isCalling = false
        })
    }

    private fun tryLoadTimelinePosts(refresh: Boolean = false) {
        if (isCalling) return
        if (refresh) page = 0

        isCalling = true
        viewModel.getTimelinePosts(limit, page * limit)
    }

    override fun onResume() {
        super.onResume()
        skeletonScreen?.hide()
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(viewLifecycleOwner, { response ->
            when (response.status) {
                ResponseStatus.UNAUTHORIZED -> startActivity(
                    Intent(
                        requireActivity(),
                        LoginActivity::class.java
                    )
                )
                ResponseStatus.API_ERROR -> Toast.makeText(
                    requireContext(),
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                ResponseStatus.UPDATED_VALUE -> {
                }
            }
        })
    }
}