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
# Kehote:

> Parantelin lopulta tällaiseen muotoon addBooking metodin controllerissa:

```
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
```

> Tulisiko geneeristen Exception kutsujen sijasta käyttää jotain tiettyä?

# Vastaus:

Kyllä, on erittäin suositeltavaa välttää geneerisen Exception-luokan kiinniottamista. Se on ikään kuin heittäisi suuren verkon mereen: saat kiinni haluamasi kalat (liiketoimintavirheet), mutta myös roskat (kuten koodivirheet tai muistin loppumisen), joita et ehkä haluaisi käsitellä samalla tavalla.

Tässä on kolme syytä ja tapaa parantaa koodia:

1. Käytä spesifejä poikkeuksia

Geneerinen Exception nappaa myös sellaiset virheet, joita et ole tarkoittanut (esim. NullPointerException). On parempi käyttää tarkempia poikkeuksia:

- IllegalArgumentException: Kun syötetty data on väärin (esim. loppuaika ennen alkuaikaa).
- Omat poikkeukset: Voit luoda oman luokan, esim. BookingConflictException, kun huone on jo varattu.

2. Erota validointi ja järjestelmävirheet

Jos koodissasi tapahtuu odottamaton virhe (esim. yhteys tietokantaan katkeaa), haluat yleensä palauttaa 500 Internal Server Error. Jos taas käyttäjä syöttää väärää tietoa, palautat 400 Bad Request. Geneerinen catch (Exception e) tekee molemmista 400-virheitä, mikä on hämmentävää rajapinnan käyttäjälle.

3. Suositeltu ratkaisu: @ControllerAdvice

Sen sijaan, että kirjoitat try-catch -lohkon jokaiseen kontrollerin metodiin, Springissä on tapana käyttää globaalia virheen käsittelijää. Tämä pitää kontrollerin siistinä.

Päivitetty kontrolleri:

```
@PostMapping
public ResponseEntity<Booking> addBooking(@RequestBody BookingRequest request) {
    // Ei try-catchia! Anna poikkeuksen "kuplia" ylöspäin.
    Booking created = bookingService.createBooking(
            request.roomName(),
            request.startTime(),
            request.endTime()
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
```

Globaali käsittelijä (GlobalExceptionHandler.java): Tämä nappaa vain ne virheet, jotka sinä hallitset.

```
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBusinessLogicErrors(IllegalArgumentException e) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Jos haluat erottaa päällekkäisyydet omaksi koodikseen (esim. 409 Conflict)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleConflict(IllegalStateException e) {
        ApiError error = new ApiError(HttpStatus.CONFLICT, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
```

Miten etenemme?
Haluaisitko, että luomme oman poikkeusluokan (esim. RoomAlreadyBookedException), jotta voimme palauttaa täsmällisemmän 409 Conflict -statuksen päällekkäisistä varauksista?

# Kehote:

> Toki, mutta olen aikaisemmin luonut tämän ApiError-luokan, jolla voi muodostaa JSON-muotoisen virheen, jonka palauttaa.
> Käytä tätä näiden poikkeusten esittämisessä, kuten itsekin oli käyttänyt.

# Vastaus:

Se on erinomainen tapa toimia. Käyttämällä omaa ApiError-luokkaasi globaalissa käsittelijässä, saat pidettyä rajapinnan vastausmuodon yhtenäisenä, tapahtuipa virhe missä päin sovellusta tahansa.

Tehdään tästä siisti kokonaisuus: luodaan muutama täsmällinen poikkeusluokka ja kytketään ne GlobalExceptionHandler-luokkaan, joka hyödyntää ApiError-luokkaasi.

1. Luodaan täsmälliset poikkeukset

Luodaan kaksi uutta poikkeusta, jotta voimme erottaa väärät syötteet (kuten menneet ajat) ja varauskonfliktit toisistaan.

```
// Käytetään, kun huone on jo varattu (HTTP 409 Conflict)
public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}

// Käytetään, kun syötteessä on vikaa (HTTP 400 Bad Request)
public class InvalidBookingException extends RuntimeException {
    public InvalidBookingException(String message) {
        super(message);
    }
}
```

2. Päivitetty GlobalExceptionHandler

Tämä luokka kaappaa heitetyt poikkeukset ja käärii ne ApiError-muotoon.

```
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ApiError> handleInvalidBooking(InvalidBookingException e) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ApiError> handleConflict(BookingConflictException e) {
        ApiError error = new ApiError(HttpStatus.CONFLICT, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Napataan myös muut odottamattomat virheet, jotta nekin tulevat ApiError-muodossa
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralError(Exception e) {
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Odottamaton virhe tapahtui.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

3. Logiikan päivitys palvelukerroksessa (BookingService)

```
public synchronized Booking createBooking(String roomName, LocalDateTime start, LocalDateTime end) {
    if (start.isAfter(end) || start.isEqual(end)) {
        throw new InvalidBookingException("Aloitusajan on oltava ennen lopetusaikaa.");
    }

    if (start.isBefore(LocalDateTime.now())) {
        throw new InvalidBookingException("Varausta ei voi tehdä menneisyyteen.");
    }

    boolean overlap = bookings.stream()
            .filter(b -> b.getRoomName().equalsIgnoreCase(roomName))
            .anyMatch(b -> start.isBefore(b.getEndTime()) && end.isAfter(b.getStartTime()));

    if (overlap) {
        throw new BookingConflictException("Huone " + roomName + " on jo varattu valitulla aikavälillä.");
    }

    Booking newBooking = new Booking(roomName, start, end);
    bookings.add(newBooking);
    return newBooking;
}
```

Mikä tässä on hyvää?
Kontrolleri puhdistuu: Voit poistaa try-catch-lohkot kontrollerista. Se vain kutsuu palvelua ja palauttaa OK tai CREATED.

Selkeät HTTP-koodit: Päällekkäisestä varauksesta tulee nyt tyylikäs 409 Conflict, joka on semanttisesti oikein.

Yhtenäisyys: Kaikki virheet näyttävät samalta kutsujalle (ID, timestamp, viesti), koska ne kaikki kulkevat ApiErrorin kautta.

Miltä tämä rakenne vaikuttaa? Haluatko seuraavaksi, että lisätään automaattinen validointi BookingRequest-luokkaan (esim. @NotBlank huoneen nimelle), jotta emme saa tyhjiä varauksia?

# Kehote:

> Meidän tulisi lisätä yksittäiselle huoneelle entiteetti ja palvelulogiikka, jotta voimme varmentaa paremmin sen, 
> onko huoneella varauksia, vai eikö huonetta ole olemassa, sekä muut vastaavat poikkeustilanteet. Luo tähän 
> tarvittavat ominaisuudet.

# Vastaus:

Tämä on looginen askel eteenpäin. Eriyttämällä huoneen (Room) omaksi entiteetikseen voimme hallita huoneen ominaisuuksia (kuten nimeä ja kapasiteettia) riippumatta varauksista. Samalla voimme varmistaa, ettei varauksia tehdä olemattomiin huoneisiin.

1. Huone-entiteetti (Room.java)

Luodaan yksinkertainen malli huoneelle.

```
public class Room {
    private String id;
    private String name;

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getterit
    public String getId() { return id; }
    public String getName() { return name; }
}
```

2. Huonepalvelu (RoomService.java)

Tämä palvelu vastaa huoneiden hallinnasta. Lisätään sinne muutama huone oletuksena.

```
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RoomService {
    private final Map<String, Room> rooms = new HashMap<>();

    public RoomService() {
        // Alustetaan muutama huone testailua varten
        rooms.put("neukkari1", new Room("neukkari1", "Neukkari 1"));
        rooms.put("neukkari2", new Room("neukkari2", "Neukkari 2"));
    }

    public Optional<Room> findById(String id) {
        return Optional.ofNullable(rooms.get(id));
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }
}
```

3. Poikkeus olemattomalle huoneelle

Lisätään uusi poikkeus, jotta voimme palauttaa 404 Not Found, jos huonetta ei löydy.

```
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message);
    }
}
```

Lisää tämä myös GlobalExceptionHandler-luokkaan:

```
@ExceptionHandler(RoomNotFoundException.class)
public ResponseEntity<ApiError> handleRoomNotFound(RoomNotFoundException e) {
    ApiError error = new ApiError(HttpStatus.NOT_FOUND, e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

4. Päivitetty varauslogiikka (BookingService.java)

Nyt muutamme BookingServiceä niin, että se käyttää RoomServiceä huoneen olemassaolon tarkistamiseen. Muutetaan samalla varaus käyttämään huoneen ID:tä nimen sijaan.

```
@Service
public class BookingService {
    private final List<Booking> bookings = new ArrayList<>();
    private final RoomService roomService;

    public BookingService(RoomService roomService) {
        this.roomService = roomService;
    }

    public synchronized Booking createBooking(String roomId, LocalDateTime start, LocalDateTime end) {
        // 1. Varmistetaan, että huone on olemassa
        Room room = roomService.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Huonetta ID:llä " + roomId + " ei löydy."));

        // 2. Aikavälin validointi
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new InvalidBookingException("Aloitusajan on oltava ennen lopetusaikaa.");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new InvalidBookingException("Varausta ei voi tehdä menneisyyteen.");
        }

        // 3. Päällekkäisyyden tarkistus (käyttäen roomId:tä)
        boolean overlap = bookings.stream()
                .filter(b -> b.getRoomId().equals(roomId))
                .anyMatch(b -> start.isBefore(b.getEndTime()) && end.isAfter(b.getStartTime()));

        if (overlap) {
            throw new BookingConflictException("Huone " + room.getName() + " on jo varattu.");
        }

        Booking newBooking = new Booking(roomId, start, end);
        bookings.add(newBooking);
        return newBooking;
    }
}
```

5. Uudet rajapintametodit (RoomController.java)

On hyvä tarjota rajapinta, josta näkee mitä huoneita on varattavissa.

```
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getRooms() {
        return roomService.getAllRooms();
    }
}
```

Yhteenveto muutoksista
Vikasieto: Jos yrität varata huonetta id: "keittiö", sovellus palauttaa 404 Not Found ja viestin "Huonetta ID:llä keittiö ei löydy".

Selkeys: Varaukset on nyt sidottu uniikkeihin ID-tunnuksiin, ei vapaaseen tekstiin.

Laajennettavuus: Voimme myöhemmin lisätä huoneille ominaisuuksia, kuten videotykki tai paikkamäärä.

Haluatko, että lisään seuraavaksi huoneen hakuun ominaisuuden, joka näyttää vain vapaana olevat huoneet tietylle aikavälille?

# Kehote:

> Emme ole vielä tehneet yhtään testejä. Luo testit hyvin ja kriittisesti BookingServicen tasolla.

# Vastaus:

Tämä on kriittinen vaihe. Koska BookingService sisältää sovelluksen tärkeimmän liiketoimintalogiikan, meidän on varmistettava, että rajoitteet pitävät kaikissa skenaarioissa.

Käytämme testaamiseen JUnit 5 -kehystä ja Mockitoa, jotta voimme simuloida RoomService:n toimintaa erillään.

Testiskenaariot

Testaamme seuraavat tapaukset:

1. Onnistunut varaus, kun huone on olemassa ja vapaa.
2. Virhe: Huonetta ei löydy (404-skenaario).
3. Virhe: Menneisyys (Varaus ei voi alkaa ennen tätä hetkeä).
4. Virhe: Aikajärjestys (Loppu ennen alkua).
5. Virhe: Päällekkäisyys (Testataan kolme eri päällekkäisyyden muotoa).

```
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BookingServiceTest {

    private BookingService bookingService;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomService.class);
        bookingService = new BookingService(roomService);
        
        // Luodaan oletusarvoinen huone mock-palveluun
        when(roomService.findById("neukkari1"))
            .thenReturn(Optional.of(new Room("neukkari1", "Neukkari 1")));
    }

    @Test
    @DisplayName("Pitäisi luoda varaus onnistuneesti kun tiedot ovat oikein")
    void createBooking_Success() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);

        Booking result = bookingService.createBooking("neukkari1", start, end);

        assertNotNull(result);
        assertEquals("neukkari1", result.getRoomId());
        assertEquals(1, bookingService.getAllBookings().size());
    }

    @Test
    @DisplayName("Pitäisi heittää RoomNotFoundException jos huonetta ei ole")
    void createBooking_RoomNotFound() {
        when(roomService.findById("olematon")).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> {
            bookingService.createBooking("olematon", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        });
    }

    @Test
    @DisplayName("Pitäisi estää varaus menneisyyteen")
    void createBooking_PastTime() {
        LocalDateTime pastStart = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        assertThrows(InvalidBookingException.class, () -> {
            bookingService.createBooking("neukkari1", pastStart, end);
        });
    }

    @Test
    @DisplayName("Pitäisi estää varaus jos loppuaika on ennen alkuaikaa")
    void createBooking_EndBeforeStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.minusHours(1);

        assertThrows(InvalidBookingException.class, () -> {
            bookingService.createBooking("neukkari1", start, end);
        });
    }

    @Test
    @DisplayName("Pitäisi estää päällekkäinen varaus")
    void createBooking_Overlap() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2); // 10:00 - 12:00

        // Tehdään ensimmäinen varaus
        bookingService.createBooking("neukkari1", start, end);

        // Yritetään tehdä päällekkäinen varaus (11:00 - 11:30)
        assertThrows(BookingConflictException.class, () -> {
            bookingService.createBooking("neukkari1", start.plusHours(1), start.plusHours(1).plusMinutes(30));
        });
        
        // Yritetään tehdä päällekkäinen varaus, joka alkaa ennen ja loppuu keskelle (09:00 - 11:00)
        assertThrows(BookingConflictException.class, () -> {
            bookingService.createBooking("neukkari1", start.minusHours(1), start.plusHours(1));
        });
    }
}
```

Mitä tässä on huomioitava?

Päällekkäisyyden logiikka: Testissä on useita päällekkäisyysskenaarioita. Logiikka start.isBefore(b.getEndTime()) && 
end.isAfter(b.getStartTime()) on matemaattisesti kattavin tapa todeta kahden aikavälin leikkaus.

Aika-riippuvuus: Käytän LocalDateTime.now().plusDays(1), jotta testit eivät epäonnistu sen takia, että "tämä hetki" 
muuttuu testin suorituksen aikana (verrattuna kovaan koodattuun päivämäärään).

Eristys: Mockaamalla RoomService:n varmistamme, että testaamme vain BookingService:n logiikkaa, emme huoneiden 
tallennusmekanismia.

Haluatko, että täydennämme testejä vielä integraatiotesteillä, jotka varmistavat, että GlobalExceptionHandler 
todella palauttaa oikeat HTTP-koodit ja ApiError-objektit?