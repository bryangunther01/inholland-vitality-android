package nl.inholland.myvitality.architecture

import dagger.Component
import nl.inholland.myvitality.di.modules.ApplicationModule
import nl.inholland.myvitality.di.modules.NetworkModule
import nl.inholland.myvitality.di.modules.ViewModelModule
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.authentication.recover.AccountRecoverActivity
import nl.inholland.myvitality.ui.authentication.register.details1.RegisterDetailsActivity
import nl.inholland.myvitality.ui.authentication.register.details2.RegisterAdditionalDetailsActivity
import nl.inholland.myvitality.ui.authentication.register.main.RegisterActivity
import nl.inholland.myvitality.ui.challenge.ChallengeActivity
import nl.inholland.myvitality.ui.challenge.participants.ChallengeParticipantsActivity
import nl.inholland.myvitality.ui.home.HomeFragment
import nl.inholland.myvitality.ui.notification.NotificationActivity
import nl.inholland.myvitality.ui.profile.edit.ProfileEditActivity
import nl.inholland.myvitality.ui.profile.overview.ProfileActivity
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
    fun inject(activity: RegisterActivity)
    fun inject(activity: RegisterDetailsActivity)
    fun inject(activity: RegisterAdditionalDetailsActivity)
    fun inject(activity: MainActivity)
    fun inject(fragment: HomeFragment)
    fun inject(overviewFragment: TimelineOverviewFragment)
    fun inject(activity: ChallengeActivity)
    fun inject(activity: TimelinePostActivity)
    fun inject(activity: CreateTimelinePostActivity)
    fun inject(activity: SearchActivity)
    fun inject(activity: TimelineLikedActivity)
    fun inject(activity: ProfileActivity)
    fun inject(activity: NotificationActivity)
    fun inject(activity: ScoreboardActivity)
    fun inject(activity: TutorialActivity)
    fun inject(activity: ProfileEditActivity)
    fun inject(activity: ChallengeParticipantsActivity)
    fun inject(activity: AccountRecoverActivity)
}