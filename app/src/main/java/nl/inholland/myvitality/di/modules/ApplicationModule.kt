package nl.inholland.myvitality.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import nl.inholland.myvitality.util.SharedPreferenceHelper
import nl.inholland.myvitality.VitalityApplication
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: VitalityApplication) {

    @Provides
    @Singleton
    fun provideApplicationContext(): Context = application

    @Singleton
    @Provides
    fun provideSharedPreferences(): SharedPreferenceHelper =
        SharedPreferenceHelper(application.applicationContext)

}