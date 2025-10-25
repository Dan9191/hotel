package ru.dan.hotel.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.dan.hotel.mapper.RoomMapper
import ru.dan.hotel.model.AvailabilityRequest
import ru.dan.hotel.model.RoomDto
import ru.dan.hotel.repository.HotelRepository
import ru.dan.hotel.repository.RoomRepository
import java.util.NoSuchElementException

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val hotelRepository: HotelRepository,
    private val roomMapper: RoomMapper,
    private val webClient: WebClient
) {
    private val logger = LoggerFactory.getLogger(RoomService::class.java)

    fun createRoom(dto: RoomDto, correlationId: String): Mono<RoomDto> {
        logger.info("[$correlationId] Creating room: ${dto.number} for hotel: ${dto.hotelId}")
        return hotelRepository.findById(dto.hotelId)
            .switchIfEmpty(Mono.error(NoSuchElementException("Hotel with id ${dto.hotelId} not found")))
            .flatMap { hotel ->
                val room = roomMapper.toEntity(dto).copy(hotelId = hotel.id!!)
                roomRepository.save(room)
            }
            .map { roomMapper.toDto(it) }
    }

    fun getAvailableRooms(correlationId: String): Flux<RoomDto> {
        logger.info("[$correlationId] Fetching available rooms")
        return roomRepository.findByAvailableTrue()
            .map { roomMapper.toDto(it) }
    }

    fun getRecommendedRooms(correlationId: String): Flux<RoomDto> {
        logger.info("[$correlationId] Fetching recommended rooms")
        return roomRepository.findRecommendedRooms()
            .map { roomMapper.toDto(it) }
    }

    fun confirmAvailability(roomId: Long, request: AvailabilityRequest, correlationId: String): Mono<Boolean> {
        logger.info("[$correlationId] Confirming availability for room: $roomId, requestId: ${request.requestId}")
        return roomRepository.findById(roomId)
            .switchIfEmpty(Mono.error(NoSuchElementException("Room with id $roomId not found")))
            .flatMap { room ->
                if (!room.available) {
                    logger.warn("[$correlationId] Room $roomId is not available")
                    Mono.error(IllegalStateException("Room is not available"))
                } else {
                    webClient.post()
                        .uri("lb://booking-service/api/bookings/check-availability")
                        .header("X-Correlation-Id", correlationId)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(Boolean::class.java)
                        .doOnError { e ->
                            logger.error("[$correlationId] Error checking availability: ${e.message}")
                            if (e is WebClientResponseException && e.statusCode.value() == 409) {
                                throw IllegalStateException("Room is already booked for these dates")
                            }
                        }
                        .timeout(java.time.Duration.ofSeconds(5))
                        .retryWhen(reactor.util.retry.Retry.backoff(3, java.time.Duration.ofMillis(500)))
                }
            }
    }

    fun releaseAvailability(roomId: Long, requestId: String, correlationId: String): Mono<Void> {
        logger.info("[$correlationId] Releasing availability for room: $roomId, requestId: $requestId")
        // Проверка идемпотентности (заглушка)
        return Mono.empty()
    }
}