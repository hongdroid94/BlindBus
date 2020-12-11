package com.salus.blindbus.creator

import androidx.viewbinding.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 레트로 핏을 호출하기 위한 크리에이터 정의 (API 를 바로 호출할 수 있도록 설정해주는 클래스
 */
class RetrofitCreator {
    companion object {
        private const val API_BASE_URL = "http://3.34.61.169"
        private var retrofitClient: Retrofit? = null

        private fun defaultRetrofit(): Retrofit? {
            if(retrofitClient == null) {
                retrofitClient = Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(createOkHttpClient())
                    .build()
            }
            return retrofitClient
        }

        fun <T> create(service: Class<T>): T {
            return defaultRetrofit()!!.create(service)
        }

        private fun createOkHttpClient(): OkHttpClient {
            val interceptor = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                interceptor.level = HttpLoggingInterceptor.Level.BODY
            } else {
                interceptor.level = HttpLoggingInterceptor.Level.NONE
            }

            return OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build()
        }
    }
}