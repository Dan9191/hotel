package ru.dan.hotel.controller

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.dan.hotel.model.HotelDto
import ru.dan.hotel.service.HotelService

@RestController
@RequestMapping("/api/hotels")
class HotelController(
    private val hotelService: HotelService
) {
    private val logger = LoggerFactory.getLogger(HotelController::class.java)

    /**
     * Добавление отеля.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_BOOKING_SERVICE', 'ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createHotel(
        @Valid @RequestBody dto: HotelDto,

        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<HotelDto> {
        return hotelService.createHotel(dto, correlationId)
    }


    /**
     * Список отелей.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_BOOKING_SERVICE', 'ROLE_USER', 'ROLE_ADMIN')")
    fun getAllHotels(
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Flux<HotelDto> {
        return hotelService.getAllHotels(correlationId)
    }

    /**
     * Тестовый метод.
     */
    @GetMapping("/hi")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    fun hi(
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<String> {
        return Mono.just("Hello from hotel service")
    }

}