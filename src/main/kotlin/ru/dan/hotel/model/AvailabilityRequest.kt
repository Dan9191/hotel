package ru.dan.hotel.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class AvailabilityRequest(
    @field:NotBlank(message = "Request ID must not be blank")
    val requestId: String,

    @field:NotNull(message = "Start date must not be null")
    val startDate: LocalDate,

    @field:NotNull(message = "End date must not be null")
    val endDate: LocalDate
)