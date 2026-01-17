package org.example.bookingsystemdemo.Service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// Gemini ei lisännyt näitä
import org.example.bookingsystemdemo.Models.Booking;

@Service
public class BookingService {
    private final List<Booking> bookings = new ArrayList<>();

    public List<Booking> getAllBookings() {
        return bookings;
    }

    public synchronized Booking createBooking(String roomName, LocalDateTime start, LocalDateTime end) {
        // 1. Tarkistus: Alku ennen loppua
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new IllegalArgumentException("Aloitusajan on oltava ennen lopetusaikaa.");
        }

        // 2. Tarkistus: Ei menneisyyteen
        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Varausta ei voi tehdä menneisyyteen.");
        }

        // 3. Tarkistus: Päällekkäisyys
        boolean overlap = bookings.stream()
                .filter(b -> b.getRoomName().equalsIgnoreCase(roomName))
                .anyMatch(b -> start.isBefore(b.getEndTime()) && end.isAfter(b.getStartTime()));

        if (overlap) {
            throw new IllegalArgumentException("Huone on jo varattu valitulla aikavälillä.");
        }

        Booking newBooking = new Booking(roomName, start, end);
        bookings.add(newBooking);
        return newBooking;
    }

    public boolean cancelBooking(String id) {
        return bookings.removeIf(b -> b.getId().equals(id));
    }
}

