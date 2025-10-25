package ru.dan.hotel.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.dan.hotel.mapper.HotelMapper
import ru.dan.hotel.model.HotelDto
import ru.dan.hotel.repository.HotelRepository

@Service
class HotelService(
    private val hotelRepository: HotelRepository,
    private val hotelMapper: HotelMapper
) {
    private val logger = LoggerFactory.getLogger(HotelService::class.java)

    fun createHotel(dto: HotelDto, correlationId: String): Mono<HotelDto> {
        logger.info("[$correlationId] Creating hotel: ${dto.name}")
        return hotelRepository.findByName(dto.name)
            .flatMap { Mono.error<HotelDto>(IllegalArgumentException("Hotel with name ${dto.name} already exists")) }
            .switchIfEmpty(
                Mono.just(hotelMapper.toEntity(dto))
                    .flatMap { hotelRepository.save(it) }
                    .map { hotelMapper.toDto(it) }
            )
    }

    fun getAllHotels(correlationId: String): Flux<HotelDto> {
        logger.info("[$correlationId] Fetching all hotels")
        return hotelRepository.findAll()
            .map { hotelMapper.toDto(it) }
    }
}