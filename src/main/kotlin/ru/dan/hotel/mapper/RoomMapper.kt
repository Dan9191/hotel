package ru.dan.hotel.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.dan.hotel.entity.Room
import ru.dan.hotel.model.RoomDto

@Mapper(componentModel = "spring")
interface RoomMapper {
    @Mapping(target = "id", ignore = true)
    fun toEntity(dto: RoomDto): Room

    fun toDto(entity: Room): RoomDto
}