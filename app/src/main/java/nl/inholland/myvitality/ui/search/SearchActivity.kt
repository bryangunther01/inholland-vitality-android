package nl.inholland.myvitality.ui.search

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTextChanged
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.architecture.ChosenFragment
import nl.inholland.myvitality.architecture.base.BaseActivity
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.CommentAdapter
import nl.inholland.myvitality.data.adapters.UserListAdapter
import nl.inholland.myvitality.data.entities.SimpleUser
import nl.inholland.myvitality.data.entities.TimelinePost
import nl.inholland.myvitality.ui.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class SearchActivity : BaseActivity(), Callback<List<SimpleUser>> {

    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var sharedPrefs: SharedPreferenceHelper
    @BindView(R.id.search_recyclerview)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.search_bar)
    lateinit var searchbar: EditText

    var layoutManager: LinearLayoutManager? = null
    var adapter: UserListAdapter? = null
    var isLoading: Boolean = false

    private var page = 0
    private var limit = 10

    override fun layoutResourceId(): Int {
        return R.layout.activity_search
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as VitalityApplication).appComponent.inject(this)

        setupRecyclerViews()
    }

    @OnClick(R.id.back_button)
    override fun onBackPressed() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra("FRAGMENT_TO_LOAD", ChosenFragment.FRAGMENT_TIMELINE.ordinal))
    }

    private fun setupRecyclerViews(){
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

                    if (!isLoading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            isLoading = true
                            tryLoadUsers()
                        }
                    }
                }
            }
        })
    }

    fun tryLoadUsers(){
        sharedPrefs.accessToken?.let {
            apiClient.searchUser("Bearer $it", searchbar.text.toString(), limit , page * limit).enqueue(this)
        }
    }

    @OnTextChanged(R.id.search_bar)
    fun onSearchChanged(){
        if(searchbar.text.length >= 3){
            tryLoadUsers()
        } else {
            adapter?.clearItems()
        }
    }

    override fun onResponse(call: Call<List<SimpleUser>>, response: Response<List<SimpleUser>>) {
        if(response.isSuccessful && response.body() != null){
            response.body()?.let {
                if(page == 0){
                    adapter?.clearItems()
                }

                if(it.isNotEmpty()){
                    adapter?.addItems(it)

                    if(it.size == limit){
                        page += 1
                    }
                }

                isLoading = false
            }
        }
    }

    override fun onFailure(call: Call<List<SimpleUser>>, t: Throwable) {

    }
}