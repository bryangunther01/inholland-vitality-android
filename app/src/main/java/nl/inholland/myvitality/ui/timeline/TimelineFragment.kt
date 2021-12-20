package nl.inholland.myvitality.ui.timeline

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.OnClick
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton

import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseFragment
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.TimelinePostAdapter
import nl.inholland.myvitality.data.entities.TimelinePost
import nl.inholland.myvitality.ui.search.SearchActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class TimelineFragment : BaseFragment(), Callback<List<TimelinePost>> {
    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper
    @BindView(R.id.timeline_searchbar)
    lateinit var timelineSearchbar: EditText
    @BindView(R.id.timeline_refresh_layout)
    lateinit var refreshLayout: SwipeRefreshLayout
    @BindView(R.id.timeline_post_recyclerview)
    lateinit var timelineRecyclerView: RecyclerView

    private var layoutManager: LinearLayoutManager? = null
    private var adapter: TimelinePostAdapter? = null
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null

    var isLoading: Boolean = true
    private var page = 0
    private var limit = 10

    override fun layoutResourceId(): Int {
        return R.layout.fragment_timeline
    }

    override fun onAttach(context: Context) {
        (requireActivity().application as VitalityApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()

        setupRecyclerViews()
        setupSkeleton()
        tryLoadChallenges()
    }

    @OnClick(R.id.timeline_searchbar)
    fun onClickSearchbar(){
        startActivity(Intent(requireActivity(), SearchActivity::class.java))
    }

    private fun setupRecyclerViews(){
        layoutManager = LinearLayoutManager(requireActivity())
        adapter = TimelinePostAdapter(requireActivity())

        timelineRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        // Setup the refreshListener
        refreshLayout.setOnRefreshListener {
            adapter?.clearItems()
            tryLoadChallenges(true)
            skeletonScreen?.show()
        }

        timelineRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
                    val visibleItemCount = layoutManager?.childCount ?: 0
                    val totalItemCount = layoutManager?.itemCount ?: 0
                    val pastVisibleItems = layoutManager?.findFirstVisibleItemPosition() ?: -1

                    if (isLoading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            isLoading = false
                            tryLoadChallenges()
                        }
                    }
                }
            }
        })
    }

    private fun setupSkeleton(){
        skeletonScreen = Skeleton.bind(timelineRecyclerView)
            .adapter(adapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.timeline_post_skeleton_view_item)
            .show()
    }

    private fun tryLoadChallenges(refresh: Boolean = false){
        if(refresh) page = 0

        sharedPrefs.accessToken?.let {
            apiClient.getTimelinePosts("Bearer $it", limit , page * limit).enqueue(this)
        }
    }

    override fun onResponse(call: Call<List<TimelinePost>>, response: Response<List<TimelinePost>>) {
        if(response.isSuccessful && response.body() != null){
            response.body()?.let {
                adapter?.addItems(it)

                if(page == 0){
                    skeletonScreen?.hide()
                }

                if(it.isNotEmpty()){
                    page += 1
                }

                isLoading = true
                refreshLayout.isRefreshing = false
            }
        } else {
            if(response.code() == 401){

            }
        }
    }

    override fun onFailure(call: Call<List<TimelinePost>>, t: Throwable) {
        Toast.makeText(requireContext(), getString(R.string.api_error), Toast.LENGTH_LONG).show()
    }
}