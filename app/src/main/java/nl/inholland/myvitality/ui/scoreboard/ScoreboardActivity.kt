package nl.inholland.myvitality.ui.scoreboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.adapters.ScoreboardAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.notification.ScoreboardViewModel
import nl.inholland.myvitality.ui.notification.ScoreboardViewModelFactory
import javax.inject.Inject


class ScoreboardActivity : BaseActivity() {

    @BindView(R.id.scoreboard_recyclerview)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.scoreboard_refresh_layout)
    lateinit var refreshLayout: SwipeRefreshLayout

    @Inject
    lateinit var factory: ScoreboardViewModelFactory
    lateinit var viewModel: ScoreboardViewModel

    var layoutManager: LinearLayoutManager? = null
    var adapter: ScoreboardAdapter? = null
    var skeletonScreen: RecyclerViewSkeletonScreen? = null

    var isCalling: Boolean = false

    private var page = 0
    private var limit = 10

    override fun layoutResourceId(): Int {
        return R.layout.activity_scoreboard
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_scoreboard)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(ScoreboardViewModel::class.java)

        setupRecyclerViews()
        setupSkeleton()

        initResponseHandler()
        initScoreboard()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun setupRecyclerViews() {
        layoutManager = LinearLayoutManager(this)
        adapter = ScoreboardAdapter(this)

        recyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        // Setup the refreshListener
        refreshLayout.setOnRefreshListener {
            adapter?.clearItems()
            tryLoadNotifications(true)
            skeletonScreen?.show()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
                    val visibleItemCount = layoutManager?.childCount ?: 0
                    val totalItemCount = layoutManager?.itemCount ?: 0
                    val pastVisibleItems = layoutManager?.findFirstVisibleItemPosition() ?: -1

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        tryLoadNotifications()
                    }
                }
            }
        })
    }

    private fun setupSkeleton() {
        skeletonScreen = Skeleton.bind(recyclerView)
            .adapter(adapter)
            .frozen(true)
            .duration(2400)
            .count(10)
            .load(R.layout.scoreboard_skeleton_view_item)
            .show()
    }

    private fun initScoreboard() {
        tryLoadNotifications()

        viewModel.results.observe(this, {
            adapter?.addItems(it)

            if (page == 0) skeletonScreen?.hide()
            if (it.isNotEmpty()) page += 1

            isCalling = false
            refreshLayout.isRefreshing = false
        })
    }

    private fun tryLoadNotifications(refresh: Boolean = false) {
        if (isCalling) return
        if (refresh) page = 0

        isCalling = true
        viewModel.getScoreboardItems(limit, page * limit)
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this, { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> Toast.makeText(
                    this,
                    getString(nl.inholland.myvitality.R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                else -> {}
            }

            isCalling = false
            refreshLayout.isRefreshing = false
        })
    }
}