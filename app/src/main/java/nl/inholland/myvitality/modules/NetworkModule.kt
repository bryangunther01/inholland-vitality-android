package nl.inholland.myvitality.modules

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import nl.inholland.myvitality.BuildConfig
import nl.inholland.myvitality.data.ApiClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


@Module
class NetworkModule {
    private val baseUrl: String = "https://vitalityfunctions.azurewebsites.net/api/"

    @Provides
    @Singleton
    fun provideApiService(): ApiClient{
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getHttpClient())
            .addConverterFactory(getMoshiConverter())
            .build()
            .create(ApiClient::class.java)
    }

    @Singleton
    private fun getHttpClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()

        clientBuilder.readTimeout(300, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(300, TimeUnit.SECONDS)
        clientBuilder.connectTimeout(300, TimeUnit.SECONDS)

        if(BuildConfig.DEBUG){
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            clientBuilder.addInterceptor(logging)
        }

        return clientBuilder.build()
    }

    @Singleton
    private fun getMoshiConverter(): MoshiConverterFactory {
        return MoshiConverterFactory.create(
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory()).build())
    }
}