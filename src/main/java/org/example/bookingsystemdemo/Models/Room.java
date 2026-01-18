package org.example.bookingsystemdemo.Models;

/**
 * Kuvaa yksittäisen huoneen varausjärjestelmässä
 */
public class Room {
    /**Huoneen tunniste */
    private String id;
    /**Huoneen nimi */
    private String name;

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getterit
    public String getId() { return id; }
    public String getName() { return name; }
}
