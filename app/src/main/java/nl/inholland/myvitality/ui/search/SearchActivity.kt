package nl.inholland.myvitality.ui.search

import android.content.Intent
import android.os.Bundle
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
import nl.inholland.myvitality.data.adapters.UserListAdapter
import nl.inholland.myvitality.data.entities.ResponseStatus
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import javax.inject.Inject

class SearchActivity : BaseActivity() {

    @BindView(R.id.search_recyclerview)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.search_bar)
    lateinit var searchbar: EditText

    @Inject
    lateinit var factory: SearchViewModelFactory
    lateinit var viewModel: SearchViewModel

    var layoutManager: LinearLayoutManager? = null
    var adapter: UserListAdapter? = null
    var isCalling: Boolean = false

    private var page = 0
    private var limit = 10

    override fun layoutResourceId(): Int {
        return R.layout.activity_search
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)

        setupRecyclerViews()
        initResponseHandler()
        initUsersObserver()

        searchbar.requestFocus()
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

        recyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        viewModel.search(searchbar.text.toString(), limit, page * limit)
    }

    private fun initUsersObserver() {
        viewModel.results.observe(this, {
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
        })
    }

    @OnTextChanged(R.id.search_bar)
    fun onSearchChanged() {
        if (searchbar.text.length >= 3) {
            tryLoadUsers()
        } else {
            adapter?.clearItems()
        }
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
                    getString(R.string.api_error),
                    Toast.LENGTH_LONG
                ).show()
                ResponseStatus.UPDATED_VALUE -> {
                }
            }
        })
    }
}