package ru.dan.hotel.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.dan.hotel.entity.Booking
import ru.dan.hotel.entity.BookingType
import ru.dan.hotel.mapper.RoomMapper
import ru.dan.hotel.model.AvailabilityRequest
import ru.dan.hotel.model.RoomDto
import ru.dan.hotel.repository.BookingRepository
import ru.dan.hotel.repository.HotelRepository
import ru.dan.hotel.repository.RoomRepository
import java.time.LocalDate
import java.util.NoSuchElementException

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val hotelRepository: HotelRepository,
    private val bookingRepository: BookingRepository,
    private val roomMapper: RoomMapper
) {
    private val logger = LoggerFactory.getLogger(RoomService::class.java)

    @Transactional
    fun createRoom(dto: RoomDto, correlationId: String): Mono<RoomDto> {
        logger.info("[$correlationId] Создание комнаты: ${dto.number} для отеля: ${dto.hotelId}")
        return hotelRepository.findById(dto.hotelId)
            .switchIfEmpty(Mono.error(NoSuchElementException("Отель с id ${dto.hotelId} не найден")))
            .flatMap { hotel ->
                val room = roomMapper.toEntity(dto).copy(hotelId = hotel.id!!)
                roomRepository.save(room)
            }
            .map { roomMapper.toDto(it) }
    }

    @Transactional
    fun getAvailableRooms(correlationId: String, startDate: LocalDate, endDate: LocalDate): Flux<RoomDto> {
        logger.info("[$correlationId] Получение доступных комнат на даты $startDate - $endDate")
        return roomRepository.findAvailableRooms(startDate, endDate)
            .map { roomMapper.toDto(it) }
    }

    @Transactional
    fun getRecommendedRooms(correlationId: String, startDate: LocalDate, endDate: LocalDate): Flux<RoomDto> {
        logger.info("[$correlationId] Получение рекомендуемых комнат на даты $startDate - $endDate")
        return roomRepository.findRecommendedRooms(startDate, endDate)
            .map { roomMapper.toDto(it) }
    }

    @Transactional
    fun confirmAvailability(roomId: Long, request: AvailabilityRequest, correlationId: String): Mono<Long> {
        logger.info("[$correlationId] Проверка доступности комнаты: $roomId, requestId: ${request.requestId}")
        return roomRepository.findById(roomId)
            .switchIfEmpty(Mono.error(NoSuchElementException("Комната с id $roomId не найдена")))
            .flatMap {
                // Проверка на идемпотентность
                bookingRepository.findByRequestId(request.requestId)
                    .flatMap { existingBooking ->
                        logger.info("[$correlationId] Бронь уже существует для requestId: ${request.requestId}")
                        if (existingBooking.roomId != roomId || existingBooking.bookingType != BookingType.TEMPORARY) {
                            Mono.error<Long>(IllegalStateException("Существующая бронь не соответствует комнате или типу"))
                        } else {
                            Mono.just(existingBooking.id!!)
                        }
                    }
                    .switchIfEmpty(
                        // Проверка на пересечение броней (включая постоянные)
                        bookingRepository.findOverlappingBookings(
                            roomId = roomId,
                            startDate = request.startDate,
                            endDate = request.endDate
                        ).flatMap<Long> {
                            logger.warn("[$correlationId] Комната $roomId уже забронирована на эти даты")
                            Mono.error(IllegalStateException("Комната уже забронирована на эти даты"))
                        }.switchIfEmpty(
                            // Создание временной брони
                            bookingRepository.save(
                                Booking(
                                    roomId = roomId,
                                    requestId = request.requestId,
                                    startDate = request.startDate,
                                    endDate = request.endDate,
                                    bookingType = BookingType.TEMPORARY
                                )
                            ).map { it.id!! }
                        )
                    )
            }
    }

    @Transactional
    fun releaseAvailability(roomId: Long, requestId: String, correlationId: String): Mono<Void> {
        logger.info("[$correlationId] Освобождение брони для комнаты: $roomId, requestId: $requestId")
        return bookingRepository.findByRequestId(requestId)
            .flatMap { booking ->
                if (booking.roomId != roomId || booking.bookingType != BookingType.TEMPORARY) {
                    logger.warn("[$correlationId] Неверный запрос на освобождение для комнаты $roomId, requestId: $requestId")
                    Mono.error(IllegalStateException("Нельзя освободить: неверная комната или тип брони"))
                } else {
                    bookingRepository.deleteById(booking.id!!)
                }
            }
            .switchIfEmpty(
                Mono.fromRunnable { logger.info("[$correlationId] Бронь не найдена для requestId: $requestId, идемпотентное освобождение") }
            )
    }
}