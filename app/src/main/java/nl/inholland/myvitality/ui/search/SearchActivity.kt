package nl.inholland.myvitality.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnTextChanged
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.R
import nl.inholland.myvitality.VitalityApplication
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.adapters.CommentAdapter
import nl.inholland.myvitality.data.adapters.UserListAdapter
import nl.inholland.myvitality.data.entities.SimpleUser
import nl.inholland.myvitality.data.entities.TimelinePost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class SearchActivity : AppCompatActivity(), Callback<List<SimpleUser>> {

    @Inject
    lateinit var apiClient: ApiClient
    @BindView(R.id.search_recyclerview)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.search_bar)
    lateinit var searchbar: EditText

    var sharedPrefs: SharedPreferenceHelper? = null

    var layoutManager: LinearLayoutManager? = null
    var adapter: UserListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        ButterKnife.bind(this)

        (application as VitalityApplication).appComponent.inject(this)
        sharedPrefs = SharedPreferenceHelper(this)

        setupRecyclerViews()
    }

    private fun setupRecyclerViews(){
        layoutManager = LinearLayoutManager(this)
        adapter = UserListAdapter(this)

        recyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }
    }

    @OnTextChanged(R.id.search_bar)
    fun onSearchChanged(){
        if(searchbar.text.length >= 3){
            sharedPrefs?.accessToken?.let {
                apiClient.searchUser("Bearer $it", searchbar.text.toString()).enqueue(this)
            }
        } else {
            adapter?.clearItems()
        }
    }

    override fun onResponse(call: Call<List<SimpleUser>>, response: Response<List<SimpleUser>>) {
        if(response.isSuccessful && response.body() != null){
            response.body()?.let {
                adapter?.clearItems()
                adapter?.addItems(it)
            }
        }
    }

    override fun onFailure(call: Call<List<SimpleUser>>, t: Throwable) {

    }
}