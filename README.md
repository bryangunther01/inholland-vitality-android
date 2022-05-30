# Welcome the the MyVitality Android App
This application is part of a vitality program of Inholland Alkmaar.
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

