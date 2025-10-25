package ru.dan.hotel.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


data class RoomDto(
    val id: Long? = null,
    val hotelId: Long,
    val number: String,
    val available: Boolean
)