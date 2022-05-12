package nl.inholland.myvitality.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnTextChanged
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.ChosenFragment
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.architecture.base.BaseActivityAdvanced
import nl.inholland.myvitality.data.adapters.UserListAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.databinding.ActivityScoreboardBinding
import nl.inholland.myvitality.databinding.ActivitySearchBinding
import nl.inholland.myvitality.ui.MainActivity
import javax.inject.Inject

class SearchActivity : BaseActivityAdvanced<ActivitySearchBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivitySearchBinding
            = ActivitySearchBinding::inflate

    @Inject
    lateinit var factory: SearchViewModelFactory
    lateinit var viewModel: SearchViewModel

    var layoutManager: LinearLayoutManager? = null
    var adapter: UserListAdapter? = null
    var isCalling: Boolean = false

    private var page = 0
    private var limit = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)

        setupRecyclerViews()
        initResponseHandler()
        initUsersObserver()

        binding.searchBar.requestFocus()
    }

    @OnClick(R.id.back_button)
    override fun onBackPressed() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra("FRAGMENT_TO_LOAD", ChosenFragment.FRAGMENT_TIMELINE.ordinal)
        )
    }

    private fun setupRecyclerViews() {
        layoutManager = LinearLayoutManager(this)
        adapter = UserListAdapter(this)

        binding.recyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
                    val visibleItemCount = layoutManager?.childCount ?: 0
                    val totalItemCount = layoutManager?.itemCount ?: 0
                    val pastVisibleItems = layoutManager?.findFirstVisibleItemPosition() ?: -1

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        tryLoadUsers()
                    }
                }
            }
        })
    }

    fun tryLoadUsers() {
        if (isCalling) return

        isCalling = true
        viewModel.search(binding.searchBar.text.toString(), limit, page * limit)
    }

    private fun initUsersObserver() {
        viewModel.results.observe(this) {
            if (page == 0) {
                adapter?.clearItems()
            }

            if (it.isNotEmpty()) {
                adapter?.addItems(it)

                if (it.size == limit) {
                    page += 1
                }
            }

            isCalling = false
        }
    }

    @OnTextChanged(R.id.search_bar)
    fun onSearchChanged() {
        if (binding.searchBar.text.length >= 3) {
            tryLoadUsers()
        } else {
            adapter?.clearItems()
        }
    }

    private fun initResponseHandler() {
        viewModel.apiResponse.observe(this) { response ->
            when (response.status) {
                ResponseStatus.API_ERROR -> Toast.makeText(
                    this,
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                else -> {
                }
            }
        }
    }
}