package nl.inholland.myvitality.architecture.base

//abstract class BaseViewModel: ViewModel(){
//    private val injector: ViewModelInjector = DaggerViewModelInjector
//        .builder()
//        .networkModule(NetworkModule)
//        .build()
//
//    init {
//        inject()
//    }
//
//    /**
//     * Injects the required dependencies
//     */
//    private fun inject() {
//        when (this) {
//            is TimelinePostViewModel -> injector.inject(this)
//        }
//    }
//}