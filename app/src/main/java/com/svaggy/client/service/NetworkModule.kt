package com.svaggy.client.service

import com.svaggy.BuildConfig
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Connection timeout
            .readTimeout(30, TimeUnit.SECONDS)     // Read timeout
            .writeTimeout(30, TimeUnit.SECONDS)    // Write timeout
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Version", BuildConfig.VERSION_NAME)
                    .addHeader("DeviceType", "ANDROID")
                    .addHeader("DeviceToken",PrefUtils.instance.getString(Constants.DeviceToken).toString())
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @Singleton
    @Provides
    fun providesRetrofit():Retrofit{
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.BASE_URL)
            .build()
    }

    @Singleton
    @Provides
    fun providesUserApi(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}