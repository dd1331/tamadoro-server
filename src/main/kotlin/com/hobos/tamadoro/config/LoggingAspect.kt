package com.hobos.tamadoro.config

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.UUID

@Aspect
@Component
class LoggingAspect {
    private val log = LoggerFactory.getLogger(LoggingAspect::class.java)

    @Around("execution(* com.hobos.tamadoro.api..*(..)) || execution(* com.hobos.tamadoro.application..*(..)) || execution(* com.hobos.tamadoro.domain..*(..))")
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = "${joinPoint.signature.declaringType.simpleName}.${joinPoint.signature.name}"
        val requestId = MDC.get("requestId") ?: UUID.randomUUID().toString().also { MDC.put("requestId", it) }
        val currentUser = tryGetCurrentUserId()
        currentUser?.let { MDC.put("userId", it) }

        val argsPreview = joinPoint.args.joinToString(prefix = "[", postfix = "]") { arg ->
            when (arg) {
                is String, is Number, is Boolean -> arg.toString()
                is UUID -> arg.toString()
                else -> arg?.javaClass?.simpleName ?: "null"
            }
        }

        val start = System.currentTimeMillis()
        log.info("→ {} args={} reqId={} userId={}", methodSignature, argsPreview, requestId, currentUser)
        return try {
            val result = joinPoint.proceed()
            val took = System.currentTimeMillis() - start
            log.info("← {} took={}ms reqId={} userId={}", methodSignature, took, requestId, currentUser)
            result
        } catch (ex: Throwable) {
            val took = System.currentTimeMillis() - start
            log.error("× {} failed after {}ms reqId={} userId={} err={}", methodSignature, took, requestId, currentUser, ex.toString())
            throw ex
        } finally {
            // do not clear requestId here; it may be reused within the same thread for further logs
        }
    }

    private fun tryGetCurrentUserId(): String? = try {
        val auth = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication
        auth?.name
    } catch (_: Exception) {
        null
    }
}
