package ru.dan.hotel.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono
import java.util.NoSuchElementException

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

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to ex.message!!))
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to ex.message!!))
        )
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(ex: WebExchangeBindException): Mono<ResponseEntity<Map<String, String>>> {
        val errors = ex.bindingResult.allErrors
            .joinToString(", ") { it.defaultMessage ?: "Неверный ввод" }
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to errors))
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): Mono<ResponseEntity<Map<String, String>>> {
        ex.printStackTrace() // Для отладки
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Произошла непредвиденная ошибка: ${ex.message}"))
        )
    }
}