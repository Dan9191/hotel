package ru.dan.hotel.controller

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.dan.hotel.model.AvailabilityRequest
import ru.dan.hotel.model.RoomDto
import ru.dan.hotel.service.RoomService

@RestController
@RequestMapping("/api/rooms")
class RoomController(
    private val roomService: RoomService
) {
    private val logger = LoggerFactory.getLogger(RoomController::class.java)

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRoom(
        @Valid @RequestBody dto: RoomDto,
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<RoomDto> {
        return roomService.createRoom(dto, correlationId)
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun getAvailableRooms(
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Flux<RoomDto> {
        return roomService.getAvailableRooms(correlationId)
    }

    @GetMapping("/recommend")
    @PreAuthorize("hasRole('USER')")
    fun getRecommendedRooms(
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Flux<RoomDto> {
        return roomService.getRecommendedRooms(correlationId)
    }

    @PostMapping("/{id}/confirm-availability")
    @PreAuthorize("hasRole('BOOKING_SERVICE')")
    fun confirmAvailability(
        @PathVariable id: Long,
        @Valid @RequestBody request: AvailabilityRequest,
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<Boolean> {
        return roomService.confirmAvailability(id, request, correlationId)
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('BOOKING_SERVICE')")
    fun releaseAvailability(
        @PathVariable id: Long,
        @RequestBody requestId: String,
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<Void> {
        return roomService.releaseAvailability(id, requestId, correlationId)
    }
}