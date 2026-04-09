package com.iksystem.common.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RateLimitFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(RateLimitFilter::class.java)
    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    companion object {
        private const val GLOBAL_REQUESTS_PER_SECOND = 20
        private const val LOGIN_REQUESTS_PER_SECOND = 3
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val ip = request.remoteAddr ?: "unknown"
        val path = request.requestURI
        val isLogin = path == "/api/v1/auth/login" && request.method == "POST"

        val key = if (isLogin) "login:$ip" else "global:$ip"
        val limit = if (isLogin) LOGIN_REQUESTS_PER_SECOND else GLOBAL_REQUESTS_PER_SECOND

        val bucket = buckets.computeIfAbsent(key) { TokenBucket(limit) }

        if (!bucket.tryConsume()) {
            log.warn("Rate limit exceeded: ip={}, path={}", ip, path)
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write("""{"error":{"code":"rate_limited","message":"Too many requests. Try again later."}}""")
            return
        }

        filterChain.doFilter(request, response)
    }

    private class TokenBucket(private val maxTokens: Int) {
        private var tokens = AtomicInteger(maxTokens)
        @Volatile private var lastRefill = System.currentTimeMillis()

        @Synchronized
        fun tryConsume(): Boolean {
            refill()
            return if (tokens.get() > 0) {
                tokens.decrementAndGet()
                true
            } else {
                false
            }
        }

        private fun refill() {
            val now = System.currentTimeMillis()
            val elapsed = now - lastRefill
            if (elapsed >= 1000) {
                val secondsPassed = (elapsed / 1000).toInt()
                val newTokens = (tokens.get() + secondsPassed * maxTokens).coerceAtMost(maxTokens)
                tokens.set(newTokens)
                lastRefill = now
            }
        }
    }
}
