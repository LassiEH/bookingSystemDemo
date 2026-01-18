package org.example.bookingsystemdemo.Controller;

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

    /**
     * Palautetaan lista varauksista tietylle huoneelle.
     * Alkuperäisessä Geminille antamassani kehotteessa oli annettu vaatimukset
     * huonosti, joten Geminin ratkaisu listasi kaikki huonevaraukset.
     * Omassa versiossani siis näytetään yhden huoneen varaukset.
     * Tässä tein olettamuksen netistä lukemani perusteella, että huone, jolla
     * ei ole varauksia palauttaa tyhjän listan HTTP-statuskoodilla 200.
     * @param roomName huoneen nimi, jonka varaukset listataan
     * @return lista JSON-muotoisia varauksia huoneelle
     */
    @GetMapping("/{roomName}")
    public List<Booking> getBookingsForRoom(@PathVariable String roomName) {
        return bookingService.getBookingsForRoom(roomName);
    }

    /**
     * Geminin ratkaisussa on parasta palauttaa JSON-virheviesti,
     * mutta sen toteutus Geminin ratkaisussa on hyvin vaikeaselkoinen.
     * Omassa ratkaisussa palautetaan onnistuessa luotu varaus,
     * ja epäonnistuessa virheen tiedot, molemmat JSON-muodossa,
     * mutta käytössä on oma virhekäsittelyn ApiError-luokka.
     * @param request lisättävä varauspyyntö
     * @return ResponseEntity, joka ilmoittaa onnistumisesta
     */
    @PostMapping
    public ResponseEntity<?> addBooking(@RequestBody BookingRequest request) {
        Booking created = bookingService.createBooking(
                request.roomName(),
                request.startTime(),
                request.endTime()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    /**
     * Geminin vastauksessa luodaan ResponseEntity statuksella
     * ja tekstiä sisältävällä bodylla, ja paras tapa on JSON-virheviesti.
     * Kuitenkin omassa ratkaisussani käytän erillistä GlobalExceptionHandler-luokkaa
     * ja ApiError-luokkaa, jotka käsittelevät virheen muualla,
     * jos HTTP-vastaus ilmoittaa virheestä.
     * Muuten No Content -vastauksessa ei ole mukana bodya viestille.
     * @param id poistettavan huoneen tunniste
     * @return ResponseEntity, joka ilmoittaa onnistumisesta
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable String id) {
        bookingService.cancelBooking(id);

        return ResponseEntity.noContent().build();
    }
}

// Apuluokka JSON-pyyntöä varten
record BookingRequest(String roomName, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {}