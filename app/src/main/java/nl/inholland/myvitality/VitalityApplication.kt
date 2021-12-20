package nl.inholland.myvitality

import android.app.Application
import nl.inholland.myvitality.architecture.ApplicationComponent
import nl.inholland.myvitality.architecture.DaggerApplicationComponent
import nl.inholland.myvitality.di.modules.ApplicationModule

class VitalityApplication : Application() {

    val appComponent: ApplicationComponent by lazy {
        initComponent()
    }

    private fun initComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }
}