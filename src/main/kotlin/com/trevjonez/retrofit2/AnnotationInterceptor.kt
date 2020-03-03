package com.trevjonez.retrofit2

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import kotlin.reflect.KClass

abstract class AnnotationInterceptor<A : Annotation>(
    val annotationType: KClass<A>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val invocationTag = request.tag(Invocation::class.java)
        val methodAnnotation = invocationTag?.method()?.getAnnotation(annotationType.java)
        return if (methodAnnotation != null) intercept(chain, methodAnnotation)
        else chain.proceed(request)
    }

    abstract fun intercept(chain: Interceptor.Chain, annotation: A): Response
}