package nl.inholland.myvitality.ui.timeline.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragment
import nl.inholland.myvitality.data.adapters.TimelinePostAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.FragmentTimelineBinding
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity
import nl.inholland.myvitality.ui.search.SearchActivity
import javax.inject.Inject

class TimelineOverviewFragment : BaseFragment<FragmentTimelineBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTimelineBinding
            = FragmentTimelineBinding::inflate

    @Inject
    lateinit var factory: TimelineOverviewViewModelFactory
    lateinit var viewModel: TimelineOverviewViewModel

    private var layoutManager: LinearLayoutManager? = null
    private var adapter: TimelinePostAdapter? = null
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null

    var isCalling: Boolean = false
    private var page = 0
    private var limit = 10

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSkeleton()

        initResponseHandler()
        initUser()
    }

    @OnClick(R.id.profile_image)
    fun onClickProfileImage() {
        startActivity(Intent(requireActivity(), ProfileActivity::class.java))
    }

    @OnClick(R.id.searchbar)
    fun onClickSearchbar() {
        startActivity(Intent(requireActivity(), SearchActivity::class.java))
    }

    private fun setupRecyclerViews() {
        layoutManager = LinearLayoutManager(requireActivity())
        adapter = TimelinePostAdapter(requireActivity()) { timelinePost, viewHolder ->
            viewModel.updateLike(timelinePost.postId, !timelinePost.iLikedPost)
            toggleLike(viewHolder, !timelinePost.iLikedPost, timelinePost.countOfLikes)
        }

        binding.recyclerview.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        // Setup the refreshListener
        binding.refreshLayout.setOnRefreshListener {
            adapter?.clearItems()
            tryLoadTimelinePosts(true)
            skeletonScreen?.show()
        }

        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    fun toggleLike(viewHolder: TimelinePostAdapter.ViewHolder, isLiked: Boolean, countOfLikes: Int) {
        if (isLiked) {
            updateLikeCount(viewHolder, true, countOfLikes+1)

            viewHolder.likedButton.setIconResource(R.drawable.ic_thumbsup_fill)
            viewHolder.likedButton.setIconTintResource(R.color.primary)
        } else {
            updateLikeCount(viewHolder, false, countOfLikes)

            viewHolder.likedButton.setIconResource(R.drawable.ic_thumbsup)
            viewHolder.likedButton.setIconTintResource(R.color.black)
        }
    }

    fun updateLikeCount(viewHolder: TimelinePostAdapter.ViewHolder, isLiked: Boolean, likeCount: Int){
        if(isLiked){
            val otherCount = likeCount-1
            val visibility = View.VISIBLE

            viewHolder.likeIcon.visibility = visibility
            viewHolder.likeCount.visibility = visibility

            if (otherCount == 0) {
                viewHolder.likeCount.text = getString(R.string.post_like_count_you)
            } else {
                viewHolder.likeCount.text = getString(R.string.post_like_count, otherCount.toString())
            }
        } else {
            val visibility = if (likeCount > 0) View.VISIBLE else View.GONE

            viewHolder.likeIcon.visibility = visibility
            viewHolder.likeCount.visibility = visibility

            viewHolder.likeCount.text = likeCount.toString()
        }
    }

    private fun setupSkeleton() {
        skeletonScreen = Skeleton.bind(binding.recyclerview)
            .adapter(adapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.timeline_post_skeleton_view_item)
            .show()
    }

    private fun initUser() {
        if(view == null) return

        viewModel.getLoggedInUser()
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            initTimelinePosts()

            // Set greeting message
            Glide.with(this)
                    .load(user.profilePicture)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.profileImage)

        }
    }

    private fun initTimelinePosts() {
        if(view == null) return

        tryLoadTimelinePosts()
        viewModel.posts.observe(viewLifecycleOwner) {
            if (page == 0 && it.isEmpty()) {
                binding.emptyGroup.visibility = View.VISIBLE
                binding.recyclerview.visibility = View.INVISIBLE
            } else {
                binding.emptyGroup.visibility = View.INVISIBLE
                binding.recyclerview.visibility = View.VISIBLE
            }

            adapter?.addItems(it)

            if (page == 0) skeletonScreen?.hide()
            if (it.isNotEmpty()) page++

            binding.refreshLayout.isRefreshing = false
            isCalling = false
        }
    }

    private fun tryLoadTimelinePosts(refresh: Boolean = false) {
        if (isCalling) return
        if (refresh) page = 0

        isCalling = true
        viewModel.getTimelinePosts(limit, page * limit)
    }
    
    private fun initResponseHandler() {
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