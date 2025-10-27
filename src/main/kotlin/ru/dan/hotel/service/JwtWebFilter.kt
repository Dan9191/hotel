package ru.dan.hotel.service

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*

@Component
class JwtWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val correlationId = exchange.request.headers.getFirst("X-Correlation-Id") ?: UUID.randomUUID().toString()

        return chain.filter(exchange)
            .contextWrite { ctx -> ctx.put("correlationId", correlationId) }
    }
}