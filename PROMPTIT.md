# Kehote:
> Tarvitsen sinut parikoodaajaksi luomaan yksinkertainen kokoushuoneiden varausrajapinta. Palvelun tulee tarjota seuraavat ratkaisut käyttäjille: huone voidaan varata tietylle aikavälille, varaus voidaan poistaa/perua, kaikkia varauksia voidaan katsella listassa. Palvelussa täytyy olla seuraava toimintalogiikan antamat rajoitteet: varauksia ei voi tehdä päällekkäin yhdelle huoneelle, varaukset eivät voi sijoittua menneisyyteen, huonevarauksen aloitusajankohta on oltava ennen lopetusajankohtaa.
> Ratkaisu voi käyttää muistinvaraista tietokantaa. Kirjoitetaan ohjelma Java-ohjelmointikielellä.
> Nyt luo alustava sovellus niin, että perusvaatimukset toimivat.

# Vastaus: 

Tämä on mainio projekti aloitettavaksi! Rakennetaan kevyt Spring Boot -sovellus, joka toteuttaa pyytämäsi logiikan. Käytämme muistinvaraista listaa tietokantana, jotta pääsemme nopeasti vauhtiin ilman ulkoisia riippuvuuksia.

### Sovelluksen rakenne
Tarvitsemme kolme pääkomponenttia:

1. Malli (Booking): Sisältää varauksen tiedot (huone, ajat).

2. Palvelu (BookingService): Sisältää liiketoimintalogiikan (tarkistukset).

3. Kontrolleri (BookingController): Tarjoaa REST-rajapinnan.

### 1. Varauksen malli (Booking. java)

Tämä luokka pitää sisällään varauksen perustiedot.

```
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
```

### 2. Palvelukerros ja logiikka (BookingService.java)

Tässä toteutamme pyytämäsi rajoitteet:
- Ei päällekkäisiä varauksia.

- Ei varauksia menneisyyteen.

- Aloitusaika ennen lopetusaikaa.

```
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
```

### 3. Rajapinta (BookingController.java)

Tämä mahdollistaa kommunikoinnin sovelluksen kanssa HTTP-pyynnöillä.

```
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
```
### Miten testaat tätä?

Kun sovellus on käynnissä, voit testata sitä esimerkiksi curl-komennolla tai Postmanilla:

1. Luo varaus: POST /api/bookings Body: {"roomName": "Neukkari 1", "startTime": "2026-02-01T10:00:00", "endTime": "2026-02-01T11:00:00"}
2. Listaa kaikki: GET /api/bookings
3. Testaa virhettä: Yritä luoda toinen varaus samalle ajalle "Neukkari 1" -huoneeseen. Palvelun pitäisi heittää virhe.

#### Seuraavat askeleet

Tämä on vasta runko. Haluatko, että lisäämme poikkeusten käsittelyn (Global Exception Handler), jotta käyttäjä saa 
selkeämmän virheilmoituksen (kuten 400 Bad Request) sen sijaan, että sovellus palauttaa vain yleisen virheen?

# Kehote:
> Haluan hyödyntää ResponseEntity luokkaa parempien vastausten luonnissa. Mikä on sen syntaksi, jos haluan esimerkiksi 
> palauttaa ResponseEntityn, jonka status on 'not found' ja lisäksi sen mukana tulisi viesti, joka kertoo paremmin virheestä

# Vastaus:

ResponseEntity on erittäin joustava luokka, jonka avulla voit hallita vastauksen tilaa (status), otsikoita (headers) ja runkoa (body).

Kun haluat palauttaa "Not Found" (404) -vastauksen viestin kera, on kaksi yleistä tapaa kirjoittaa syntaksi.

1. Rakentaja-syntaksi (Suositeltu)
   Tämä on selkein ja moderni tapa ketjuttaa metodit.

```
@DeleteMapping("/{id}")
public ResponseEntity<String> deleteBooking(@PathVariable String id) {
    boolean removed = bookingService.cancelBooking(id);
    
    if (!removed) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // Asettaa statuksen 404
                .body("Varausta tunnuksella " + id + " ei löytynyt, joten sitä ei voitu poistaa.");
    }
    
    return ResponseEntity.ok("Varaus poistettu onnistuneesti.");
}
```

2. Lyhyempi staattinen metodi
   ResponseEntity-luokassa on valmiita apumetodeja yleisimmille koodeille (kuten ok(), badRequest(), notFound()). Huomaa kuitenkin, että .notFound()-metodi ei salli rungon (body) lisäämistä suoraan, vaan se palauttaa yleensä vain tyhjän 404-vastauksen. Siksi viestin kanssa käytetään usein status()-metodia kuten yllä.

Jos haluat välttämättä käyttää notFound()-alkua, se näyttää tältä (mutta on hieman kömpelömpi viestin kanssa):

```
return ResponseEntity.notFound().build(); // Palauttaa vain 404 ilman tekstiä
```

Paras tapa: JSON-virheviesti tekstin sijaan
Yleensä rajapinnoissa on tapana palauttaa pelkän tekstin sijaan pieni JSON-objekti, jotta kutsuva sovellus voi lukea virheen helpommin.

```
@GetMapping("/{id}")
public ResponseEntity<?> getBookingById(@PathVariable String id) {
    return bookingService.findOptionalById(id)
            .map(booking -> ResponseEntity.ok(booking))
            .orElseGet(() -> ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Varausta ei löytynyt", "id", id)));
}
```
