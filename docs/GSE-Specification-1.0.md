# GSE — Gonsiorr Sailing Engine
## Główny dokument projektu

**Wersja specyfikacji:** 1.0  
**Status:** zatwierdzona przez użytkownika  
**Data punktu kontrolnego:** 2 listopada 2026  
**Nazwa moda:** Gonsiorr Sailing Engine  
**Skrót:** GSE  
**Docelowy katalog projektu:** `~/Desktop/GSE`

---

# 1. CEL PROJEKTU

Gonsiorr Sailing Engine (GSE) to mod do Minecraft Java 1.21.1, którego głównym celem jest dodanie katamaranu klasy Formula 18 (F18) wraz z rozbudowanym, możliwie realistycznym systemem żeglowania dostosowanym do mechaniki Minecrafta.

Mod ma obejmować:

- katamaran F18 z dwoma kadłubami;
- dwa miejsca dla graczy;
- sterowanie żaglami, fokiem, sterami i gennakerem;
- globalny system wiatru;
- fizykę ruchu, bezwładności, dryfu i oporu;
- kolizje z innymi łodziami i przeszkodami;
- zachowanie łodzi na brzegu;
- system wyposażenia i progresji komponentów;
- stację rzemieślniczą;
- crafting, serwis i personalizację;
- foiling i trapezy;
- HUD sternika;
- trwałe zapisywanie danych łodzi.

**Aktualny zakres projektu obejmuje wyłącznie F18.**

Nie implementować innych klas łodzi. Architektura może być modularna, ale nie należy rozszerzać aktualnego zakresu na inne jednostki.

# 2. ŚRODOWISKO TECHNICZNE

| Element | Wartość |
|---|---|
| Minecraft | Java 1.21.1 |
| Mod loader | Fabric |
| Java | 21 |
| System użytkownika | macOS |
| Komputer | MacBook Air M4 |
| Nazwa moda | Gonsiorr Sailing Engine |
| Skrót | GSE |
| Katalog projektu | `~/Desktop/GSE` |

Nie pytać ponownie o wersję Minecrafta, loader ani Javę.

# 3. ZASADY PROJEKTOWE

## 3.1. Nazewnictwo klas

| Element | Nazwa |
|---|---|
| Encja łodzi | `F18Entity` |
| ID encji | `f18` |
| Item łodzi | `F18Item` |
| Definicja łodzi | `F18BoatDefinition` |
| Model | `F18Model` |
| Renderer | `F18Renderer` |
| Rejestr | `BoatRegistry` |
| Silnik fizyki | `SailingPhysicsEngine` |
| Wyposażenie | `BoatEquipment` |
| Stacja craftingu | `SailingCraftingStation` |
| Menedżer wiatru | `WindManager` |
| HUD | `SailingHUD` |

Nie używać nazw `NacraEntity` ani nazw producentów/brandów w kodzie, identyfikatorach lub dokumentacji technicznej.

## 3.2. Zasady implementacji

- Nie nadpisywać istniejących plików przed sprawdzeniem ich zawartości.
- W pierwszej kolejności wykorzystać istniejące źródło V0.2.5, jeśli jest dostępne.
- Nie rozpoczynać projektu od zera bez sprawdzenia istniejącego kodu.
- Nie pytać użytkownika o ponowną akceptację każdego etapu.
- Rozwijać projekt etapami i wykonywać build/testy, gdy środowisko na to pozwala.
- Model wizualny i system hitboxów/kolizji są osobnymi systemami.
- Serwer odpowiada za autorytatywny stan łodzi i fizykę.
- Klient odpowiada za renderowanie i animacje.

## 3.3. Raportowanie statusu

Każdy element projektu musi być oznaczany jednym z czterech stanów:

1. **Zaprojektowane** — wymaganie zostało uzgodnione.
2. **Zaimplementowane** — istnieje w kodzie.
3. **Skompilowane** — build zakończył się powodzeniem.
4. **Przetestowane** — działanie zostało sprawdzone w grze.

Nie wolno twierdzić, że funkcja działa, jeśli została tylko opisana w specyfikacji.

# 4. MODEL F18

## 4.1. Technologia

Model ma być generowany bezpośrednio w Javie.

Na obecnym etapie:
- bez Blockbencha;
- bez zewnętrznego modelowania;
- bez tekstur;
- proste kolory materiałów;
- geometria blokowa zgodna ze stylem Minecrafta.

Model ma bazować na kuboidach, a nie realistycznej, gładkiej siatce.

Skala:

**1 jednostka modelu = 1 blok = 1 metr.**

## 4.2. Wymiary

| Parametr | Wartość |
|---|---:|
| Długość całkowita | 5,5 bloku |
| Szerokość całkowita | 2,6 bloku |
| Wysokość kadłuba | 0,5 bloku |
| Wysokość masztu | 9,5 bloku |
| Szerokość jednego kadłuba | 0,35–0,45 bloku |
| Masa referencyjna | 180 kg |

## 4.3. Elementy modelu

Każdy ruchomy element musi być osobną grupą z własnym punktem obrotu.

Elementy:

- `hull_left`
- `hull_right`
- `crossbeams`
- `trampoline`
- `mast`
- `mainsail`
- `jib`
- `rudders`
- `daggerboards`
- `bowsprit`
- `gennaker`

Hierarchia:

```text
F18
├── hull_left
├── hull_right
├── crossbeams
├── trampoline
├── mast
├── mainsail
├── jib
├── rudders
├── daggerboards
├── bowsprit
└── gennaker
```

Kadłuby są osobnymi, nieruchomymi grupami. Grot i fok muszą mieć możliwość animowania. Stery obracają się zależnie od sterowania. Gennaker pojawia się po rozłożeniu.

## 4.4. Pierwszy funkcjonalny build

Powinien zawierać:

1. `F18Entity`;
2. `F18Item`;
3. `F18Model`;
4. `F18Renderer`;
5. dwa kadłuby w skali 1:1;
6. maszt, poprzeczki i trampolinę;
7. grot i fok;
8. animowane żagle i stery;
9. dwa miejsca dla graczy;
10. podstawowe wsiadanie i wysiadanie.

# 5. ARCHITEKTURA

- `F18Entity` — stan łodzi, pasażerowie, ruch, wyposażenie i żagle.
- `F18Item` — stawianie i odzyskiwanie łodzi.
- `F18BoatDefinition` — parametry F18 i sloty.
- `BoatRegistry` — rejestracja.
- `SailingPhysicsEngine` — obliczenia fizyki.
- `BoatEquipment` — wyposażenie i kompatybilność.
- `SailingCraftingStation` — crafting, serwis i personalizacja.
- `WindManager` — globalny wiatr.
- `SailingHUD` — interfejs sternika.
- `F18Model` — geometria i punkty obrotu.
- `F18Renderer` — renderowanie i animacje.

# 6. FIZYKA ŻEGLOWANIA

## 6.1. Prędkość maksymalna kadłuba

| Tier | Prędkość maksymalna |
|---|---:|
| I | 13 bloków/s |
| II | 15 bloków/s |
| III | 16,5 bloku/s |
| IV | 18,5 bloku/s |
| V | 20 bloków/s |
| X — foiling | 24 bloki/s |

Pozostałe komponenty wpływają na osiąganie limitu kadłuba, ale nie dodają własnego limitu prędkości.

Wyjątek stanowią końcowe mnożniki wiatru i gennakera.

## 6.2. Przyspieszenie żagli

| Tier | Mnożnik |
|---|---:|
| I | 100% |
| II | 115% |
| III | 130% |
| IV | 145% |
| V | 160% |

## 6.3. Opór kadłuba

| Tier | Opór |
|---|---:|
| I | 100% |
| II | 95% |
| III | 90% |
| IV | 85% |
| V | 80% |

Wyższy Tier kadłuba zmniejsza opór wody.

Bezwładność jest osobnym parametrem i wynika z masy, wymiarów oraz korekty konstrukcyjnej.

## 6.4. Wpływ wyposażenia

- Miecze zmniejszają dryf boczny i boczną bezwładność.
- Maszt odblokowuje wyższe poziomy żagli i gennakera.
- Maszt daje niewielką poprawę przyspieszenia i wydajności.
- Maszt nie dodaje własnej prędkości maksymalnej.
- Ster poprawia reakcję na skręt i ogranicza straty prędkości podczas manewru.
- Trapezy nie dodają napędu.

## 6.5. Dynamika ruchu

- Mocne przyspieszenie przy niskich prędkościach.
- Przyspieszenie stopniowo słabnie w pobliżu prędkości docelowej.
- Prędkość docelowa zmienia się wraz z wiatrem, kursem, trymem i wyposażeniem.
- Zmiana wiatru natychmiast zmienia prędkość docelową, ale prędkość łodzi reaguje płynnie.
- Brak minimalnej prędkości.
- Przy zerowym wietrze brak napędu żaglowego.
- Łódź może wytracić prędkość i się zatrzymać.
- Brak biegu wstecznego.
- `S` luzuje żagle.
- Wiatr może powodować naturalny dryf wsteczny.

## 6.6. Wzór prędkości

```text
V_final = V_EQ * W_gennaker * W_wind
```

`V_EQ` obejmuje:
- wyposażenie;
- wydajność kursu;
- jakość trymu;
- straty hydrodynamiczne;
- wpływ mieczy.

Następnie nakładany jest mnożnik gennakera, a na końcu mnożnik wiatru.

Nie nakładać później limitu kadłuba, który unieważniałby uzgodnione mnożniki końcowe.

## 6.7. Kąt względem wiatru

- 0° — dokładnie pod wiatr.
- 180° — dokładnie z wiatrem.
- Martwa strefa ±7,5°.

Polara F18 bez gennakera:

| Kąt | Współczynnik |
|---|---:|
| 0–7,5° | ×0 |
| 7,5–30° | ×0,55–0,75 |
| 30–45° | ×0,75–0,90 |
| 45–60° | ×0,90–0,98 |
| 70° | ×1,00 |
| 70–90° | ×0,95–0,98 |
| 90–120° | ×0,90–0,97 |
| 120–150° | ×0,82–0,92 |
| 150–180° | ×0,70–0,82 |

Dokładne wartości pośrednie wymagają późniejszego balansu i testów.

## 6.8. Trym

Sterowanie:
- `W` — wybieranie żagli.
- `S` — luzowanie.
- Trym wspólny dla grota i foka.
- Zakres 0–100%.
- `W`: +10 punktów procentowych/s.
- `S`: −15 punktów procentowych/s.

Optymalny trym:

| Kąt | Optymalny trym |
|---|---:|
| 15° | 100% |
| 30° | 95% |
| 45° | 85% |
| 70° | 70% |
| 90° | 60% |
| 120° | 40% |
| 150° | 25% |
| 180° | 15% |

W martwej strefie brak optymalnego trymu.

Wydajność zależnie od odchylenia:

| Odchylenie | Mnożnik |
|---|---:|
| 0 pp | ×1,00 |
| 5 pp | ×0,99 |
| 10 pp | ×0,95 |
| 15 pp | ×0,88 |
| 20 pp | ×0,78 |
| 30 pp | ×0,55 |
| 40 pp | ×0,30 |
| ≥50 pp | ×0,10 |

Pasmo optimum jest węższe na ostrym bajdewindzie i fordewindzie, a szersze w półwietrze i baksztagu.

HUD pokazuje zielone pasmo optimum i kropkę aktualnego trymu. Bez liczb i strzałek.

# 7. GLOBALNY WIATR

## 7.1. Czas świata

Doba Minecrafta trwa 20 minut rzeczywistych:

- 10 minut dnia;
- 7 minut nocy;
- 1,5 minuty świtu;
- 1,5 minuty zmierzchu.

## 7.2. Harmonogram zmian

- Główna zmiana kierunku i siły raz na dobę, w środku nocy.
- Wymuszone losowanie początkowe o północy.
- Maksymalnie 3 dodatkowe zmiany siły na dobę.
- Dodatkowe zmiany następują w równych odstępach.
- Każda zmiana siły może wynosić maksymalnie ±1 Bft względem poprzedniej.
- Każde przejście trwa 60 sekund.

## 7.3. Zwykła pogoda

| Bft | Prawdopodobieństwo |
|---:|---:|
| 0 | 2% |
| 1 | 3% |
| 2 | 5% |
| 3 | 12% |
| 4 | 23% |
| 5 | 25% |
| 6 | 20% |
| 7 | 6% |
| 8 | 4% |
| 9–12 | 0% |

## 7.4. Burza

Przy rozpoczęciu burzy siła jest losowana natychmiast z rozkładu burzowego. Kierunek pozostaje bez zmian.

Przy zakończeniu burzy siła jest losowana natychmiast z rozkładu zwykłego. Kierunek pozostaje bez zmian.

Losowania burzowe są niezależne od harmonogramu regularnego.

| Bft | Prawdopodobieństwo |
|---:|---:|
| 4–6 | 20% |
| 7–8 | 40% |
| 9–10 | 30% |
| 11–12 | 10% |

## 7.5. Skala Kt

| Bft | Kt |
|---:|---:|
| 0 | <1 |
| 1 | 1–3 |
| 2 | 4–6 |
| 3 | 7–10 |
| 4 | 11–16 |
| 5 | 17–21 |
| 6 | 22–27 |
| 7 | 28–33 |
| 8 | 34–40 |
| 9 | 41–47 |
| 10 | 48–55 |
| 11 | 56–63 |
| 12 | ≥64 |

Bft 12 nie ma górnego limitu.

Konkretna wartość Kt jest losowana z przedziału. Bieżąca siła zmienia się płynnie do wartości docelowej przez 60 sekund.

Wyświetlane Bft wynika z bieżącego Kt.

Kierunek wiatru 0–360° zmienia się płynnie przez 60 sekund.

Fizyka korzysta z ciągłej wartości Kt, a nie tylko z Bft.

## 7.6. Krzywa wydajności wiatru

Orientacyjna krzywa dla kadłuba Tier I:

| Kt | Współczynnik |
|---:|---:|
| 0 | 0% |
| 5 | 65% |
| 10 | 78% |
| 15 | 88% |
| 20 | 96% |
| 24 | 100% |
| 27 | 110% |
| 30 | 115% |
| 35 | 115% |

Optymalny wiatr:

| Tier | Kt |
|---|---:|
| I | 24 |
| II | 23 |
| III | 21 |
| IV | 19 |
| V | 18 |
| X | 18 |

Maksymalny mnożnik: ×1,15.

# 8. GENNAKER

- Żagiel dodatkowy.
- Bez ręcznego trymu.
- Automatyczny idealny trym.
- Brak własnej krzywej napędu i statystyki trymu.
- Mnożnik zależy od kąta względem globalnego wiatru i siły Kt.
- Bez wiatru pozornego.
- Maksymalny mnożnik ×1,15.
- Neutralny mnożnik ×1.
- Na złym kursie może spaść do ×0.

Orientacyjnie:
- baksztag: +15%;
- fordewind: +10%;
- baksztagopolwiatr: +5%;
- półwiatr: ×1,00;
- ostry kurs pełny: współczynnik maleje nieliniowo do zera.

Dokładne granice kątowe wymagają finalizacji.

Sterowanie:
- `G` — rozłożenie/złożenie gennakera.
- Zwrot przez rufę automatycznie przenosi gennaker.
- Zwrot przez sztag przy rozłożonym gennakerze jest blokowany.
- Nie chować automatycznie przy złym kursie.
- Można rozłożyć przy zerowej prędkości.
- Zły kąt może wyhamować łódź lub ją zatrzymać.

# 9. ZAŁOGA

F18 ma dokładnie dwa miejsca:

1. Sternik — tył łodzi, przy sterach.
2. Pasażer — przód łodzi.

Zasady:
- Tylko sternik steruje.
- Tylko sternik obsługuje żagle, trym i kotwicę.
- Pasażer nie wpływa na napęd.
- Pasażer może wysiąść bez wpływu na ruch.
- Brak kary za samotne żeglowanie.
- Podczas zwrotów gracze płynnie zmieniają burty, zachowując role.

## 9.1. Wsiadanie

- Prawy przycisk myszy: wolne miejsce pasażera.
- `Shift` + prawy przycisk: miejsce sternika, jeśli wolne.
- Pasażer bez sternika może przejąć miejsce sternika przez `Shift` + prawy przycisk.
- Pasażer przy aktywnym sterniku nie może go wyrzucić.
- Sternik może dobrowolnie przejść na wolne miejsce pasażera.

## 9.2. Przekazanie steru

- Sternik używa `Shift` + prawy przycisk i przechodzi do tymczasowego miejsca pośredniego.
- Zachowuje kontrolę.
- Pasażer używa `Shift` + prawy przycisk.
- Role i miejsca zostają natychmiast zamienione.
- Jeśli pasażer nie zaakceptuje, sternik może wrócić tą samą akcją.
- Pasażer nie może sam wejść do miejsca pośredniego.
- Jeśli nie ma wolnego miejsca, zamiana nie następuje.
- Sternik może rozpocząć przekazanie przy dowolnej prędkości.
- Miejsce pośrednie nie zwiększa pojemności.

## 9.3. Wysiadanie

`Shift` + `X` — wysiadanie.

Jeśli sternik wysiada:
- żagle opuszczają się automatycznie;
- łódź zachowuje bezwładność;
- łódź stopniowo zwalnia;
- po zatrzymaniu automatycznie rzuca kotwicę.

Ponowne wejście sternika nie podnosi żagli automatycznie.

# 10. PRZECHYŁ, TRAPEZY I FOILING

## 10.1. Przechył

- Brak wywracania.
- Przechył wyłącznie wizualny.
- Nie wpływa na prędkość.

## 10.2. Trapezy

- Aktywacja przy 90% osiągalnej prędkości maksymalnej.
- Dezaktywacja poniżej 80%.
- Działają podczas samotnego żeglowania.
- Nie dodają napędu.

## 10.3. Foiling

Warunki:
- wszystkie sześć głównych komponentów Tier V;
- specjalny materiał Foiling Alloy;
- miecze L-foil Tier X zamiast standardowych V;
- bez nowej rudy i worldgenu.

Parametry:
- limit prędkości Tier X: 24 bloki/s;
- start foilingu przy 19,2 bloku/s;
- aktywacja trapezów/balastu przy 21,6 bloku/s;
- stabilizowany przechył 25° od 21,6 bloku/s.

Zachowanie:
- poniżej 19,2: kadłuby na wodzie;
- 19,2–21,6: foiling, załoga na trampolinie;
- od 21,6: trapezy i balast;
- poniżej 21,6, ale powyżej 19,2: foiling trwa, załoga wraca na trampolinę;
- poniżej 19,2: płynne zejście na wodę.

Oba kadłuby unoszą się. Miecz zawietrzny pozostaje zanurzony, a nawietrzny prawie wychodzi z wody. Kadłub nawietrzny znajduje się wyżej.

Skręty mogą chwilowo zmniejszać siłę nośną. Podczas zwrotów unoszenie przenosi się na drugi miecz.

Bez fal, losowego kołysania i losowej wysokości.

# 11. DRYF, BEZWŁADNOŚĆ I OPÓR

- Bez fal, prądów i wpływu głębokości.
- Rozróżnienie tylko woda / brak wody.
- Masa pasażera nie wpływa na fizykę.
- Przechył nie wpływa na prędkość.
- Znacząca bezwładność.
- Stopniowe wytracanie prędkości.
- Opór wody rośnie wraz z prędkością.
- Wyższy Tier kadłuba zmniejsza opór.
- Pozostałe komponenty nie zmieniają oporu kadłuba.
- Dryf boczny wynika z bocznej siły wiatru.
- Wektor ruchu może różnić się od kierunku dziobu.
- Miecze ograniczają dryf, ale go nie eliminują.
- Największy dryf na ostrym bajdewindzie.
- Znaczący dryf na ostrym kursie pełnym i w półwietrze.
- Umiarkowany w baksztagu.
- Niewielki w fordewindzie.
- Fizyka deterministyczna.

Wiatr może przesuwać łódź w bok, wstecz lub po skosie, jeśli sternik jest na pokładzie i nie ma napędu żaglowego.

Bez sternika łódź nie dryfuje od wiatru.

# 12. KOTWICA

- `H` — opuszczenie/podniesienie kotwicy.
- Można rzucić kotwicę w dowolnym momencie.
- Kotwica utrzymuje łódź w miejscu w wietrze.
- `R` i `H` działają niezależnie.
- Automatyczne kotwiczenie po wysiadaniu sternika następuje dopiero po całkowitym zatrzymaniu.
- Po ponownym wejściu kotwica pozostaje opuszczona.
- Żagle pozostają opuszczone, dopóki gracz nie podniesie ich ręcznie.

# 13. KOLIZJE I LĄD

Kolizje uwzględniają:
- masę;
- wymiary;
- prędkość względną;
- kąt uderzenia;
- geometrię kontaktu;
- wektor i impuls.

Zasady:
- zmieniają się kierunek i prędkość;
- brak uszkodzeń i zużycia;
- po kolizji ruch jest zachowany;
- opór wody stopniowo wyhamowuje łódź;
- kotwica ogranicza ruch, ale nie tworzy niewidzialnej ściany;
- zakotwiczona łódź może zostać przesunięta lub obrócona po kolizji.

## 13.1. Płaska linia brzegowa

| Prędkość | Przesunięcie |
|---|---:|
| 1× | 1 blok |
| 2× | 2 bloki |
| 3× | 3 bloki |
| 4× | 4 bloki |

Liniowe wyhamowanie: 1× na blok.

Poniżej 1× łódź zatrzymuje się na krawędzi wody.

## 13.2. Przeszkody

- Przeszkoda ponad linią wody: natychmiastowe zatrzymanie.
- Blok na poziomie lub poniżej kadłuba: zatrzymanie i dźwięk uderzenia o drewno.
- Wystający element powyżej kadłuba: fizyczne odbicie zależne od wektora uderzenia.
- Nie niszczyć bloków.
- Nie wyrzucać graczy z łodzi.

# 14. TRWAŁOŚĆ DANYCH

- Łódź nie despawnuje.
- Przetrwa restart serwera i wylogowanie.
- Odzyskanie daje jeden skonfigurowany `F18Item`.
- Brak osobnego systemu pickupów.
- Zachować kompatybilność z Carry On, jeśli możliwa.
- Zachować komponenty, Tier, personalizację, nazwę, gennaker, kotwicę i miejsca.
- Bez utraty i duplikacji komponentów.
- Każdy może odzyskać łódź.
- Nazwa jest kosmetyczna i zapisywana w danych łodzi.
- Nazwa zachowuje się jak nazwa przedmiotu z zaklęciem.
- Brak trwałości, uszkodzeń, napraw i zużycia.

# 15. WYPOSAŻENIE

## 15.1. Główne komponenty

1. Kadłub I–V.
2. Miecze I–V; X L-foil zastępuje V.
3. Grot I–V.
4. Fok I–V.
5. Maszt I–V.
6. Ster I–V.

## 15.2. Dodatki

- Trampolina — obowiązkowa, bez Tier i statystyk.
- Trapezy — opcjonalne, binarne, bez Tier i statystyk.
- Bukszpryt III–V, bez statystyk.
- Gennaker I–III.
- Foiling Alloy.
- L-foil X.

Gennaker wymaga masztu i bukszprytu. Maksymalny Tier gennakera jest niższym z Tier masztu i bukszprytu, z limitem III.

## 15.3. Kompatybilność

- Tier kadłuba ogranicza maksymalny Tier masztu, mieczy i sterów.
- Tier masztu ogranicza Tier grota, foka i gennakera.
- Tier bukszprytu ogranicza Tier gennakera.
- F18 startuje jako Tier I z pustymi slotami.

## 15.4. Progresja

- Tier I — od początku.
- Tier II — po wykonaniu Tier I.
- Tier III — po wykonaniu Tier II.
- Tier IV — po odkryciu materiałów Netheru.
- Tier V — po odkryciu materiałów Endu.
- Tier X — po odkryciu technologii foilingu.

Tier IV wymaga Overworld + Nether.

Tier V wymaga Overworld + Nether + End.

Nie dodawać nowych rud worldgen. Nie używać Mythic Metals.

# 16. STACJA RZEMIEŚLNICZA

Jedna stacja: `SailingCraftingStation`.

Trzy zakładki:

## 16.1. Crafting

- Własna siatka 5×5.
- Slot wyniku.
- Receptury GSE.
- Integracja EMI z kategorią `Sailing Workshop`, jeśli możliwa.
- EMI pokazuje układ 5×5, wynik i wymóg stacji.

## 16.2. Serwis

- Jeden slot wejściowy na łódź.
- Dynamiczne sloty kompatybilnych komponentów.
- Instalowanie, wymiana i zdejmowanie.
- Walidacja kompatybilności.
- Przeliczenie statystyk.
- Operacje tylko przy zatrzymanej łodzi i bez załogi.
- Zdjęty komponent wraca jako dokładny przedmiot z personalizacją.

## 16.3. Personalizacja

- Grot i fok personalizowane niezależnie.
- Barwnik ustawia kolor bazowy.
- Możliwość wzorów banerowych.
- Jeden slot barwnika/wzoru i jeden komponent naraz.
- Trampolina i trapezy mają warianty kolorystyczne.
- Nazwa łodzi jest zapisywana w danych łodzi.
- Personalizacja nie wpływa na statystyki.
- Wyjęty żagiel zachowuje własny wygląd.
- Nowy żagiel nie dziedziczy personalizacji starego.

# 17. MATERIAŁY I RECEPTURY

Poniższe receptury są szkicem zatwierdzonego balansu. Nie są potwierdzone jako zaimplementowane.

Dokładne układy 5×5 nie zostały jeszcze zatwierdzone.

## 17.1. Materiały bazowe

| Materiał | Składniki | Wynik |
|---|---|---:|
| Fiberglass | 4 sand + 2 glass + 1 iron | 4 |
| Epoxy resin | 2 coal + 1 glass + 1 honey bottle | 2 |
| Glass laminate | 2 fiberglass + 1 epoxy + 1 iron | 2 |
| Reinforcing fiber | 2 fiberglass + 1 string + 1 copper | 2 |
| Metal fittings | 3 iron + 1 copper | 4 |

## 17.2. Materiały Netheru

| Materiał | Składniki | Wynik |
|---|---|---:|
| Heat-resistant fiber | 2 Nether quartz + 2 Blaze powder + 1 glass | 2 |
| Nether resin | 1 magma cream + 1 Blaze powder + 1 honey bottle | 2 |
| Nether composite | 1 heat fiber + 1 nether resin + 1 iron | 1 |

## 17.3. Materiały Endu

| Materiał | Składniki | Wynik |
|---|---|---:|
| End fiber | 2 chorus fruit + 1 ender pearl + 1 phantom membrane | 2 |
| End resin | 1 chorus fruit + 1 ender pearl + 1 honey bottle | 2 |
| End composite | 1 end fiber + 1 end resin + 1 nether composite | 1 |

## 17.4. Kadłub

- I: 8 laminates, 4 fittings, 4 oak planks.
- II: 6 laminates, 4 reinforcing fibers, 6 fittings.
- III: 8 laminates, 6 reinforcing fibers, 8 fittings.
- IV: kadłub III + 1 nether composite + 6 heat fibers + 4 nether resin + 8 fittings.
- V: kadłub IV + 1 end composite + 6 end fibers + 4 end resin + 8 fittings.

## 17.5. Miecze

- I: 4 laminates, 4 fittings, 2 reinforcing fibers.
- II: 5 laminates, 4 reinforcing fibers, 4 fittings.
- III: 6 laminates, 6 reinforcing fibers, 4 fittings.
- IV: miecze III + 1 nether composite + 4 heat fibers + 3 nether resin.
- V: miecze IV + 1 end composite + 4 end fibers + 3 end resin.

## 17.6. Grot

- I: 6 fiberglass, 4 string, 2 epoxy.
- II: 8 fiberglass, 6 string, 3 epoxy.
- III: 8 fiberglass, 8 reinforcing fibers, 4 epoxy.
- IV: grot III + 1 nether composite + 5 heat fibers + 3 nether resin.
- V: grot IV + 1 end composite + 5 end fibers + 3 end resin.

## 17.7. Fok

- I: 4 fiberglass, 3 string, 1 epoxy.
- II: 5 fiberglass, 4 string, 2 epoxy.
- III: 6 fiberglass, 4 reinforcing fibers, 2 epoxy.
- IV: fok III + 1 nether composite + 3 heat fibers + 2 nether resin.
- V: fok IV + 1 end composite + 3 end fibers + 2 end resin.

## 17.8. Maszt

- I: 8 planks, 6 fittings, 2 reinforcing fibers.
- II: 8 planks, 8 fittings, 4 reinforcing fibers.
- III: 6 laminates, 8 fittings, 4 reinforcing fibers.
- IV: maszt III + 1 nether composite + 4 heat fibers + 4 nether resin.
- V: maszt IV + 1 end composite + 4 end fibers + 4 end resin.

## 17.9. Ster

- I: 4 laminates, 6 fittings, 2 reinforcing fibers.
- II: 5 laminates, 8 fittings, 3 reinforcing fibers.
- III: 6 laminates, 8 fittings, 4 reinforcing fibers.
- IV: ster III + 1 nether composite + 4 heat fibers + 3 nether resin.
- V: ster IV + 1 end composite + 4 end fibers + 3 end resin.

## 17.10. Bukszpryt

- III: 4 laminates, 6 fittings, 2 reinforcing fibers.
- IV: bukszpryt III + 1 nether composite + 3 heat fibers + 3 nether resin.
- V: bukszpryt IV + 1 end composite + 3 end fibers + 3 end resin.

## 17.11. Gennaker

- I: 6 fiberglass, 3 string, 2 epoxy.
- II: gennaker I + 4 reinforcing fibers + 3 epoxy.
- III: gennaker II + 1 nether composite + 4 heat fibers + 2 nether resin.

## 17.12. Dodatki

- Trampolina: 6 string, 4 planks, 2 leather.
- Trapezy: 4 chains, 4 iron ingots, 2 string.
- Foiling core: 2 end composites + 2 diamonds + 1 netherite ingot → 1.
- Foiling Alloy: 1 core + 2 ender pearls + 2 prismarine shards + 2 gold ingots → 1.
- L-foil X: miecze V + 2 Foiling Alloy + 2 end composites + 4 end fibers + 2 end resin + 4 fittings.

Nie używać Dragon Breath ani Shulker Shells.

# 18. HUD I STEROWANIE

## 18.1. HUD

HUD znajduje się u góry ekranu i jest widoczny wyłącznie dla sternika.

Pokazuje:
- prędkość w blokach/s;
- wiatr w Kt i Bft;
- kierunek wiatru;
- kierunek dziobu;
- kąt względem wiatru;
- pasek trymu;
- zielone pasmo optimum;
- kropkę aktualnego trymu;
- wychylenie steru od 100% w lewo przez 0 do 100% w prawo;
- stan żagli;
- stan gennakera;
- stan kotwicy.

Bez ostrzeżeń, porad i komunikatów podpowiadających graczowi, co ma zrobić.

## 18.2. Sterowanie
}:��$z{-���jם�����4�����"��5B�%TDDU"��F�W"��bbF�W"��S��66RDttU$$�$E2���F�W"��bbF�W"��R���F�W"����66RE$��Ĕ�R�E$U�U2�d��Ĕ�u�������F�W"����66R$�u5$�B��F�W"��2bbF�W"��S��66RtT���U"��F�W"��bbF�W"��3��Ӱ�Ч�