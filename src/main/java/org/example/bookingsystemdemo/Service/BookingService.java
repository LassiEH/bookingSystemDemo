package org.example.bookingsystemdemo.Service;

import org.example.bookingsystemdemo.Exception.BookingConflictException;
import org.example.bookingsystemdemo.Exception.BookingNotFoundException;
import org.example.bookingsystemdemo.Exception.InvalidBookingException;
import org.example.bookingsystemdemo.Models.Room;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// Gemini ei lisännyt näitä
import org.example.bookingsystemdemo.Models.Booking;

@Service
public class BookingService {
    private final List<Booking> bookings = new ArrayList<>();
    private final RoomService roomService;

    public BookingService(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Hakee huoneen varaukset
     * @param roomName huoneen nimi, kirjaimet voivat
     *                 ollat isoja tai pieniä
     * @return listan varauksista
     */
    public List<Booking> getBookingsForRoom(String roomName) {
        // varmistetaan huoneen olemassaolo
        Room room = roomService.findByName(roomName);
        return bookings.stream()
                .filter(b -> b.getRoom().getName().equalsIgnoreCase(room.getName()))
                .toList();
    }

    /**
     * Luo uuden huonevarauksen
     * @param roomName varattavan huoneen nimi
     * @param start varauksen alun ajankohta
     * @param end varauksen lopun ajankohta
     * @return luodun varauksen
     */
    public synchronized Booking createBooking(String roomName, LocalDateTime start, LocalDateTime end) {

        /**
         * Geminin ratkaisussa poikkeus käsitellään tässä, mutta itse
         * näkisin, että RoomService vastaa huoneeseen liittyvistä
         * virheistä.
         */
        Room room = roomService.findByName(roomName);

        // 1. Tarkistus: Alku ennen loppua
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new InvalidBookingException("Aloitusajan on oltava ennen lopetusaikaa.");
        }

        // 2. Tarkistus: Ei menneisyyteen
        if (start.isBefore(LocalDateTime.now())) {
            throw new InvalidBookingException("Varausta ei voi tehdä menneisyyteen.");
        }

        // 3. Tarkistus: Päällekkäisyys
        boolean overlap = bookings.stream()
                .filter(b -> b.getRoom().equals(room))
                .anyMatch(b -> start.isBefore(b.getEndTime()) && end.isAfter(b.getStartTime()));

        if (overlap) {
            throw new BookingConflictException("Huone on jo varattu valitulla aikavälillä.");
        }

        Booking newBooking = new Booking(room, start, end);
        bookings.add(newBooking);
        return newBooking;
    }

    /**
     * Poistaa annetun huonevarauksen
     * @param id poistettavan varauksen tunniste
     * @return onnistumiseen perustuvan boolean arvon
     */
    public void cancelBooking(String id) {
        boolean removed = bookings.removeIf(b -> b.getId().equals(id));
        if (!removed) {
            throw new BookingNotFoundException("Varausta '" + id + "' ei ole.");
        }
    }
}

