package org.example.bookingsystemdemo.Service;

import org.example.bookingsystemdemo.Exception.RoomNotFoundException;
import org.example.bookingsystemdemo.Models.Room;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RoomService {
    private final Map<String, Room> rooms = new HashMap<>();

    public RoomService() {
        // Alustetaan muutama huone testailua varten
        rooms.put("neukkari1", new Room("neukkari1", "Neukkari1"));
        rooms.put("neukkari2", new Room("neukkari2", "Neukkari2"));
    }

    /**
     * Hakee huoneen annetun nimen perusteella
     *
     * @param name huoneen nimi
     * @return palauttaa huoneen
     */
    public Room findByName(String name) {
        Room room = rooms.get(name.toLowerCase());
        if (room == null) {
            throw new RoomNotFoundException("Huonetta ei ole.");
        }
        return room;
    }

}