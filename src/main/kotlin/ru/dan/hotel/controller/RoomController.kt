package ru.dan.hotel.controller

import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.dan.hotel.model.AvailabilityRequest
import ru.dan.hotel.model.RoomDto
import ru.dan.hotel.service.RoomService
import java.time.LocalDate

@RestController
@RequestMapping("/api/rooms")
class RoomController(private val roomService: RoomService) {

    /**
     * Создание комнаты.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun createRoom(
        @Valid @RequestBody roomDto: RoomDto,
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<RoomDto> {
        return roomService.createRoom(roomDto, correlationId)
    }

    /**
     * Получение списка доступных комнат на указанные даты.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_BOOKING_SERVICE', 'ROLE_ADMIN')")
    fun getAvailableRooms(
        @RequestHeader("X-Correlation-Id") correlationId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): Flux<RoomDto> {
        return roomService.getAvailableRooms(correlationId, startDate, endDate)
    }

    /**
     * Получение списка рекомендуемых комнат на указанные даты.
     */
    @GetMapping("/recommend")
    @PreAuthorize("hasAnyRole('ROLE_BOOKING_SERVICE', 'ROLE_ADMIN')")
    fun getRecommendedRooms(
        @RequestHeader("X-Correlation-Id") correlationId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): Flux<RoomDto> {
        return roomService.getRecommendedRooms(correlationId, startDate, endDate)
    }

    /**
     * Бронирование комнаты (временная блокировка).
     */
    @PostMapping("/{id}/confirm-availability")
    @PreAuthorize("hasAnyRole('ROLE_BOOKING_SERVICE', 'ROLE_ADMIN')")
    fun confirmAvailability(
        @PathVariable id: Long,
        @Valid @RequestBody request: AvailabilityRequest,
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<Long> {
        return roomService.confirmAvailability(id, request, correlationId)
    }

    /**
     * Освобождение временной брони.
     */
    @PostMapping("/{id}/release-availability")
    @PreAuthorize("hasAnyRole('ROLE_BOOKING_SERVICE', 'ROLE_ADMIN')")
    fun releaseAvailability(
        @PathVariable id: Long,
        @RequestParam requestId: String,
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<Void> {
        return roomService.releaseAvailability(id, requestId, correlationId)
    }
}