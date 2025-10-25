package ru.dan.hotel.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtUtil {
    private val secret: SecretKey = Keys.hmacShaKeyFor("your-256-bit-secret-key-1234567890abcdef".toByteArray())

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser().verifyWith(secret).build().parseSignedClaims(token)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getUsernameFromToken(token: String): String {
        val claims: Claims = Jwts.parser().verifyWith(secret).build().parseSignedClaims(token).payload
        return claims.subject
    }

    fun getRolesFromToken(token: String): List<String> {
        val claims: Claims = Jwts.parser().verifyWith(secret).build().parseSignedClaims(token).payload
        @Suppress("UNCHECKED_CAST")
        return claims["roles"] as List<String>
    }
}