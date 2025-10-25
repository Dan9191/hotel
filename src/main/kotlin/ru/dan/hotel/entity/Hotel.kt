package ru.dan.hotel.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("hotels")
data class Hotel(
    @Id
    val id: Long? = null,

    @Column("name")
    val name: String,

    @Column("address")
    val address: String
)