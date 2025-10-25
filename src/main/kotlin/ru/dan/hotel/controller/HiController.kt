package ru.dan.hotel.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/hi")
class HiController {

    @GetMapping()
    fun hi(
        @RequestHeader("X-Correlation-Id") correlationId: String
    ): Mono<String> {
        return Mono.just(correlationId)
    }
}
