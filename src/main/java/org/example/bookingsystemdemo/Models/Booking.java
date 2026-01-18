package org.example.bookingsystemdemo.Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Booking {
    /** varauksen tunniste */
    private String id;
    /** varauksen huone */
    private Room room;
    /** varauksen aloitusajankohta */
    private LocalDateTime startTime;
    /** varauksen lopetusajankohta */

    private LocalDateTime endTime;

    /**
     * Luo uuden varauksen
     *
     * @param room varattava huone
     * @param startTime varauksen aloitusaika
     * @param endTime varauksen lopetusaika
     */
    public Booking(Room room, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = UUID.randomUUID().toString();
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getterit
    public String getId() { return id; }
    public Room getRoom() { return room; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
