package ru.dan.hotel.model

import jakarta.validation.constraints.NotBlank

data class HotelDto(
    val id: Long? = null,

    @field:NotBlank(message = "Name must not be blank")
    val name: String,

    @field:NotBlank(message = "Address must not be blank")
    val address: String
)