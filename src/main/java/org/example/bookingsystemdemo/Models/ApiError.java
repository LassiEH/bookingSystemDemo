package org.example.bookingsystemdemo.Models;

import org.springframework.http.HttpStatus;

/**
 * ApiError-luokka esittää virhevastausta.
 * Käytetään, kun pyyntö epäonnistuu.
 */
public class ApiError {
    // HTTP-statuskoodi
    private int status;
    // HTTP-statuksen tekstimuotoinen esitys
    private String error;
    // Koodissa määritelty kuvaus virheestä
    private String message;

    /**
     * Luo uuden ApiError-olion
     * @param status annettu HTTP-statuskoodi
     * @param message määritelty virheviesti
     */
    public ApiError(HttpStatus status, String message) {
        this.status = status.value();
        // Käytetään statuksesta saatavaa tekstiä suoraan
        this.error = status.getReasonPhrase();
        this.message = message;
    }

    /**
     * Hakija HTTP-statuskoodile
     * @return HTTP-status (400, yms.)
     */
    public int getStatus() {
        return status;
    }

    /**
     * Hakija virhetekstille
     * @return HTTP-statuksen virheteksti
     */
    public String getError() {
        return error;
    }

    /**
     * Hakija määritellylle virhetekstille
     * @return kustomoitu virheteksti
     */
    public String getMessage() {
        return message;
    }
}
