# Uporabniške zgodbe

Dokument vsebuje uporabniške zgodbe za novo funkcionalnost **Interakcija z recepti** (ocenjevanje, komentiranje in priljubljeni recepti) v aplikaciji Moja kuharica.

## Pregled

| ID | Zgodba | Prioriteta | Ocena zahtevnosti (SP) | Odgovorni član |
|---|---|---|---|---|
| US-1 | Ocenjevanje receptov | Visoka | 5 | Branka Jelača |
| US-2 | Komentiranje receptov | Srednja | 5 | Ivan Todić |
| US-3 | Priljubljeni recepti | Srednja | 5 | Janja Đokić |

**Prioriteta:** Visoka (ključna vrednost za uporabnika), Srednja (pomembna dopolnitev), Nizka (prijetna dodatna možnost).
**Ocena zahtevnosti:** v točkah zgodbe (SP), kjer 1 pomeni zelo majhno in 8 veliko delo.

## Predpogoj: registracija in prijava uporabnikov

Za ocenjevanje, komentiranje in dodajanje receptov med priljubljene mora biti uporabnik prijavljen v aplikacijo.

Registracija in prijava sta predvideni v obstoječem DPU in nista del treh novih uporabniških zgodb. Obravnavani sta kot skupni tehnični predpogoj za US-1, US-2 in US-3. V sprejemnih kriterijih je prijava zato zapisana kot začetno stanje (Given), neprijavljen uporabnik pa je scenarij napake.

## Splošno za vse zgodbe

**Odvisnost med zgodbami.** Zgodbe so med seboj neodvisne, odvisne so le od predpogoja. Zato jih lahko člani razvijajo vzporedno, ko je prijava na voljo.

**Skupne tehnične podrobnosti.**

- Zaledje (Spring Boot): vsaka funkcionalnost dobi svojo entiteto, ki je povezana z receptom in uporabnikom, ter svoje končne točke.
- Obličje (React): nove komponente v obstoječi strani s podrobnostmi recepta.
- Neprijavljen uporabnik prejme odgovor `401`, neobstoječ recept `404`, neveljaven vnos `400`.
- Vsaka nova končna točka je opisana v Swaggerju (`@Operation`).

---

## US-1: Ocenjevanje receptov

**Odgovorni član:** Branka Jelača
**Prioriteta:** Visoka
**Ocena zahtevnosti:** 5 SP

### Uporabniška zgodba

Kot **prijavljen uporabnik** želim **recept oceniti z oceno od 1 do 5**, da **drugim uporabnikom pokažem, kako dober je recept, in da lažje najdejo kakovostne recepte**.

### Sprejemni kriteriji

1. **Uspešna ocena**
   - **Given:** sem prijavljen in imam odprt recept, ki ga še nisem ocenil,
   - **When:** izberem oceno 4 in jo potrdim,
   - **Then:** se ocena shrani in pri receptu se prikaže povprečna ocena 4,0 ter število ocen 1.

2. **Ponovna ocena istega recepta**
   - **Given:** sem prijavljen in sem recept že ocenil,
   - **When:** poskusim isti recept ponovno oceniti,
   - **Then:** nova ocena se ne shrani, moja prvotna ocena ostane nespremenjena in prikaže se sporočilo, da sem ta recept že ocenil.

3. **Neveljavna ocena**
   - **Given:** sem prijavljen in imam odprt recept,
   - **When:** poskusim oddati oceno 0,
   - **Then:** ocena se ne shrani in prikaže se sporočilo, da mora biti ocena med 1 in 5.
   
4. **Neprijavljen uporabnik**
   - **Given:** nisem prijavljen,
   - **When:** poskusim oceniti recept,
   - **Then:** ocena se ne shrani in prikaže se sporočilo, da se moram za ocenjevanje prijaviti.

### Podrobnosti implementacije

- Nova entiteta `Rating` (vrednost 1 do 5), povezana z receptom in uporabnikom. Za par (uporabnik, recept) obstaja največ ena ocena.
- Končni točki: `PUT /api/recipes/{id}/rating` (odda oceno) in `GET /api/recipes/{id}` (v odgovoru povprečna ocena in število ocen).
- Povprečna ocena se izračuna v zaledju in je zaokrožena na eno decimalko.
- Obličje: izbira ocene z zvezdicami na strani recepta ter prikaz povprečja in števila ocen na seznamu in pri receptu.

### Naloge (sub-issues)

| Naloga | Opis | Odgovorni |
|---|---|---|
| US-1.1 Zaledje: ocenjevanje | Entiteta `Rating`, repozitorij, končna točka za oddajo ocene, preverjanje vrednosti 1 do 5, izračun povprečja. | Branka Jelača |
| US-1.2 Obličje: ocenjevanje | Komponenta za izbiro ocene, prikaz povprečne ocene in števila ocen, prikaz sporočil o napakah. | Branka Jelača |

---

## US-2: Komentiranje receptov

**Odgovorni član:** Ivan Todić
**Prioriteta:** Srednja
**Ocena zahtevnosti:** 5 SP

### Uporabniška zgodba

Kot **prijavljen uporabnik** želim **recept komentirati**, da **z drugimi delim svoje izkušnje s pripravo jedi in nasvete**.

### Sprejemni kriteriji

1. **Uspešen komentar**
   - **Given:** sem prijavljen in imam odprt recept,
   - **When:** vnesem komentar "Zelo okusno!" in ga oddam,
   - **Then:** se komentar shrani in prikaže na vrhu seznama komentarjev pri receptu, skupaj z mojim uporabniškim imenom in datumom.

2. **Prikaz komentarjev**
   - **Given:** recept ima tri komentarje,
   - **When:** odprem recept,
   - **Then:** so prikazani vsi trije komentarji, urejeni od najnovejšega do najstarejšega.

3. **Prazen ali predolg komentar**
   - **Given:** sem prijavljen in imam odprt recept,
   - **When:** oddam prazen komentar ali komentar, daljši od 500 znakov,
   - **Then:** komentar se ne shrani in prikaže se sporočilo o napaki.

4. **Neprijavljen uporabnik**
   - **Given:** nisem prijavljen,
   - **When:** poskusim oddati komentar,
   - **Then:** komentar se ne shrani in prikaže se sporočilo, da se moram za komentiranje prijaviti.

### Podrobnosti implementacije

- Nova entiteta `Comment` (besedilo, datum nastanka), povezana z receptom in avtorjem.
- Končni točki: `POST /api/recipes/{id}/comments` (dodaj komentar) in `GET /api/recipes/{id}/comments` (vrni komentare, najnovejši prvi).
- Besedilo komentarja je obvezno in dolgo največ 500 znakov.
- Obličje: obrazec za komentar in seznam komentarjev na strani recepta. Obrazec je viden samo prijavljenemu uporabniku, gost namesto njega vidi sporočilo o prijavi.

### Naloge (sub-issues)

| Naloga | Opis | Odgovorni |
|---|---|---|
| US-2.1 Zaledje: komentiranje | Entiteta `Comment`, repozitorij, končni točki za dodajanje in branje komentarjev, preverjanje dolžine besedila. | Ivan Todić |
| US-2.2 Obličje: komentiranje | Obrazec za komentar, seznam komentarjev z avtorjem in datumom, prikaz sporočil o napakah. | Ivan Todić |

---

## US-3: Priljubljeni recepti

**Odgovorni član:** Janja Đokić  
**Prioriteta:** Srednja  
**Ocena zahtevnosti:** 5 SP

### Uporabniška zgodba

Kot **prijavljen uporabnik** želim **dodajati recepte med svoje priljubljene in jih po potrebi odstraniti**, da **lahko hitro najdem recepte, ki jih želim ponovno pripraviti**.

### Sprejemni kriteriji

**AC-1: Uspešno dodajanje recepta med priljubljene**

- **Given:** Sem prijavljen in imam odprt recept, ki še ni na mojem seznamu priljubljenih.
- **When:** Kliknem gumb »Dodaj med priljubljene«.
- **Then:** Recept se doda na moj seznam priljubljenih in gumb prikaže, da je recept shranjen.

**AC-2: Odstranitev recepta iz priljubljenih**

- **Given:** Sem prijavljen in imam recept že shranjen med priljubljenimi.
- **When:** Kliknem gumb »Odstrani iz priljubljenih«.
- **Then:** Recept se odstrani z mojega seznama priljubljenih, vendar ostane dostopen na seznamu vseh receptov.

**AC-3: Pregled priljubljenih receptov**

- **Given:** Sem prijavljen in imam med priljubljenimi shranjena dva recepta.
- **When:** Odprem razdelek »Moji priljubljeni recepti«.
- **Then:** Prikažeta se oba shranjena recepta, recepti drugih uporabnikov, ki jih nisem shranil, pa niso prikazani.

**AC-4: Preprečevanje podvojenih zapisov**

- **Given:** Sem prijavljen in sem določen recept že dodal med priljubljene.
- **When:** Ponovno poskusim dodati isti recept med priljubljene.
- **Then:** Recept je na mojem seznamu prikazan samo enkrat.

**AC-5: Neprijavljen uporabnik**

- **Given:** Nisem prijavljen v aplikacijo.
- **When:** Poskusim dodati recept med priljubljene.
- **Then:** Recept se ne shrani in prikaže se sporočilo, da se moram za uporabo te funkcionalnosti prijaviti.

### Podrobnosti implementacije

**Zaledje – Spring Boot**

- Ustvarimo entiteto `FavoriteRecipe`, ki povezuje entiteti `User` in `Recipe`.
- V podatkovni bazi zagotovimo, da lahko posamezen uporabnik isti recept med priljubljene doda samo enkrat.
- Prijavljenega uporabnika določimo na podlagi preverjene prijave.
- Implementiramo naslednje končne točke:
  - `PUT /api/recipes/{id}/favorite` – doda recept med priljubljene.
  - `DELETE /api/recipes/{id}/favorite` – odstrani recept iz priljubljenih.
  - `GET /api/me/favorites` – vrne seznam priljubljenih receptov trenutnega uporabnika.
- Dodajanje že shranjenega recepta ne ustvari podvojenega zapisa.
- Ob zahtevi za neobstoječ recept vrnemo odgovor `404`.
- Neprijavljen uporabnik ne more dodajati ali odstranjevati priljubljenih receptov.
- Dokumentacijo končnih točk pripravimo v Swagger UI.

**Obličje – React**

- Na prikazu recepta dodamo gumb »Dodaj med priljubljene«.
- Gumb prikazuje, ali je recept že shranjen med priljubljenimi.
- Dodamo razdelek »Moji priljubljeni recepti«.
- Uporabnik lahko s seznama priljubljenih odstrani recept.
- Ob uspešni spremembi posodobimo prikaz brez ponovnega nalaganja celotne strani.
- Ob napaki prikažemo ustrezno sporočilo.

### Naloge (sub-issues)

**US-3.1 – Zaledje: priljubljeni recepti**

**Odgovorni član:** Janja Đokić

Implementacija entitete `FavoriteRecipe`, povezave med uporabnikom in receptom, repozitorija, poslovne logike ter končnih točk za dodajanje, odstranjevanje in pridobivanje priljubljenih receptov. Preprečimo podvojene zapise in preverimo delovanje v Swagger UI.

**US-3.2 – Obličje: priljubljeni recepti**

**Odgovorni član:** Janja Đokić

Izdelava gumba za dodajanje in odstranjevanje receptov iz priljubljenih, prikaz trenutnega stanja ter razdelka »Moji priljubljeni recepti«. Povezava z zaledjem, prikaz napak in preverjanje sprejemnih kriterijev.
