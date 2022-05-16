package nl.inholland.myvitality.architecture

import dagger.Component
import nl.inholland.myvitality.di.modules.ApplicationModule
import nl.inholland.myvitality.di.modules.NetworkModule
import nl.inholland.myvitality.di.modules.ViewModelModule
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.achievement.AchievementActivity
import nl.inholland.myvitality.ui.activity.detail.ActivityDetailActivity
import nl.inholland.myvitality.ui.activity.overview.ActivityOverviewActivity
import nl.inholland.myvitality.ui.activity.participants.ActivityParticipantsActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.authentication.register.additionaldetails.RegisterAdditionalDetailsActivity
import nl.inholland.myvitality.ui.authentication.register.details.RegisterDetailsActivity
import nl.inholland.myvitality.ui.home.HomeFragment
import nl.inholland.myvitality.ui.notification.NotificationActivity
import nl.inholland.myvitality.ui.profile.edit.ProfileEditActivity
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity
import nl.inholland.myvitality.ui.push_notifications.PushService
import nl.inholland.myvitality.ui.scoreboard.ScoreboardActivity
import nl.inholland.myvitality.ui.search.SearchActivity
import nl.inholland.myvitality.ui.splash.SplashScreenActivity
import nl.inholland.myvitality.ui.timeline.liked.TimelineLikedActivity
import nl.inholland.myvitality.ui.timeline.overview.TimelineOverviewFragment
import nl.inholland.myvitality.ui.timelinepost.create.CreateTimelinePostActivity
import nl.inholland.myvitality.ui.timelinepost.view.TimelinePostActivity
import nl.inholland.myvitality.ui.tutorial.TutorialActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, NetworkModule::class, ViewModelModule::class])
interface ApplicationComponent {
    fun inject(activity: SplashScreenActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: RegisterDetailsActivity)
    fun inject(activity: RegisterAdditionalDetailsActivity)
    fun inject(activity: MainActivity)
    fun inject(fragment: HomeFragment)
    fun inject(overviewFragment: TimelineOverviewFragment)
    fun inject(activity: ActivityDetailActivity)
    fun inject(activity: TimelinePostActivity)
    fun inject(activity: CreateTimelinePostActivity)
    fun inject(activity: SearchActivity)
    fun inject(activity: TimelineLikedActivity)
    fun inject(activity: ProfileActivity)
    fun inject(activity: NotificationActivity)
    fun inject(activity: ScoreboardActivity)
    fun inject(activity: TutorialActivity)
    fun inject(activity: ProfileEditActivity)
    fun inject(activity: ActivityParticipantsActivity)
    fun inject(activity: ActivityOverviewActivity)
    fun inject(activity: AchievementActivity)
    fun inject(pushService: PushService)
}