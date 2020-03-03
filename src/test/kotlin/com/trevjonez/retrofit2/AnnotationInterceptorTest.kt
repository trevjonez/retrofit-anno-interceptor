package com.trevjonez.retrofit2

import okhttp3.ConnectionSpec
import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT))
            .build()
    }

    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://a.b.c.d.q.w.e.r.t.y.com/foo/bar/")
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    val testService by lazy { retrofit.create<TestService>() }

    val mockServer by lazy { MockWebServer() }

    @Test
    fun `base url gets replaced`() {
        mockServer.enqueue(MockResponse().apply {
            setResponseCode(503)
            setBody("boom")
        })
        mockServer.start(10753)

        val result = testService.getNothing().execute()
        assertEquals("Server Error", result.message())
        assertEquals(503, result.code())

        //validate the mock server got the request proving the interceptor did its job
        val request = mockServer.takeRequest()
        assertEquals("/foo/bar/nothing/", request.path)
    }
}

interface TestService {

    @GET("nothing/")
    @ReplaceBaseUrl("127.0.0.1", 10753)
    fun getNothing(): Call<String>
}

annotation class ReplaceBaseUrl(val host: String, val port: Int = -1) {

    class Interceptor : AnnotationInterceptor<ReplaceBaseUrl>(ReplaceBaseUrl::class) {
        override fun intercept(chain: Chain, annotation: ReplaceBaseUrl): Response {
            val request = chain.request()

            val newUrl = request.url
                .newBuilder()
                .host(annotation.host)
                .port(annotation.port)
                .build()

            val newRequest = request.newBuilder()
                .url(newUrl)
                .build()

            return chain.proceed(newRequest)
        }
    }
}