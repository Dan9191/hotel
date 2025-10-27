package ru.dan.hotel.config

import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import javax.crypto.SecretKey

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    private val secret: SecretKey = Keys.hmacShaKeyFor(
        "your-256-bit-secret-key-1234567890abcdef".toByteArray()
    )

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/api/hotels/hi").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth ->
                oauth.jwt { jwt -> jwt.jwtDecoder(jwtDecoder()) }
            }
            .build()
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder
            .withSecretKey(secret)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }
}