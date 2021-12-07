package nl.inholland.myvitality.architecture

import dagger.Component
import nl.inholland.myvitality.modules.NetworkModule
import nl.inholland.myvitality.ui.challenge.ChallengeActivity
import nl.inholland.myvitality.ui.home.HomeFragment
import nl.inholland.myvitality.ui.MainActivity
import nl.inholland.myvitality.ui.timelinepost.TimelinePostActivity
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.authentication.register.RegisterActivity
import nl.inholland.myvitality.ui.authentication.register.RegisterDetails2Activity
import nl.inholland.myvitality.ui.authentication.register.RegisterDetailsActivity
import nl.inholland.myvitality.ui.search.SearchActivity
import javax.inject.Singleton

import nl.inholland.myvitality.ui.timeline.TimelineFragment
import nl.inholland.myvitality.ui.splash.SplashScreenActivity
import nl.inholland.myvitality.ui.timelinepost.create.CreateTimelinePostActivity


@Singleton
@Component(modules = [NetworkModule::class])
interface ApplicationComponent {
    fun inject(activity: SplashScreenActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: RegisterActivity)
    fun inject(activity: RegisterDetailsActivity)
    fun inject(activity: RegisterDetails2Activity)
    fun inject(activity: MainActivity)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: TimelineFragment)
    fun inject(activity: ChallengeActivity)
    fun inject(activity: TimelinePostActivity)
    fun inject(activity: CreateTimelinePostActivity)
    fun inject(activity: SearchActivity)
}