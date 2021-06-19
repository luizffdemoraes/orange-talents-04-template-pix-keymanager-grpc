package br.com.zup.luiz.interceptor

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Type(ExceptionHandlerInterceptor::class)
@Around
annotation class ErrorHandler