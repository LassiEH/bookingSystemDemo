package org.example.bookingsystemdemo.Controller;

import org.example.bookingsystemdemo.Models.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
// Gemini ei lisännyt näitä
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

    /**
     * Geminin ratkaisussa on parasta palauttaa JSON-virheviesti,
     * mutta sen toteutus Geminin ratkaisussa on hyvin vaikeaselkoinen.
     * Omassa ratkaisussa palautetaan onnistuessa luotu varaus,
     * ja epäonnistuessa virheen tiedot, molemmat JSON-muodossa,
     * mutta käytössä on oma virhekäsittelyn ApiError-luokka.
     */
    @PostMapping
    public ResponseEntity<?> addBooking(@RequestBody BookingRequest request) {

        try {
            Booking created = bookingService.createBooking(
                    request.roomName(),
                    request.startTime(),
                    request.endTime()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(created);

        } catch (Exception e) {
            ApiError error = new ApiError(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    /**
     * Geminin vastauksessa luodaan ResponseEntity statuksella
     * ja tekstiä sisältävällä bodylla, paras tapa JSON-virheviesti.
     * Kuitenkin omassa ratkaisussani käytän erillistä ApiError-luokkaa,
     * jos HTTP-vastaus ilmoittaa virheestä, muuten No Content -vastauksessa
     * ei ole mukana bodya viestille.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable String id) {
        boolean removed = bookingService.cancelBooking(id);

        if (!removed) {
            ApiError error = new ApiError(
                    HttpStatus.NOT_FOUND,
                    ("Varausta id:llä " + id + " ei ole")
            );
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }

        return ResponseEntity.noContent().build();
    }
}

// Apuluokka JSON-pyyntöä varten
record BookingRequest(String roomName, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {}