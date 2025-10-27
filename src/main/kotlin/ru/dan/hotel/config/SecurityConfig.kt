package ru.dan.hotel.config

import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
            .csrf { it.disable() } // Отключаем CSRF для REST API
            .authorizeExchange {
                it
                    // Разрешаем доступ к OpenAPI и Swagger UI без аутентификации
                    .pathMatchers("/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**").permitAll()
                    // Существующий разрешенный путь
                    .pathMatchers("/api/hotels/hi").permitAll()
                    // Эндпоинты для GET-запросов (доступны для USER, BOOKING_SERVICE, ADMIN)
                    .pathMatchers(HttpMethod.GET, "/api/hotels").hasAnyAuthority("ROLE_USER", "ROLE_BOOKING_SERVICE", "ROLE_ADMIN")
                    .pathMatchers(HttpMethod.GET, "/api/rooms").hasAnyAuthority("ROLE_USER", "ROLE_BOOKING_SERVICE", "ROLE_ADMIN")
                    .pathMatchers(HttpMethod.GET, "/api/rooms/recommend").hasAnyAuthority("ROLE_USER", "ROLE_BOOKING_SERVICE", "ROLE_ADMIN")
                    // Эндпоинты для POST-запросов (доступны для BOOKING_SERVICE, ADMIN)
                    .pathMatchers(HttpMethod.POST, "/api/hotels").hasAnyAuthority("ROLE_BOOKING_SERVICE", "ROLE_ADMIN")
                    .pathMatchers(HttpMethod.POST, "/api/rooms").hasAnyAuthority("ROLE_BOOKING_SERVICE", "ROLE_ADMIN")
                    .pathMatchers(HttpMethod.POST, "/api/rooms/*/confirm-availability").hasAnyAuthority("ROLE_BOOKING_SERVICE", "ROLE_ADMIN")
                    .pathMatchers(HttpMethod.POST, "/api/rooms/*/release-availability").hasAnyAuthority("ROLE_BOOKING_SERVICE", "ROLE_ADMIN")
                    // Все остальные запросы требуют аутентификации
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