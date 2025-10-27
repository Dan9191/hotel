package ru.dan.hotel.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import ru.dan.hotel.entity.Hotel

interface HotelRepository : ReactiveCrudRepository<Hotel, Long> {

    @Query("SELECT * FROM hotels WHERE name = ?")
    fun findByName(name: String): Mono<Hotel>
}