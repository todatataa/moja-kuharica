# Moja kuharica

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

## 1. O projektu

Moja kuharica uporabniku omogoča, da na enem mestu hrani svoje recepte. Vsak recept vsebuje naslov, kratek opis, sestavine, navodila in čas priprave.

**Cilji projekta:**

- vzpostaviti ločeno zaledje (REST API) in obličje (spletni vmesnik),
- podatke trajno hraniti v relacijski bazi PostgreSQL,
- dokumentirati API s Swaggerjem (OpenAPI).

**Funkcionalnosti:**

- dodajanje, pregledovanje, urejanje in brisanje receptov (CRUD),
- iskanje po naslovu in sestavinah,
- paginacija (5 receptov na stran),
- razvrščanje po času nastanka, naslovu in času priprave,
- interaktivna dokumentacija API-ja (Swagger UI).

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
| `VITE_API_URL` | obličje (naslov API-ja) | `http://localhost:8080/api/recipes` |

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
<img width="696" height="592" alt="Moja-Kuharica-DPU" src="https://github.com/user-attachments/assets/125ec5d5-b48c-4dd2-a01c-89cd25943f01" />




## Avtor

Ivan Todić, FERI, Univerza v Mariboru.
