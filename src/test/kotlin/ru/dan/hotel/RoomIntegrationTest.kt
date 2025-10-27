package ru.dan.hotel

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Description
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import ru.dan.hotel.entity.Booking
import ru.dan.hotel.entity.BookingType
import ru.dan.hotel.entity.Hotel
import ru.dan.hotel.entity.Room
import ru.dan.hotel.model.AvailabilityRequest
import ru.dan.hotel.model.RoomDto
import ru.dan.hotel.repository.BookingRepository
import ru.dan.hotel.repository.HotelRepository
import ru.dan.hotel.repository.RoomRepository
import java.time.LocalDate

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(properties = ["spring.flyway.enabled=true"])
class RoomIntegrationTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var hotelRepository: HotelRepository

    @Autowired
    lateinit var roomRepository: RoomRepository

    @Autowired
    lateinit var bookingRepository: BookingRepository

    @Autowired
    lateinit var databaseClient: DatabaseClient

    private var hotelId: Long? = null // Храним ID созданного отеля, nullable

    @BeforeEach
    fun setUp() {
        // Очистка таблиц в правильном порядке
        bookingRepository.deleteAll().block()
        roomRepository.deleteAll().block()
        hotelRepository.deleteAll().block()

        // Сбрасываем автоинкремент для hotels
        databaseClient.sql("ALTER TABLE hotels ALTER COLUMN id RESTART WITH 1")
            .then()
            .block()

        // Создаем тестовый отель
        val hotel = hotelRepository.save(Hotel(name = "Test Hotel", address = "123 Test St")).block()!!
        println("Создан отель с id: ${hotel.id}") // Для отладки
        hotelId = hotel.id
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Description("должен создать комнату и получить доступные комнаты")
    fun should_create_room_and_get_available_rooms() {
        val roomDto = RoomDto(hotelId = hotelId!!, number = "101")

        // Создание комнаты
        webTestClient.post()
            .uri("/api/rooms")
            .header("X-Correlation-Id", "test-123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(roomDto)
            .exchange()
            .expectStatus().isCreated
            .expectBody(RoomDto::class.java)
            .consumeWith { response ->
                val createdRoom = response.responseBody!!
                assert(createdRoom.id != null)
                assert(createdRoom.number == roomDto.number)
                assert(createdRoom.hotelId == roomDto.hotelId)
            }

        // Проверка доступных комнат
        webTestClient.get()
            .uri("/api/rooms?startDate=2025-11-01&endDate=2025-11-03")
            .header("X-Correlation-Id", "test-123")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(RoomDto::class.java)
            .hasSize(1)
            .consumeWith<WebTestClient.ListBodySpec<RoomDto>> { response ->
                val rooms = response.responseBody!!
                assert(rooms.all { it.hotelId == hotelId })
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Description("должен получить рекомендуемые комнаты")
    fun should_receive_recommended_rooms() {
        // Создаем комнаты
        roomRepository.save(RoomDto(hotelId = hotelId!!, number = "101").toEntity()).block()
        roomRepository.save(RoomDto(hotelId = hotelId!!, number = "102").toEntity()).block()
        roomRepository.save(RoomDto(hotelId = hotelId!!, number = "103").toEntity()).block()

        // Проверка рекомендуемых комнат
        webTestClient.get()
            .uri("/api/rooms/recommend?startDate=2025-11-01&endDate=2025-11-03")
            .header("X-Correlation-Id", "test-123")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(RoomDto::class.java)
            .hasSize(3) // Все комнаты доступны, так как нет броней
            .consumeWith<WebTestClient.ListBodySpec<RoomDto>> { response ->
                val rooms = response.responseBody!!
                assert(rooms.all { it.hotelId == hotelId })
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Description("должен подтвердить доступность комнаты и вернуть ID брони")
    fun should_confirm_the_availability_room_and_return_booking_ID() {
        // Создаем комнату
        val room = roomRepository.save(RoomDto(hotelId = hotelId!!, number = "101").toEntity()).block()!!

        // Проверка доступности
        val request = AvailabilityRequest(
            requestId = "req-123",
            startDate = LocalDate.of(2025, 11, 1),
            endDate = LocalDate.of(2025, 11, 3)
        )

        webTestClient.post()
            .uri("/api/rooms/${room.id}/confirm-availability")
            .header("X-Correlation-Id", "test-123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(Long::class.java)
            .consumeWith { response ->
                val bookingId = response.responseBody!!
                val booking = bookingRepository.findById(bookingId).block()!!
                assert(booking.roomId == room.id)
                assert(booking.requestId == request.requestId)
                assert(booking.bookingType == BookingType.TEMPORARY)
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Description("должен не подтвердить доступность для забронированной комнаты")
    fun should_not_confirm_availability_booked_room() {
        // Создаем комнату
        val room = roomRepository.save(RoomDto(hotelId = hotelId!!, number = "101").toEntity()).block()!!

        // Создаем временную бронь
        bookingRepository.save(
            Booking(
                roomId = room.id!!,
                requestId = "req-123",
                startDate = LocalDate.of(2025, 11, 1),
                endDate = LocalDate.of(2025, 11, 3),
                bookingType = BookingType.TEMPORARY
            )
        ).block()

        // Пытаемся забронировать ту же комнату на пересекающиеся даты
        val request = AvailabilityRequest(
            requestId = "req-456",
            startDate = LocalDate.of(2025, 11, 1),
            endDate = LocalDate.of(2025, 11, 3)
        )

        webTestClient.post()
            .uri("/api/rooms/${room.id}/confirm-availability")
            .header("X-Correlation-Id", "test-123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Комната уже забронирована на эти даты")
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Description("должен подтвердить идемпотентную бронь с тем же requestId")
    fun should_confirm_an_idempotent_reservation_with_the_same_requestId() {
        // Создаем комнату
        val room = roomRepository.save(RoomDto(hotelId = hotelId!!, number = "101").toEntity()).block()!!

        // Создаем временную бронь
        val booking = bookingRepository.save(
            Booking(
                roomId = room.id!!,
                requestId = "req-123",
                startDate = LocalDate.of(2025, 11, 1),
                endDate = LocalDate.of(2025, 11, 3),
                bookingType = BookingType.TEMPORARY
            )
        ).block()!!

        // Повторный запрос с тем же requestId
        val request = AvailabilityRequest(
            requestId = "req-123",
            startDate = LocalDate.of(2025, 11, 1),
            endDate = LocalDate.of(2025, 11, 3)
        )

        webTestClient.post()
            .uri("/api/rooms/${room.id}/confirm-availability")
            .header("X-Correlation-Id", "test-123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(Long::class.java)
            .isEqualTo(booking.id!!)
    }

    private fun RoomDto.toEntity() = Room(
        id = this.id,
        hotelId = this.hotelId,
        number = this.number
    )
}