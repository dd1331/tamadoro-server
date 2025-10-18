package com.hobos.tamadoro.config

import com.hobos.tamadoro.domain.auth.AuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

class JwtAuthenticationFilter(
    private val authService: AuthService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
        val bearerPrefix = "Bearer "

        if (authorization?.startsWith(bearerPrefix) == true) {
            val token = authorization.substring(bearerPrefix.length)
            try {
                val userId: UUID = authService.validateToken(token)

                val authentication = UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER"))
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
                
                filterChain.doFilter(request, response)
                return
            } catch (_: Exception) {
                // Token is invalid or expired - return 401 Unauthorized
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.writer.write("{\"error\":\"Unauthorized\",\"message\":\"Invalid or expired token\"}")
                return
            }
        } else if (authorization != null) {
            // Authorization header exists but doesn't start with "Bearer "
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("{\"error\":\"Unauthorized\",\"message\":\"Invalid authorization header format\"}")
            return
        }

        filterChain.doFilter(request, response)
    }
}


