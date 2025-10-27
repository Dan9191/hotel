package ru.dan.hotel.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono

@RestControllerAdvice
class GlobalExceptionHandler {

    // Обработка IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to ex.message!!))
        )
    }

    // Обработка ошибок валидации (например, для @Valid)
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(ex: WebExchangeBindException): Mono<ResponseEntity<Map<String, String>>> {
        val errors = ex.bindingResult.allErrors
            .joinToString(", ") { it.defaultMessage ?: "Invalid input" }
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to errors))
        )
    }

    // Обработка других исключений (на случай непредвиденных ошибок)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "An unexpected error occurred"))
        )
    }
}