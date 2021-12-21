package nl.inholland.myvitality.ui.notification

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.adapters.NotificationAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import javax.inject.Inject


class NotificationActivity : BaseActivity() {

    @BindView(nl.inholland.myvitality.R.id.notifications_recyclerview)
    lateinit var recyclerView: RecyclerView

    @BindView(nl.inholland.myvitality.R.id.notifications_refresh_layout)
    lateinit var refreshLayout: SwipeRefreshLayout

    @Inject
    lateinit var factory: NotificationViewModelFactory
    lateinit var viewModel: NotificationViewModel

    var layoutManager: LinearLayoutManager? = null
    var adapter: NotificationAdapter? = null
    var isCalling: Boolean = false

    private var page = 0
    private var limit = 10

    override fun layoutResourceId(): Int {
        return R.layout.activity_notification
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_notifications)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(NotificationViewModel::class.java)

        setupRecyclerViews()
        initResponseHandler()
        initNotifications()
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.notifications_nav_menu, menu)
//        return true
//    }
//
//    //and this to handle actions
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if(item.itemId == R.id.navigation_notification_settings){
//            Toast.makeText(this, "WIP - Show notification settings", Toast.LENGTH_LONG).show()
//            return true
//        }
//
//        return super.onOptionsItemSelected(item)
//    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun setupRecyclerViews() {
        layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(this)

        recyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        // Setup the refreshListener
        refreshLayout.setOnRefreshListener {
            adapter?.clearItems()
            tryLoadNotifications(true)
            // TODO:
//            skeletonScreen?.show()
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

    private fun initNotifications() {
        tryLoadNotifications()

        viewModel.notifications.observe(this, {
            adapter?.addItems(it)

            // TODO
//            if (page == 0) skeletonScreen?.hide()
            if (it.isNotEmpty()) page += 1

            refreshLayout.isRefreshing = false
            isCalling = false
        })
    }

    private fun tryLoadNotifications(refresh: Boolean = false) {
        if (isCalling) return
        if (refresh) page = 0

        isCalling = true
        viewModel.getNotifications(limit, page * limit)
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this, { response ->
            when (response.status) {
                ResponseStatus.UNAUTHORIZED -> startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )
                ResponseStatus.API_ERROR -> Toast.makeText(
                    this,
                    getString(nl.inholland.myvitality.R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                ResponseStatus.UPDATED_VALUE -> { }
            }
        })
    }
}