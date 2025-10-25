package ru.dan.hotel.info

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class HotelInfoContributor(
    @Value("\${spring.application.name}") private val appName: String,
    @Value("\${server.port}") private val serverPort: String
) : InfoContributor {

    override fun contribute(builder: Info.Builder) {
        builder.withDetail("app", mapOf(
            "name" to "Hotel Management Service",
            "description" to "Service for managing hotels and rooms",
            "version" to "1.0.0"
        ))
        builder.withDetail("eureka", mapOf(
            "instance-id" to "$appName:$serverPort"
        ))
    }
}