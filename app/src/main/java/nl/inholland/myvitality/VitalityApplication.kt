package nl.inholland.myvitality

import android.app.Application
import nl.inholland.myvitality.architecture.ApplicationComponent
import nl.inholland.myvitality.architecture.DaggerApplicationComponent

class VitalityApplication : Application() {

    val appComponent: ApplicationComponent by lazy {
        initComponent()
    }

    private fun initComponent(): ApplicationComponent {
        return DaggerApplicationComponent.create()
    }
}