package ru.dan.hotel.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import ru.dan.hotel.entity.Booking
import java.time.LocalDate

interface BookingRepository : ReactiveCrudRepository<Booking, Long> {
    @Query("""
        SELECT * FROM bookings 
        WHERE room_id = :roomId 
        AND booking_type = 'TEMPORARY' 
        AND (start_date <= :endDate AND end_date >= :startDate)
    """)
    fun findOverlappingTemporaryBookings(
        roomId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Mono<Booking>

    @Query("SELECT * FROM bookings WHERE request_id = :requestId")
    fun findByRequestId(requestId: String): Mono<Booking>
}