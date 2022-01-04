package nl.inholland.myvitality.ui.timelinepost.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.gunther.bryan.newsreader.utils.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class TimelinePostViewModelFactory @Inject constructor(private val apiClient: ApiClient, private val sharedPrefs: SharedPreferenceHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimelinePostViewModel::class.java)) {
            return TimelinePostViewModel(apiClient, sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}