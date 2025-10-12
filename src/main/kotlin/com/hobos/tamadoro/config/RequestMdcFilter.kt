package com.hobos.tamadoro.config

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestMdcFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(RequestMdcFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val existing = MDC.get("requestId")
        val requestId = existing ?: request.getHeader("X-Request-Id") ?: UUID.randomUUID().toString()
        MDC.put("requestId", requestId)
        val method = request.method
        val path = request.requestURI
        val query = request.queryString?.let { "?$it" } ?: ""
        val start = System.currentTimeMillis()
        log.info("↘ {} {}{} reqId={}", method, path, query, requestId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            val took = System.currentTimeMillis() - start
            log.info("↗ {} {}{} status={} took={}ms reqId={}", method, path, query, response.status, took, requestId)
            MDC.remove("requestId")
            MDC.remove("userId")
        }
    }
}
