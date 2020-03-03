package com.trevjonez.retrofit2

import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.GET

class AnnotationInterceptorTest {

    val httpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(ReplaceBaseUrl.Interceptor())
            .build()
    }

    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://SomethingUseless.com/foo/bar/")
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    val testService by lazy { retrofit.create<TestService>() }

    @Test
    fun `base url gets replaced`() {
        val error = assertThrows<IllegalArgumentException> {
            testService.getNothing().execute()
        }


        assertEquals("unexpected host: https://SomethingUseful.com/", error.message)
    }
}

interface TestService {

    @GET("/nothing")
    @ReplaceBaseUrl("https://SomethingUseful.com/")
    fun getNothing(): Call<String>
}

annotation class ReplaceBaseUrl(val value: String) {

    class Interceptor : AnnotationInterceptor<ReplaceBaseUrl>(ReplaceBaseUrl::class) {
        override fun intercept(chain: Chain, annotation: ReplaceBaseUrl): Response {
            val request = chain.request()
            val newUrl = request.url()
                .newBuilder()
                .host(annotation.value)
                .build()

            val newRequest = request.newBuilder()
                .url(newUrl)
                .build()

            return chain.proceed(newRequest)
        }
    }
}