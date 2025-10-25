package ru.dan.hotel.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("rooms")
data class Room(
    @Id
    val id: Long? = null,
    @Column("hotel_id")
    val hotelId: Long,
    @Column("number")
    val number: String,
    @Column("available")
    val available: Boolean
)