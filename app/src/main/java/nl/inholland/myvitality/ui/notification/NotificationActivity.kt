package nl.inholland.myvitality.ui.notification

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.base.BaseActivityAdvanced
import nl.inholland.myvitality.data.adapters.NotificationAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityNotificationBinding
import nl.inholland.myvitality.ui.widgets.dialog.Dialogs
import javax.inject.Inject


class NotificationActivity : BaseActivityAdvanced<ActivityNotificationBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityNotificationBinding
            = ActivityNotificationBinding::inflate

    @Inject
    lateinit var factory: NotificationViewModelFactory
    lateinit var viewModel: NotificationViewModel

    var layoutManager: LinearLayoutManager? = null
    var adapter: NotificationAdapter? = null
    var isCalling: Boolean = false

    private var page = 0
    private var limit = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.navigation_notifications)

        (application as VitalityApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(NotificationViewModel::class.java)

        setupRecyclerViews()
        initResponseHandler()
        initNotifications()

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.validatePushToken()
        }, 1000)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.notifications_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.notification_settings){
            val isEnabled = viewModel.notificationsEnabled.value ?: false

            val dialog = Dialogs.createNotificationSettingsDialog(this, isEnabled)
            val button = dialog.findViewById<AppCompatButton>(R.id.dialog_button)

            button.setOnClickListener {
                val isChecked = dialog.findViewById<SwitchCompat>(R.id.notifications_general_switch).isChecked
                if(isChecked) { viewModel.savePushToken() } else { viewModel.deletePushToken() }
            }

            dialog.show()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerViews() {
        layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(this)

        binding.recyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        // Setup the refreshListener
        binding.refreshLayout.setOnRefreshListener {
            adapter?.clearItems()
            tryLoadNotifications(true)
            Dialogs.showGeneralLoadingDialog(this)
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

        viewModel.notifications.observe(this) {
            if (page == 0 && it.isEmpty()) {
                binding.emptyIcon.visibility = View.VISIBLE
                binding.emptyText.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.INVISIBLE
            } else {
                binding.emptyIcon.visibility = View.INVISIBLE
                binding.emptyText.visibility = View.INVISIBLE
                binding.recyclerView.visibility = View.VISIBLE
            }

            adapter?.addItems(it)

            Dialogs.hideCurrentDialog()
            if (it.isNotEmpty()) page += 1

            binding.refreshLayout.isRefreshing = false
            isCalling = false
        }
    }

    private fun tryLoadNotifications(refresh: Boolean = false) {
        if (isCalling) return
        if (refresh) page = 0

        isCalling = true
        viewModel.getNotifications(limit, page * limit)
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this) { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> Toast.makeText(
                    this,
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                ResponseStatus.UPDATED_VALUE -> {
                    Dialogs.hideCurrentDialog()
                }
                else -> {
                }
            }
        }
    }
}