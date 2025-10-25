package ru.dan.hotel.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import ru.dan.hotel.entity.Room

interface RoomRepository : ReactiveCrudRepository<Room, Long> {
    fun findByAvailableTrue(): Flux<Room>

    @Query("SELECT * FROM rooms WHERE available = true ORDER BY RANDOM() LIMIT 5")
    fun findRecommendedRooms(): Flux<Room>
}