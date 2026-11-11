# Podrobni opisi primerov uporabe – Moja Kuharica

Akterja: **Gost** (neprijavljen obiskovalec) in **Uporabnik** (prijavljen; podeduje vse primere uporabe Gosta).

---

## PU-01: Ogled receptov

| | |
|---|---|
| **Primer uporabe:** Ogled receptov | **ID:** PU-01 |
| **Cilj:** | Obiskovalec si želi ogledati seznam receptov in podrobnosti izbranega recepta (sestavine, postopek, ocene, komentarji). |
| **Akterji:** | Gost, Uporabnik, sistem |
| **Predpogoji:** | Aplikacija je dostopna; v bazi obstaja vsaj en objavljen recept. |
| **Stanje sistema po PU:** | Nespremenjeno (samo branje podatkov). |
| **Scenarij:** | 1. Akter odpre začetno stran ali seznam receptov.<br>2. Sistem prikaže seznam objavljenih receptov (naslov, slika, povprečna ocena).<br>3. Akter izbere recept.<br>4. Sistem prikaže podrobnosti recepta: sestavine, postopek, avtorja, ocene in komentarje. |
| **Alternativni tokovi:** | 2a. Ni objavljenih receptov: sistem prikaže sporočilo »Trenutno ni receptov«.<br>3a. Akter se vrne na seznam brez izbire recepta. |
| **Izjeme:** | Recept je bil med ogledom izbrisan ali ne obstaja: sistem prikaže napako 404 in povezavo nazaj na seznam.<br>Napaka pri povezavi z bazo: sistem prikaže splošno sporočilo o napaki. |

---

## PU-02: Ogled svojih receptov

| | |
|---|---|
| **Primer uporabe:** Ogled svojih receptov | **ID:** PU-02 |
| **Cilj:** | Uporabnik želi pregledati vse recepte, ki jih je sam objavil. |
| **Akterji:** | Uporabnik, sistem |
| **Predpogoji:** | Uporabnik je prijavljen. |
| **Stanje sistema po PU:** | Nespremenjeno. |
| **Scenarij:** | 1. Uporabnik izbere »Moji recepti«.<br>2. Sistem poišče recepte, katerih avtor je uporabnik.<br>3. Sistem prikaže seznam z osnovnimi podatki (naslov, datum, povprečna ocena).<br>4. Uporabnik po želji izbere recept za podroben ogled. |
| **Alternativni tokovi:** | 3a. Uporabnik še nima receptov: sistem prikaže sporočilo in povezavo na objavo prvega recepta.<br>4a. Uporabnik izbere »Uredi«: nadaljuje se primer uporabe Urejanje svojih receptov (extend). |
| **Izjeme:** | Uporabnik ni prijavljen: preusmeritev na prijavo.<br>Napaka baze: sistem prikaže sporočilo o napaki. |

---

## PU-03: Ocenjevanje receptov

| | |
|---|---|
| **Primer uporabe:** Ocenjevanje receptov | **ID:** PU-03 |
| **Cilj:** | Uporabnik želi recept oceniti (npr. 1–5 zvezdic). |
| **Akterji:** | Uporabnik, sistem |
| **Predpogoji:** | Uporabnik je prijavljen; recept obstaja; uporabnik ni avtor recepta (če je ta omejitev določena). |
| **Stanje sistema po PU:** | Ocena je shranjena (ali posodobljena) in povprečna ocena recepta preračunana. |
| **Scenarij:** | 1. Uporabnik odpre recept (PU-01).<br>2. Uporabnik izbere oceno.<br>3. Sistem shrani oceno.<br>4. Sistem preračuna in prikaže novo povprečno oceno. |
| **Alternativni tokovi:** | 3a. Uporabnik je recept že ocenil: sistem posodobi obstoječo oceno.<br>3b. Uporabnik umakne svojo oceno: sistem jo izbriše in preračuna povprečje. |
| **Izjeme:** | Uporabnik ni prijavljen: preusmeritev na prijavo.<br>Ocena izven dovoljenega razpona: sistem jo zavrne.<br>Napaka baze: ocena ni shranjena, sistem prikaže napako. |

---

## PU-04: Komentiranje receptov

| | |
|---|---|
| **Primer uporabe:** Komentiranje receptov | **ID:** PU-04 |
| **Cilj:** | Uporabnik želi pod receptom objaviti komentar. |
| **Akterji:** | Uporabnik, sistem |
| **Predpogoji:** | Uporabnik je prijavljen; recept obstaja. |
| **Stanje sistema po PU:** | Komentar je shranjen in viden pod receptom (z avtorjem in časom). |
| **Scenarij:** | 1. Uporabnik odpre recept (PU-01).<br>2. Uporabnik vnese besedilo komentarja.<br>3. Uporabnik odda komentar.<br>4. Sistem validira in shrani komentar.<br>5. Sistem osveži seznam komentarjev. |
| **Alternativni tokovi:** | 3a. Uporabnik prekliče vnos: besedilo se zavrže.<br>4a. Komentar je prazen ali predolg: sistem prikaže opozorilo.<br>5a. Uporabnik izbriše svoj komentar: sistem ga odstrani. |
| **Izjeme:** | Uporabnik ni prijavljen: preusmeritev na prijavo.<br>Napaka baze: komentar ni shranjen, besedilo ostane v polju, sistem prikaže napako. |

---

## PU-05: Všečkanje receptov

| | |
|---|---|
| **Primer uporabe:** Všečkanje receptov | **ID:** PU-05 |
| **Cilj:** | Uporabnik želi izraziti, da mu je recept všeč (in ga po želji kasneje umakniti). |
| **Akterji:** | Uporabnik, sistem |
| **Predpogoji:** | Uporabnik je prijavljen; recept obstaja. |
| **Stanje sistema po PU:** | Všeček je shranjen (ali odstranjen) in števec všečkov posodobljen. |
| **Scenarij:** | 1. Uporabnik odpre recept (PU-01).<br>2. Uporabnik izbere »Všeč mi je«.<br>3. Sistem zabeleži všeček.<br>4. Sistem posodobi števec všečkov. |
| **Alternativni tokovi:** | 2a. Uporabnik je recept že všečkal: izbira umakne všeček in sistem zmanjša števec. |
| **Izjeme:** | Uporabnik ni prijavljen: preusmeritev na prijavo.<br>Podvojen zahtevek (dvojni klik): sistem upošteva le en všeček na uporabnika.<br>Napaka baze: stanje se ne spremeni, sistem prikaže napako. |
