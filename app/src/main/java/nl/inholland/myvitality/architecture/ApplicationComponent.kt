package nl.inholland.myvitality.architecture

import dagger.Component
import nl.inholland.myvitality.modules.NetworkModule
import nl.inholland.myvitality.ui.authentication.login.LoginActivity
import nl.inholland.myvitality.ui.authentication.register.RegisterActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface ApplicationComponent {
    fun inject(activity: LoginActivity)
    fun inject(activity: RegisterActivity)
}