package org.example.bookingsystemdemo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.example.bookingsystemdemo.Exception.BookingConflictException;
import org.example.bookingsystemdemo.Exception.InvalidBookingException;
import org.example.bookingsystemdemo.Exception.RoomNotFoundException;
import org.example.bookingsystemdemo.Models.Booking;
import org.example.bookingsystemdemo.Models.Room;
import org.example.bookingsystemdemo.Service.BookingService;
import org.example.bookingsystemdemo.Service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private BookingService bookingService;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomService.class);
        bookingService = new BookingService(roomService);

        // Luodaan oletusarvoinen huone mock-palveluun
        when(roomService.findByName("Neukkari 1"))
                .thenReturn(new Room("neukkari1", "Neukkari 1"));
    }

    @Test
    @DisplayName("Luo varauksen onnistuneesti, kun tiedot ovat oikein")
    void createBooking_Success() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);

        Booking result = bookingService.createBooking("Neukkari 1", start, end);

        assertNotNull(result);
        assertEquals("Neukkari 1", result.getRoom().getName());
    }

    @Test
    @DisplayName("Heittää RoomNotFoundException, jos huonetta ei ole")
    void createBooking_RoomNotFound() {
        when(roomService.findByName("olematon")).thenThrow(new RoomNotFoundException("olematon"));

        assertThrows(RoomNotFoundException.class, () -> {
            bookingService.createBooking("olematon",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(1).plusHours(1));
        });
    }

    @Test
    @DisplayName("Estää varauksen tekemisen menneisyyteen")
    void createBooking_PastTime() {
        LocalDateTime pastStart = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        assertThrows(InvalidBookingException.class, () -> {
            bookingService.createBooking("Neukkari 1", pastStart, end);
        });
    }

    @Test
    @DisplayName("Estää varauksen, jos loppuaika on ennen alkuaikaa")
    void createBooking_EndBeforeStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.minusHours(1);

        assertThrows(InvalidBookingException.class, () -> {
            bookingService.createBooking("Neukkari 1", start, end);
        });
    }

    @Test
    @DisplayName("Estää päällekkäiset varaukset")
    void createBooking_Overlap() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2);

        // Tehdään ensimmäinen varaus
        bookingService.createBooking("Neukkari 1", start, end);

        // Tehdään päällekkäinen varaus
        assertThrows(BookingConflictException.class, () -> {
            bookingService.createBooking(
                    "Neukkari 1",
                    start.plusHours(1),
                    start.plusHours(1).plusMinutes(30));
        });

        // Tehdään päällekkäinen varaus, joka alkaa ennen ja loppuu keskelle
        assertThrows(BookingConflictException.class, () -> {
            bookingService.createBooking(
                    "Neukkari 1",
                    start.minusHours(1),
                    start.plusHours(1));
        });
    }
}