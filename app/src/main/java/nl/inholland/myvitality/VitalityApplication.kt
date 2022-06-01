package nl.inholland.myvitality

import android.app.Application
import nl.inholland.myvitality.architecture.ApplicationComponent
import nl.inholland.myvitality.architecture.DaggerApplicationComponent
import nl.inholland.myvitality.di.modules.ApplicationModule
import nl.inholland.myvitality.di.modules.NetworkModule

class VitalityApplication : Application() {

    val appComponent: ApplicationComponent by lazy {
        initComponent()
    }

    /**
     * Initialize the Application Component so dagger knows what to register
     */
    private fun initComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .networkModule(NetworkModule(this))
            .build()
    }
}