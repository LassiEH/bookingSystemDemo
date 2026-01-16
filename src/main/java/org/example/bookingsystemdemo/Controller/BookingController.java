package org.example.bookingsystemdemo.Controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
// Gemini lisännyt näitä
import org.example.bookingsystemdemo.Service.BookingService;
import org.example.bookingsystemdemo.Models.Booking;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> getBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping
    public Booking addBooking(@RequestBody BookingRequest request) {
        return bookingService.createBooking(
                request.roomName(),
                request.startTime(),
                request.endTime()
        );
    }

    @DeleteMapping("/{id}")
    public String deleteBooking(@PathVariable String id) {
        if (bookingService.cancelBooking(id)) {
            return "Varaus poistettu.";
        }
        return "Varausta ei löytynyt.";
    }
}

// Apuluokka JSON-pyyntöä varten
record BookingRequest(String roomName, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {}