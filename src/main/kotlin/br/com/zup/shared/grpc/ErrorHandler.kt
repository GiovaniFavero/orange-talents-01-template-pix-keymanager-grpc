package br.com.zup.shared.grpc

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationTarget.*

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(CLASS, FILE, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Around //kotlin vai tornar classe open
@Type(ExceptionHandlerInterceptor::class)
annotation class ErrorHandler()
