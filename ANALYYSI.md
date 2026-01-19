# Analyysi

## 1. Mitä tekoäly teki hyvin?

Tekoäly teki hyvin peruslogiikan, erityisesti Java-kirjastoja hyvin hyödyntäen. Esimerkiksi
BookingService.java:ssa olevat toimintalogiikan tarkistukset se teki helposti luettaviksi
käyttäen asianmukaisia kirjastoja ja metodeja.

## 2. Mitä tekoäly teki huonosti?

Tekoälyn raaka koodi ei noudata juuri hyviä käytäntöjä. Se ei tee riittävää dokumentointia, 
ja se tarjoaa helppoja ja nopeita ratkaisuja, jotka eivät ylläpidettävyyden ja jatkettavuuden
kannalta ole hyviä. Lisäksi tekoäly ei ehdottanut testejä tai ymmärtänyt importoida omia luokkiani.
Esimerkiksi alkuperäiset virheilmoitukset olivat pelkkiä tekstipätkiä, jotka eivät vastaa
nykyaikaista hyvien standardien mukaista virhetilanteiden hallintaa. Toinen esimerkki voisi
olla sen tarjoama ratkaisu laittaa virhekäsittelyn muodostaminen BookingControlleriin.
Kuitenkin sanoisin, että näiltä virheiltä olisi voitu välttyä laajemmalla kehotesuunnittelulla,
jos olisin alusta alkaen pyytänyt luomaan laajan dokumentaation, eriyttämään ominaisuuksia ja luomaan
testit.

## 3. Tärkeimmät parannukset.

### Poikkeustilanteiden ja virhekäsittelyn parannukset ja lisäykset. 
Kehotin tekoälyä hyödyntämään ResponseEntityä HTTP-vastausten luonnissa, mutta tekoälyn 
vastaus oli kovin puutteellinen, joten loin virhevastauksille luokan, joka kokoaa JSON-muotoon
kattavamman ilmoituksen. Lopuksi ohjasin vielä tekoälyn ehdottaman globaalin poikkeuskäsittelijän
hyödyntämään tätä luokkaa. Loin vielä tekoälyn unohtamat poikkeukset. Näkisin tämän noudattavan paremmin
modernia kehitystä, ja ratkaisujeni noudattavan REST-standardeja ja hyviä tapoja.

### Huoneelle entiteetti ja parempi testaus
Kehotin tekoälyä luomaan huoneelle oman Room-luokan, jotta voimme luoda rakenteet, joilla paremmin 
varmentua siitä, että väärä huone palauttaa virheilmoituksen. Näkisin tämän olevan jatkettavuuden 
kannalta parempi ratkaisu, sillä jos tämä olisi oikea sovellus, olisi hyvä varmistaa tällainen reunatapaus. 
Lisäksi korjasin tekoälyn RoomService-luokkaa, johon se lisäsi tarpeettomia ja vääränlaisia metodeja. 
Jätin kuitenkin yksinkertaisen muutaman huoneen lisäyksen, jotta tehtävän paino pysyy tehtävänannon vaatimuksissa.

### Yksikkötestit
Kehotin tekoälyä luomaan yksikkötestit jatkettavuuden takaamiseksi, jotta tulevaisuuden lisäysten luomat
ongelmat ja defektit havaittaisiin. Kuitenkin muutokseni tekoälyn vastauksiin ja muu puutteellinen konteksi
ratkaisustani teki tekoälyn raakakoodista jokseenkin virheellistä, joten jouduin muokkaaman testejä.
Lisäksi tekoäly loi epämääräisiä kommentteja ja nimiä testeille. Poistin turhat konditionaalit testien nimistä sekä
esimerkiksi täysin väärät ja turhat kellonajat, joita tekoäly oli pannut esimerkiksi testiin, jossa käsiteltiin päällekkäisiä varauksia.
