package ru.dan.hotel

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Description
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.dan.hotel.model.HotelDto
import ru.dan.hotel.repository.HotelRepository
import ru.dan.hotel.repository.RoomRepository

@SpringBootTest
@AutoConfigureWebTestClient
class HotelIntegrationTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var hotelRepository: HotelRepository

    @Autowired
    lateinit var roomRepository: RoomRepository

    @BeforeEach
    fun setUp() {
        hotelRepository.deleteAll().block()
        roomRepository.deleteAll().block()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Description("Создание отеля")
    fun should_create_hotel_and_retrieve_it() {
        val hotelDto = HotelDto(name = "Test Hotel", address = "123 Test St")

        // Создание отеля через API
        webTestClient.post()
            .uri("/api/hotels")
            .header("X-Correlation-Id", "test-123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(hotelDto)
            .exchange()
            .expectStatus().isCreated
            .expectBody(HotelDto::class.java)
            .consumeWith { response ->
                val createdHotel = response.responseBody!!
                assert(createdHotel.id != null)
                assert(createdHotel.name == hotelDto.name)
                assert(createdHotel.address == hotelDto.address)
            }

        // Проверка получения всех отелей
        webTestClient.get()
            .uri("/api/hotels")
            .header("X-Correlation-Id", "test-123")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(HotelDto::class.java)
            .hasSize(1)
            .contains(hotelDto.copy(id = 1))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Description("Ошибка создания отеля с дубликатом имени")
    fun should_fail_to_create_hotel_with_duplicate_name() {
        val hotelDto = HotelDto(name = "Test Hotel", address = "123 Test St")

        // Создаем первый отель
        webTestClient.post()
            .uri("/api/hotels")
            .header("X-Correlation-Id", "test-123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(hotelDto)
            .exchange()
            .expectStatus().isCreated

        // Пытаемся создать отель с тем же именем
        webTestClient.post()
            .uri("/api/hotels")
            .header("X-Correlation-Id", "test-123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(hotelDto)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Hotel with name ${hotelDto.name} already exists")
    }
}