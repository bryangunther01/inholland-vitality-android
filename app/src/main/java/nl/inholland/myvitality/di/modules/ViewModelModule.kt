package nl.inholland.myvitality.di.modules

import dagger.Module
import dagger.Provides
import nl.inholland.myvitality.util.SharedPreferenceHelper
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.ui.activity.detail.ActivityViewModel
import nl.inholland.myvitality.ui.home.HomeViewModelFactory
import nl.inholland.myvitality.ui.notification.NotificationViewModel
import nl.inholland.myvitality.ui.notification.ScoreboardViewModel
import nl.inholland.myvitality.ui.profile.overview.ProfileViewModel
import nl.inholland.myvitality.ui.search.SearchViewModel
import nl.inholland.myvitality.ui.timeline.liked.TimelineLikedViewModel
import nl.inholland.myvitality.ui.timeline.overview.TimelineOverviewViewModel

@Module
class ViewModelModule {

    @Provides
    fun providesHomeViewModelFactory(apiClient: ApiClient, sharedPres: SharedPreferenceHelper): HomeViewModelFactory {
        return HomeViewModelFactory(apiClient, sharedPres)
    }

    @Provides
    fun providesChallengeViewModelFactory(apiClient: ApiClient, sharedPres: SharedPreferenceHelper): ActivityViewModel {
        return ActivityViewModel(apiClient, sharedPres)
    }

    @Provides
    fun providesSearchViewModelFactory(apiClient: ApiClient, sharedPres: SharedPreferenceHelper): SearchViewModel {
        return SearchViewModel(apiClient, sharedPres)
    }

    @Provides
    fun providesTimelineOverviewViewModelFactory(apiClient: ApiClient, sharedPres: SharedPreferenceHelper): TimelineOverviewViewModel {
        return TimelineOverviewViewModel(apiClient, sharedPres)
    }

    @Provides
    fun providesTimelineLikedViewModelFactory(apiClient: ApiClient, sharedPres: SharedPreferenceHelper): TimelineLikedViewModel {
        return TimelineLikedViewModel(apiClient, sharedPres)
    }

    @Provides
    fun providesProfileViewModelModelFactory(apiClient: ApiClient, sharedPres: SharedPreferenceHelper): ProfileViewModel {
        return ProfileViewModel(apiClient, sharedPres)
    }

    @Provides
    fun providesNotificationViewModelModelFactory(apiClient: ApiClient, sharedPres: SharedPreferenceHelper): NotificationViewModel {
        return NotificationViewModel(apiClient, sharedPres)
    }

    @Provides
    fun providesScoreboardViewModelModelFactory(apiClient: ApiClient, sharedPres: SharedPreferenceHelper): ScoreboardViewModel {
        return ScoreboardViewModel(apiClient, sharedPres)
    }
}