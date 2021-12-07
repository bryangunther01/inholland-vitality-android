package nl.inholland.myvitality.architecture

import dagger.Component
import nl.inholland.myvitality.modules.NetworkModule
import javax.inject.Singleton

/**
 * Component providing inject() methods for presenters.
 */
@Singleton
@Component(modules = [(NetworkModule::class)])
interface ViewModelInjector {

    //fun inject(postListViewModel: TimelinePostViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector

        fun networkModule(networkModule: NetworkModule): Builder
    }
}