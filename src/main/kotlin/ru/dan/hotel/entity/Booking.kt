package ru.dan.hotel.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("bookings")
data class Booking(
    @Id
    val id: Long? = null,

    @Column("room_id")
    val roomId: Long,

    @Column("request_id")
    val requestId: String,

    @Column("start_date")
    val startDate: LocalDate,

    @Column("end_date")
    val endDate: LocalDate,

    @Column("booking_type")
    val bookingType: BookingType,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class BookingType {
    TEMPORARY,
    PERMANENT
}