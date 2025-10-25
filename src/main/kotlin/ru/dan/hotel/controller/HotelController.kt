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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createHotel(
        @Valid @RequestBody dto: HotelDto,
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<HotelDto> {
        return hotelService.createHotel(dto, correlationId)
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun getAllHotels(
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Flux<HotelDto> {
        return hotelService.getAllHotels(correlationId)
    }
}