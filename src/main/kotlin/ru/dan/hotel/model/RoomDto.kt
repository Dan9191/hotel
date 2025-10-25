package ru.dan.hotel.model

data class RoomDto(
    val id: Long? = null,
    val hotelId: Long,
    val number: String,
    val available: Boolean
)