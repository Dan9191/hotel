package ru.dan.hotel.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.dan.hotel.entity.Hotel
import ru.dan.hotel.model.HotelDto

@Mapper(componentModel = "spring")
interface HotelMapper {
    @Mapping(target = "id", ignore = true)
    fun toEntity(dto: HotelDto): Hotel

    fun toDto(entity: Hotel): HotelDto
}