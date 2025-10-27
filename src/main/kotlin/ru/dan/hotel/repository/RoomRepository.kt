package ru.dan.hotel.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import ru.dan.hotel.entity.Room
import java.time.LocalDate

interface RoomRepository : ReactiveCrudRepository<Room, Long> {

    @Query("""
        SELECT r.* FROM rooms r
        WHERE NOT EXISTS (
            SELECT 1 FROM bookings b
            WHERE b.room_id = r.id
            AND b.start_date <= :endDate
            AND b.end_date >= :startDate
        )
    """)
    fun findAvailableRooms(startDate: LocalDate, endDate: LocalDate): Flux<Room>

    @Query("""
        SELECT r.* FROM rooms r
        WHERE NOT EXISTS (
            SELECT 1 FROM bookings b
            WHERE b.room_id = r.id
            AND b.start_date <= :endDate
            AND b.end_date >= :startDate
        )
        ORDER BY RANDOM()
        LIMIT 5
    """)
    fun findRecommendedRooms(startDate: LocalDate, endDate: LocalDate): Flux<Room>
}