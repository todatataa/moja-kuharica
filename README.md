# Moja kuharica
Avtorji: Ivan Todić, Branka Jelača, Janja Đokić

Fakulteta: Fakulteta za elektrotehniko, računalništvo in informatiko (FERI), Univerza v Mariboru

Spletna aplikacija za shranjevanje, iskanje in urejanje kuharskih receptov. Nastala je kot študentski projekt na FERI (Univerza v Mariboru).

## Kazalo

1. [O projektu](#1-o-projektu)
2. [Uporabljene tehnologije](#2-uporabljene-tehnologije)
3. [Struktura projekta](#3-struktura-projekta)
4. [Navodila za nameščanje in zagon](#4-navodila-za-nameščanje-in-zagon)
5. [REST API](#5-rest-api)
6. [Navodila za razvijalce](#6-navodila-za-razvijalce)
7. [Standardi kodiranja](#7-standardi-kodiranja)
8. [DPU](#8DPU)
9. [Vizija projekta](#-vizija-projekta)
10. [Besednjak](#-besednjak)
11. [Razredni diagram](#-razredni-diagram)

## 1. O projektu

Moja kuharica uporabniku omogoča, da na enem mestu hrani svoje recepte. Vsak recept vsebuje naslov, kratek opis, sestavine, navodila in čas priprave.

**Cilji projekta:**

- vzpostaviti ločeno zaledje (REST API) in obličje (spletni vmesnik),
- podatke trajno hraniti v relacijski bazi PostgreSQL,
- dokumentirati API s Swaggerjem (OpenAPI).

**Funkcionalnosti:**

- dodajanje, pregledovanje in urejanje receptov (CRUD),
- iskanje po naslovu in sestavinah,
- paginacija (5 receptov na stran),
- razvrščanje po času nastanka, naslovu in času priprave,
- interaktivna dokumentacija API-ja (Swagger UI).

Podrobni opisi načrtovanih primerov uporabe: [docs/podrobni_opisi_primerov_uporabe.md](docs/podrobni_opisi_primerov_uporabe.md)

Uporabniške zgodbe: [docs/user-stories.md](docs/user-stories.md)

## 2. Uporabljene tehnologije

| Del | Tehnologija | Verzija |
|---|---|---|
| Zaledje | Java (JDK) | 21 ali novejša (razvito na JDK 25) |
| Zaledje | Spring Boot (Web MVC, Data JPA) | 4.1.1 |
| Zaledje | Hibernate ORM | 7.4.5 |
| Zaledje | PostgreSQL JDBC gonilnik | 42.7.13 |
| Zaledje | springdoc-openapi (Swagger UI) | 3.0.0 |
| Zaledje | Maven (Maven Wrapper, ni treba nameščati) | 3.9.11 |
| Baza | PostgreSQL (Docker slika `postgres:16`) | 16 |
| Obličje | React | 19.3.1 |
| Obličje | Vite | 8.3.5 |
| Obličje | Node.js / npm | 24.15.0 / 11.12.1 |
| Orodja | Docker, Docker Compose | 29.3.0 |
| Orodja | Git | 2.50.1 |

## 3. Struktura projekta

```
moja-kuharica/
├── backend/                      Spring Boot zaledje
│   ├── src/main/java/si/feri/backend/
│   │   ├── BackendApplication.java   vstopna točka aplikacije
│   │   ├── Recipe.java               entiteta (tabela recipe)
│   │   ├── RecipeRepository.java     dostop do baze (Spring Data JPA)
│   │   ├── RecipeController.java     REST končne točke
│   │   └── PagedResponse.java        odgovor s paginacijo
│   ├── src/main/resources/
│   │   └── application.properties    nastavitve (povezava z bazo)
│   ├── pom.xml                       odvisnosti (Maven)
│   └── mvnw                          Maven Wrapper
├── frontend/                     React obličje
│   ├── src/
│   │   ├── main.jsx                  zagon Reacta
│   │   ├── App.jsx                   glavna komponenta (seznam, obrazec, API klici)
│   │   └── App.css                   stili
│   ├── index.html
│   └── package.json                  odvisnosti (npm)
├── docs/vaja1/                   dokumentacija iz prve vaje
├── docker-compose.yml            PostgreSQL v Dockerju
├── .env.example                  primer okoljskih spremenljivk
└── README.md
```

| Mapa / datoteka | Vsebina |
|---|---|
| `backend/` | zaledje: REST API, dostop do baze, Swagger |
| `frontend/` | uporabniški vmesnik, ki prek `fetch` kliče API |
| `docker-compose.yml` | zagon baze PostgreSQL z eno ukazno vrstico |
| `docs/` | dokumentacija in posnetki zaslona |

Obličje in zaledje sta ločena projekta. Komunicirata samo prek HTTP (JSON).

## 4. Navodila za nameščanje in zagon

### 4.1 Predpogoji

| Orodje | Preverjanje | Opomba |
|---|---|---|
| Git | `git --version` | |
| JDK 21 ali novejši | `java -version` | |
| Node.js 20 ali novejši (LTS) | `node -v` | priložen je npm |
| Docker Desktop | `docker info` | mora teči pred zagonom baze |

Maven ni potreben, ker projekt vsebuje Maven Wrapper (`mvnw`).

### 4.2 Kloniranje

```bash
git clone https://github.com/todatataa/moja-kuharica.git
cd moja-kuharica
```

### 4.3 Baza (PostgreSQL v Dockerju)

```bash
docker compose up -d
docker exec recipes-db pg_isready -U recipes -d recipesdb
```

Zadnji ukaz mora izpisati `accepting connections`. Baza je dostopna na vratih **5433**. Podatki se ohranijo v Docker volumnu `pgdata`.

### 4.4 Zaledje

```bash
cd backend
./mvnw spring-boot:run
```

Ko se v konzoli izpiše `Started BackendApplication`, je zaledje dosegljivo:

- API: http://localhost:8080/api/recipes
- Swagger UI: http://localhost:8080/swagger-ui/index.html

Alternativa: v IntelliJ IDEA odpri mapo `backend` in zaženi razred `BackendApplication`.

### 4.5 Obličje

V novem terminalu:

```bash
cd frontend
npm install
npm run dev
```

Aplikacija je dostopna na http://localhost:5173.

### 4.6 Okoljske spremenljivke

Vse imajo privzete vrednosti za lokalni razvoj, zato jih za osnovni zagon ni treba nastavljati. Primer je v datoteki `.env.example`.

| Spremenljivka | Uporablja | Privzeto |
|---|---|---|
| `DB_NAME` | Docker Compose | `recipesdb` |
| `DB_USER` | Docker Compose, zaledje | `recipes` |
| `DB_PASSWORD` | Docker Compose, zaledje | `recipes123` |
| `DB_PORT` | Docker Compose (vrata na računalniku) | `5433` |
| `DB_URL` | zaledje (JDBC naslov) | `jdbc:postgresql://localhost:5433/recipesdb` |
| `VITE_API_URL` | obličje (naslov API-ja za recepte) | `http://localhost:8080/api/recipes` |
| `VITE_AUTH_URL` | obličje (naslov API-ja za prijavo) | `http://localhost:8080/api/auth` |
| `CORS_ORIGIN` | zaledje (izvor obličja, ki sme pošiljati piškotke) | `http://localhost:5173` |

Docker Compose prebere datoteko `.env` v korenu projekta samodejno. Zaledje je ne bere, zato spremenljivke nastavi v terminalu (`export DB_PASSWORD=...`) ali v zagonski konfiguraciji IntelliJ. Če spremeniš `DB_PORT`, spremeni tudi `DB_URL`.

### 4.7 Pogoste težave

| Težava | Rešitev |
|---|---|
| Zaledje javi `Connection refused` | zaženi Docker in bazo (`docker compose up -d`) |
| Vrata 5433 so zasedena | nastavi drug `DB_PORT` in ustrezen `DB_URL` |
| Obličje prikaže »Zaledje ni dosegljivo« | zaženi zaledje na vratih 8080 |
| IntelliJ ne uvozi Mavena | uporabi Maven Wrapper (v nastavitvah Maven izberi *Use Maven wrapper*) |

### 4.8 Ustavitev

`Ctrl+C` v terminalih zaledja in obličja, nato `docker compose stop`. Podatki ostanejo.

## 5. REST API

Osnovni naslov: `http://localhost:8080/api/recipes`. Polna dokumentacija je v Swagger UI.

| Metoda | Pot | Opis |
|---|---|---|
| POST | `/api/recipes` | doda nov recept |
| GET | `/api/recipes` | vrne recepte (iskanje, paginacija, razvrščanje) |
| GET | `/api/recipes/{id}` | vrne en recept (404, če ne obstaja) |
| PUT | `/api/recipes/{id}` | posodobi recept (404, če ne obstaja) |
| DELETE | `/api/recipes/{id}` | izbriše recept (204 / 404) |

Branje receptov (`GET`) je odprto vsem. Dodajanje, urejanje in brisanje zahteva prijavo, sicer zaledje vrne `401`. Recept lahko spreminja ali briše samo njegov avtor (`ownerId`), sicer zaledje vrne `403`. Stari recepti brez avtorja so odprti za vse prijavljene.

### Uporabniki in prijava

Prijava uporablja HTTP sejo (piškotek `JSESSIONID`). Obličje mora zato vse zahteve pošiljati z `credentials: "include"` (to naredi `apiFetch` v `frontend/src/api.js`).

| Metoda | Pot | Opis |
|---|---|---|
| POST | `/api/auth/register` | registracija (201, 400 pri neveljavnih podatkih, 409 pri zasedenem imenu ali e-naslovu) |
| POST | `/api/auth/login` | prijava z uporabniškim imenom ali e-naslovom (200 / 401) |
| POST | `/api/auth/logout` | odjava, razveljavi sejo (204) |
| GET | `/api/auth/me` | trenutno prijavljen uporabnik (401, če ni prijave) |

Obličje in zaledje morata biti odprta na istem gostitelju (obe na `localhost`), sicer brskalnik piškotka seje ne pošlje.

Parametri za `GET /api/recipes`:

| Parameter | Privzeto | Opis |
|---|---|---|
| `q` | prazno | iskalni niz (naslov in sestavine) |
| `page` | `0` | številka strani (od 0) |
| `size` | `5` | število receptov na stran (največ 50) |
| `sortBy` | `id` | `id`, `title` ali `preparationTimeMinutes` |
| `direction` | `desc` | `asc` ali `desc` |

## 6. Navodila za razvijalce

### Delo z repozitorijem

1. Veja `main` je vedno delujoča. Neposredno vanjo ne pišemo.
2. Za vsako spremembo ustvari vejo iz `main`:

   ```bash
   git checkout main
   git pull
   git checkout -b feature/kratek-opis
   ```

   Poimenovanje vej: `feature/...` (nova funkcionalnost), `fix/...` (popravek), `docs/...` (dokumentacija).
3. Spremembe shranjuj v majhnih commitih:

   ```bash
   git add .
   git commit -m "feat: dodano filtriranje po kategoriji"
   ```

4. Pred pošiljanjem preveri, da se projekt prevede:

   ```bash
   cd backend && ./mvnw clean compile
   cd ../frontend && npm run build
   ```

5. Pošlji vejo in odpri Pull Request na `main`:

   ```bash
   git push -u origin feature/kratek-opis
   ```

6. Pull Request pregleda drug član ekipe. Po odobritvi se združi (merge), veja pa se izbriše.

### Sporočila commitov

Format: `tip: kratek opis v ukaznem naklonu`.

| Tip | Uporaba |
|---|---|
| `feat` | nova funkcionalnost |
| `fix` | popravek napake |
| `docs` | dokumentacija |
| `refactor` | preoblikovanje kode brez spremembe delovanja |
| `chore` | konfiguracija, odvisnosti |

### Česa ne objavljamo v repozitorij

Mape `target/` in `node_modules/`, datoteke `.env`, gesla in ključe ter nastavitve IDE (`.idea/`). To je urejeno v `.gitignore`.

## 7. Standardi kodiranja

**Splošno**

- Imena v kodi so v angleščini, besedila v uporabniškem vmesniku pa v slovenščini.
- Vsaka datoteka ima en jasen namen, funkcije so kratke.
- Komentarji pojasnjujejo *zakaj*, ne *kaj* koda počne.

**Java (zaledje)**

- Paketi z malimi črkami (`si.feri.backend`), razredi `PascalCase`, metode in spremenljivke `camelCase`, konstante `UPPER_SNAKE_CASE`.
- Zamik 4 presledki.
- Zgradba po slojih: kontroler (HTTP) → repozitorij (baza) → entiteta (model).
- Odvisnosti se vbrizgajo prek konstruktorja.
- Vsaka končna točka ima opis z `@Operation`, parametri z `@Parameter` (Swagger).

**REST API**

- Poti so množinski samostalniki (`/api/recipes`), akcije pa določa HTTP metoda.
- Pravilne statusne kode: `200` uspeh, `204` brisanje, `404` ni zapisa.
- Vhodni parametri se omejijo (npr. dovoljena polja razvrščanja, največja velikost strani).

**JavaScript / React (obličje)**

- Funkcijske komponente s kljukami (hooks), komponente v `PascalCase`, spremenljivke in funkcije v `camelCase`.
- Zamik 2 presledka, `const` kjer je mogoče.
- Naslov API-ja je v spremenljivki (`VITE_API_URL`), ne razpršen po kodi.
- Razredi CSS v `kebab-case`.
- Napake pri klicih API-ja se obravnavajo (`try/catch`) in uporabniku se prikaže sporočilo.

## 8. DPU
<img width="696" height="592" alt="Moja-Kuharica-DPU (2)" src="https://github.com/user-attachments/assets/711e2821-91d4-4f67-9f9c-b09e5f76cdcf" />


## 9. Vizija projekta

### 9.1 Opis rešitve

Vizija projekta **Moja kuharica** je razviti pregledno in enostavno spletno aplikacijo, ki uporabnikom omogoča shranjevanje, organiziranje in iskanje kuharskih receptov na enem mestu.

Aplikacija je namenjena vsem, ki radi kuhajo in želijo imeti svoje najljubše recepte urejene ter hitro dostopne, predvsem študentom, domačim kuharjem in ljubiteljem kuhanja.

Glavni cilj aplikacije je poenostaviti shranjevanje in iskanje receptov ter zmanjšati potrebo po uporabi različnih zapiskov ali drugih virov. Uporabniku želimo omogočiti, da hitro najde želeni recept in pridobi vse potrebne informacije za pripravo jedi.

### 9.2 Ključne funkcionalnosti

Aplikacija podpira dve vlogi: **gosta** (neprijavljen obiskovalec) in **uporabnika** (prijavljen član). Uporabnik je specializacija gosta, zato lahko izvaja vse funkcionalnosti gosta, poleg tega pa še dodatne, ki zahtevajo prijavo.

**Funkcionalnosti gosta:**

- **Ogled receptov:** gost lahko pregleduje objavljene recepte in njihove podrobnosti (naslov, opis, sestavine, navodila in čas priprave).
- **Iskanje receptov:** omogočeno je iskanje po naslovu recepta in sestavinah.
- **Filtriranje receptov:** rezultate iskanja je mogoče dodatno zožiti z izbranimi kriteriji. Filtriranje je razširitev iskanja in se izvede samo, kadar ga gost izbere.

**Funkcionalnosti uporabnika:**

- **Objavljanje receptov:** uporabnik lahko objavi nov recept z naslovom, opisom, navodili in časom priprave. Del objave je obvezen vnos sestavin, brez katerega recepta ni mogoče objaviti.
- **Ogled svojih receptov:** uporabnik ima pregled nad recepti, ki jih je sam objavil.
- **Urejanje svojih receptov:** pri ogledu svojih receptov lahko uporabnik po želji spremeni podatke obstoječega recepta. Urejanje je razširitev ogleda svojih receptov.
- **Ocenjevanje receptov:** uporabnik lahko recepte drugih uporabnikov oceni.
- **Komentiranje receptov:** uporabnik lahko recepte komentira in deli svoje izkušnje.
- **Všečkanje receptov:** uporabnik lahko recepte všečka in si tako označi tiste, ki so mu všeč.

### 9.3 Obseg rešitve in morebitne omejitve

Projekt Moja kuharica je osredotočen na shranjevanje, pregledovanje, iskanje in urejanje kuharskih receptov. Namen rešitve je uporabnikom omogočiti enostavno organizacijo receptov in dostop do informacij, potrebnih za pripravo jedi.

Trenutna rešitev ima naslednje omejitve:

- **Brez samostojne mobilne aplikacije:** aplikacija je zasnovana kot spletna aplikacija, do katere uporabniki dostopajo prek spletnega brskalnika.
- **Brez samodejnega izračunavanja hranilnih vrednosti:** aplikacija ne izračunava kalorij, beljakovin, maščob ali drugih hranilnih vrednosti jedi.
- **Brez samodejnega ustvarjanja nakupovalnih seznamov:** aplikacija ne omogoča samodejnega oblikovanja nakupovalnega seznama na podlagi sestavin izbranih receptov.
- **Brez načrtovanja tedenskih jedilnikov:** uporabniki ne morejo sestavljati tedenskih načrtov prehrane.
- **Brez samodejnega prilagajanja količin sestavin:** aplikacija ne preračunava količin sestavin glede na izbrano število porcij.

Navedene omejitve določajo trenutni obseg rešitve in funkcionalnosti, ki niso vključene v projekt.

## 10. Besednjak

V spodnji tabeli so predstavljeni pomembni domenski izrazi, ki se uporabljajo v aplikaciji Moja kuharica.


| Izraz | Razlaga |
|---|---|
| Recept | Zapis, ki vsebuje informacije in navodila za pripravo določene jedi. |
| Naslov recepta | Ime recepta oziroma jedi, po katerem lahko uporabnik recept prepozna in poišče. |
| Opis recepta | Kratka predstavitev jedi ali dodatne informacije o receptu. |
| Sestavina | Živilo ali druga prehranska sestavina, potrebna za pripravo jedi. |
| Navodila za pripravo | Zaporedje korakov, ki opisujejo postopek priprave jedi. |
| Čas priprave | Predvideni čas, potreben za pripravo jedi. |
| Zbirka receptov | Nabor receptov, ki so shranjeni v aplikaciji in jih uporabnik lahko pregleduje. |
| Čas nastanka recepta | Podatek o tem, kdaj je bil recept dodan v aplikacijo. Uporablja se tudi za razvrščanje receptov. |
| Gost | Neprijavljen obiskovalec aplikacije, ki lahko pregleduje in išče recepte. |
| Uporabnik | Registrirana oseba, ki se lahko prijavi v aplikacijo in uporablja dodatne funkcionalnosti. |
| Avtor recepta | Uporabnik, ki je ustvaril in objavil recept v aplikaciji. |
| Ocena | Vrednost, s katero uporabnik izrazi svoje mnenje o kakovosti recepta. |
| Povprečna ocena | Povprečna vrednost vseh ocen, ki so jih uporabniki dodelili določenemu receptu. |
| Komentar | Besedilno mnenje ali izkušnja, ki jo uporabnik napiše ob receptu. |
| Všeček | Oznaka, s katero uporabnik izrazi, da mu je določen recept všeč. |
| Priljubljeni recept | Recept, ki ga uporabnik shrani na svoj seznam priljubljenih za lažji poznejši dostop. |
| Iskanje receptov | Postopek iskanja receptov glede na vnesene ključne besede, na primer naslov ali sestavine. |
| Filtriranje receptov | Omejevanje prikazanih receptov glede na izbrane kriterije. |
| Razvrščanje receptov | Urejanje seznama receptov po izbranem vrstnem redu, na primer po naslovu ali času priprave. |
| RIS | Razvoj informacijskih sistemov, predmet, v okviru katerega nastaja projekt Moja kuharica. |

11. Razredni diagram
Razredni diagram aplikacije Moja kuharica prikazuje domenski model in glavne operacije za vse primere uporabe iz DPU, vključno z načrtovanimi funkcionalnostmi. Imena domenskih razredov so zapisana v slovenščini zaradi skladnosti z DPU; v zaledju so poimenovana v angleščini.
 
[Uredljiva izvorna datoteka (Draw.io)](docs/moja-kuharica_razredni-diagram.drawio)
11.1 Vloga razredov in ključne metode
Razred	Vloga in ključne metode
Uporabnik (AppUser)	Predstavlja registriranega uporabnika z enoličnim uporabniškim imenom, e-pošto in zgoščeno vrednostjo gesla. Metode za pridobivanje identifikatorja in uporabniškega imena omogočajo določitev avtorja recepta, komentarja ali ocene.
Recept (Recipe)	Hrani naslov, opis, sestavine, navodila, čas priprave in identifikator avtorja. setSestavine() nastavi sestavine; getPovprecnaOcena() in getSteviloOcen() vrneta izpeljana podatka o ocenah. Število všečkov je načrtovan izpeljani podatek.
Komentar (Comment)	Predstavlja besedilo komentarja, čas objave, recept in avtorja. Metode getBesedilo(), getCasObjave() in getAvtor() omogočajo prikaz komentarjev.
Ocena (Rating)	Povezuje uporabnika in recept z vrednostjo od 1 do 5. Konceptualna metoda veljavnaVrednost() predstavlja preverjanje dovoljenega razpona, ki ga v dejanski implementaciji izvaja RecipeController.rate() in omejitev v bazi.
PriljubljeniRecept (FavoriteRecipe)	Povezuje uporabnika z receptom, ki ga je všečkal oziroma shranil med priljubljene. getUporabnik() in getRecept() vrneta povezana objekta.
KriterijiIskanja	Predstavlja kriterije za iskanje po naslovu oziroma sestavinah, paginacijo, razvrščanje in načrtovano filtriranje po največjem času priprave. Konceptualna metoda ustreza(recept) preveri ujemanje recepta s kriteriji. Trenutno se parametri iskanja in razvrščanja posredujejo neposredno v RecipeController.getAll().
UpravljanjeReceptov	Logični storitveni razred, ki združuje glavne operacije aplikacije. V obstoječi implementaciji so te operacije razdeljene med RecipeController, CommentService in FavoriteRecipeService, zato ta razred ni samostojna Java datoteka.


11.2 Ključne operacije glede na DPU
Primer uporabe	Operacije razreda UpravljanjeReceptov
Ogled receptov	pridobiRecept(), poisciRecepte()
Iskanje in filtriranje receptov	poisciRecepte(kriteriji)
Objavljanje receptov in vnos sestavin	objaviRecept(), preveriSestavine()
Ogled in urejanje svojih receptov	mojiRecepti(), urediRecept()
Ocenjevanje receptov	dodajOceno()
Komentiranje receptov	dodajKomentar(), pridobiKomentarje()
Všečkanje receptov	dodajPriljubljeni(), odstraniPriljubljeni(), mojiPriljubljeni()


Operacija dodajanja komentarja preveri, da besedilo ni prazno ali sestavljeno samo iz presledkov in ne presega 500 znakov. Komentarji se vračajo od najnovejšega do najstarejšega. Pri ocenjevanju se preverita vrednost od 1 do 5 in enoličnost para uporabnik–recept; uporabnik ne more oceniti lastnega recepta. Enoličnost istega para velja tudi pri priljubljenih receptih.
11.3 Povezave med razredi
- Asociacija Uporabnik–Recept: uporabnik lahko objavi nič ali več receptov; nov recept ima enega avtorja. Kardinalnost 0..1 na strani uporabnika upošteva stare recepte brez shranjenega identifikatorja avtorja.
- Asociacije Uporabnik–Komentar/Ocena/PriljubljeniRecept: posamezni zapis pripada enemu uporabniku, uporabnik pa ima lahko nič ali več zapisov.
- Kompoziciji Recept–Komentar in Recept–Ocena: en recept ima nič ali več komentarjev oziroma ocen. Zapisi pripadajo natanko enemu receptu in se ob njegovem brisanju izbrišejo; polni romb je na strani recepta.
- Asociacija Recept–PriljubljeniRecept: en recept je lahko shranjen med priljubljenimi pri več uporabnikih. Odstranitev priljubljenega zapisa ne izbriše recepta.
- Odvisnosti: črtkani puščici iz razreda UpravljanjeReceptov kažeta uporabo razredov Recept in KriterijiIskanja.
Gost je akter iz DPU, ne samostojen shranjen domenski objekt; zato ga v razrednem diagramu ne modeliramo z dedovanjem. Operacije, označene z {načrt}, prikazujejo predvidene funkcionalnosti (npr. ogled lastnih receptov, časovni filter, obvezno preverjanje sestavin in brisanje lastnega komentarja); {model} označuje konceptualno operacijo. Starejši podrobni opis PU-03 predvideva spreminjanje oziroma umik ocene, medtem ko aktualna zgodba US-1 in implementacija dovoljujeta samo eno nespremenljivo oceno na uporabnika in recept. Diagram sledi aktualni zgodbi US-1; to razliko je treba uskladiti v projektni dokumentaciji.






