package nl.inholland.myvitality.di.modules

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import nl.inholland.myvitality.BuildConfig
import nl.inholland.myvitality.architecture.enumadapter.EnumJsonAdapterFactory
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.TokenInterceptor
import nl.inholland.myvitality.data.TokenApiClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule(val context: Context) {
    private val BASE_URL: String = "https://vitalityfunctionsapp.azurewebsites.net/api/"

    @Provides
    @Singleton
    fun provideApiService(): ApiClient{
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getHttpClient())
            .addConverterFactory(getMoshiConverter())
            .build()
            .create(ApiClient::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenApiService(): TokenApiClient {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getHttpClient(false))
            .addConverterFactory(getMoshiConverter())
            .build()
            .create(TokenApiClient::class.java)
    }

    @Singleton
    private fun getHttpClient(withInterceptor: Boolean? = true): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()

        clientBuilder.readTimeout(300, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(300, TimeUnit.SECONDS)
        clientBuilder.connectTimeout(300, TimeUnit.SECONDS)

        if(withInterceptor == true){
            clientBuilder.addInterceptor(TokenInterceptor(context, provideTokenApiService()))
        }

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
                .add(KotlinJsonAdapterFactory())
                .add(EnumJsonAdapterFactory)
                .build())
    }
}