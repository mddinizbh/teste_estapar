package com.marley.parking.adapter.inbound.logging

import com.marley.parking.adapter.inbound.dto.WebhookEventDto
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import org.slf4j.MDC

@Singleton
@InterceptorBean(LogContext::class)
class LogContextInterceptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        val event = context.parameterValues
            .filterIsInstance<WebhookEventDto>()
            .firstOrNull()

        try {
            event?.let {
                MDC.put("plate", it.license_plate)
                MDC.put("event_type", it.event_type)
            }
            return context.proceed()
        } finally {
            MDC.clear()
        }
    }
}
