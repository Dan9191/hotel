package ru.dan.hotel.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange {
                it.pathMatchers("/actuator/**", "/hi").permitAll()
                it.pathMatchers("/api/hotels").hasRole("USER")
                it.pathMatchers("/api/rooms", "/api/rooms/recommend").hasRole("USER")
                it.pathMatchers("/api/hotels/**").hasRole("ADMIN")
                it.pathMatchers("/api/rooms/**").hasAnyRole("ADMIN", "BOOKING_SERVICE")
                it.anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                }
            }
            .build()
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        val secret = "your-256-bit-secret-key-1234567890abcdef"
        val keyBytes = secret.toByteArray()
        val secretKey = SecretKeySpec(keyBytes, "HmacSHA256")
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }

    private fun grantedAuthoritiesExtractor(): ReactiveJwtAuthenticationConverterAdapter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val roles = jwt.claims["roles"] as? List<String> ?: run {
                logger.warn("No roles found in JWT claims")
                emptyList()
            }
            roles.map { SimpleGrantedAuthority("ROLE_$it") }
        }
        return ReactiveJwtAuthenticationConverterAdapter(converter)
    }
}