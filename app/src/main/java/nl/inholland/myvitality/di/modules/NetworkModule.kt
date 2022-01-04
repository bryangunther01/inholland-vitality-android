package nl.inholland.myvitality.di.modules

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import nl.inholland.myvitality.BuildConfig
import nl.inholland.myvitality.architecture.enumadapter.EnumJsonAdapterFactory
import nl.inholland.myvitality.data.ApiClient
import nl.inholland.myvitality.data.AuthorizationInterceptor
import nl.inholland.myvitality.data.TokenAuthenticator
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Module
class NetworkModule(val context: Context) {
    private val BASE_URL: String = "https://vitalityfunctionsapp.azurewebsites.net/api/"

    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */
    @Provides
    @Singleton
    internal fun provideRetrofitInterface(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getHttpClient(TokenAuthenticator(context, provideTokenApi())))
            .addConverterFactory(getMoshiConverter())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiClient{
        return retrofit.create(ApiClient::class.java)
    }

    fun provideTokenApi(): ApiClient {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getHttpClient())
            .addConverterFactory(getMoshiConverter())
            .build()
            .create(ApiClient::class.java)
    }

    @Singleton
    private fun getHttpClient(authenticator: TokenAuthenticator? = null): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()

        clientBuilder.readTimeout(300, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(300, TimeUnit.SECONDS)
        clientBuilder.connectTimeout(300, TimeUnit.SECONDS)

        authenticator?.let {
            clientBuilder.authenticator(authenticator)
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