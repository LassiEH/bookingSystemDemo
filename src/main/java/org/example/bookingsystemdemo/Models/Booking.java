package org.example.bookingsystemdemo.Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Booking {
    private String id;
    private String roomName;
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public Booking(String roomName, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = UUID.randomUUID().toString();
        this.roomName = roomName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getterit ja Setterit
    public String getId() { return id; }
    public String getRoomName() { return roomName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
