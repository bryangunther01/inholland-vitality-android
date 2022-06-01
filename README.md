# Welcome the the MyVitality Android App
This application is part of a vitality program of Inholland Alkmaar. The application is written in kotlin.
## Project setup
Clone this repository and import into **Android Studio**, hit **Run**.
## Understanding the Application Structure
The application makes use of Dagger2 as dependency injection library following the structure shown below.
* The ApplicationModule together with the ApplicationComponent are responsible for the injection of the context and the sharedpreferences
* The NetworkModule handles all API related matters
* The ViewModelModule handles the registration of ViewModels
  ![dagger](https://vitalityappv2tst.blob.core.windows.net/images/android_dagger.drawio.png)

Every activity and fragment collects it's data from a viewmodel which communicates with the designated (Token)ApiClient. The ViewModel stores the retreived data and the activity/fragment observes if there is any new data to show in the activity/fragment.
![dagger](https://vitalityappv2tst.blob.core.windows.net/images/android_activity_structure.drawio.png)


## Register a new fragment or activity
For fragments and activities there are baseclasses with view binding implemented. When you create a new fragment or activity you have to follow some steps described below.

## Inject fragment/activity
To register the fragment or activity to the appcomponent you need to add the following lines to your fragment/activity in the `onCreate`.
#### Activity
```(application as VitalityApplication).appComponent.inject(this) ```

#### Fragment
```(requireActivity().application as VitalityApplication).appComponent.inject(this)```

You also need to create the inject method for you activity or fragment in the app component as shown below.
```fun inject(activity: YourActivity)```

## View Binding
All of the base classes have viewbinding, but you have to register it before you can use it.
On top of your class you extend the base class like this ```BaseActivity<YourBinding>()```.
Afterwards you need override the bindingInflator of the base class by adding the following to your fragment/activity.
#### Activity
```
override val bindingInflater: (LayoutInflater) -> YourBinding
        = YourBinding::inflate
```
#### Fragment
```
override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> YourBinding
        = YourBinding::inflate
```

## View Models
The project makes use of a MVVM structure which means every activity or fragment has a viewmodel class. To register a viewmodel class make sure you add the following code to the `ViewModelModule.kt`:
```
@Provides  
fun provideYourViewModelFactory(apiClient: ApiClient, sharedPres: SharedPreferenceHelper): YourViewModel{  
    return YourViewModel(apiClient, sharedPres)  
}
```

After this you only need to register the viewmodel in your activity/fragment to make use of it by adding the following code:

```
@Inject  
lateinit var factory: YourViewModelFactory
lateinit var viewModel: YourViewModel

....

override fun onCreate(savedInstanceState: Bundle?) {  
    super.onCreate(savedInstanceState)  
  
    viewModel = ViewModelProviders.of(this, factory).get(YourViewModel::class.java)  
}

```


## External dependencies
* [Dagger2](https://github.com/google/dagger) for dependency injection
* [Retrofit](https://github.com/square/retrofit) as HTTP client in combination with OkHttp
* [OkHttp](https://github.com/square/okhttp) as HTTP client in combination with Retrofit
* [Firebase Messaging](https://github.com/firebase/quickstart-android/tree/master/messaging) for push notifications
* [Firebase Dynamic Links](https://github.com/firebase/firebase-android-sdk/tree/master/firebase-dynamic-links) for deeplinking
* [MSAL](https://github.com/AzureAD/microsoft-authentication-library-for-android) for Microsoft AD authentication
* [Glide](https://github.com/bumptech/glide) for image loading
* [Skeleton](https://github.com/ethanhua/Skeleton) for skeleton loading
* [MutliSelectSpinner](https://github.com/telichada/SearchableMultiSelectSpinner)

