package ru.dan.hotel.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class InfoContributor(
    @Value("\${spring.application.name}") private val appName: String,
    @Value("\${server.port}") private val serverPort: String
) : InfoContributor {

    override fun contribute(builder: Info.Builder) {
        builder.withDetail("app", mapOf(
            "name" to "Hotel Service $appName",
            "description" to "Hotel information",
            "version" to "1.0.0"
        ))
        builder.withDetail("eureka", mapOf(
            "instance-id" to "$appName:$serverPort"
        ))
    }
}