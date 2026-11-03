# GSE implementation status

The approved baseline is [GSE-Specification-1.0.md](GSE-Specification-1.0.md). The later control change takes precedence over the original table: arrow keys steer/trim, Ctrl + right-click selects or transfers the helm. Plain right-click boards as passenger.

## Source provenance

- The project already present at `~/Desktop/GSE` was inspected before editing.
- The user has now supplied source archives v0.2 through v0.2.5. The v0.2.5 archive is the newest reference; it has not yet been compared with or merged into this project. `work/prototype-before-spec` is an earlier Fabric/vanilla-Boat skeleton.
- Work continues from the existing Fabric project. Do not assume this tree came from the supplied v0.2.5 archive.

## Implemented in source; pending build and game verification

- `F18Entity` is a custom `Entity`, not a vanilla `Boat`; its server-side movement is driven by `SailingPhysicsEngine`.
- Sailing uses the shared server wind, polar, trim, tier limits, acceleration, hull drag, rudder, daggerboards, lateral wind drift, gennaker, and anchor.
- Water support is sampled under both hulls. On dry ground the boat falls to terrain and slows according to the specified 1–4 m/s beaching distances; solid obstacles clip movement and produce a wood impact sound.
- Boat-to-boat/entity contact uses relative speed, mass, contact side, and an anchor displacement response. There is no block damage.
- The F18 has two seats, a stern skipper and forward passenger on opposite hulls. Ctrl + right-click selects/requests/accepts the helm; body posture follows tack-side movement while the camera remains free.
- New boats start with raised sails. Leaving after being skipper lowers sails and gennaker, lets the boat coast, then anchors it. Re-entering does not raise sails or lift the anchor.
- The item places and recovers one configured F18 and preserves equipment, sail state, trim, colors, banner-pattern selections, anchor, and name.
- The global wind includes the specified Beaufort distributions, storm changes, daily direction/strength transitions, continuous knots, and client sync.
- Arrow-key controls, remappable key mappings, skipper HUD, dynamic sail/rudder/gennaker visuals, foil lift/heel, trapezes, and ballast are present.
- Tiered equipment, compatibility checks, progression recipes, 5x5 shapeless workshop crafting, service, dye/pattern personalization, and direct workshop name entry are present.

## Scope limits to report accurately

- The supplied v0.2.5 archive has not yet been compared with the current project tree.
- The specification did not approve exact 5x5 recipe layouts, so recipes currently accept shapeless placement in the 5x5 grid.
- Banner patterns are retained as data and drawn as procedural geometric approximations on the untextured sails.
- EMI and Carry On are not installed in the Prism GSE instance; their optional integrations are not included.
- Shoreline/contact values and sailing balance still need in-game tuning. Source presence is not proof that these behaviors work.

## Ostatni zapisany punkt kontrolny (2–3 listopada 2026)

Build `./gradlew --offline build` zakończył się powodzeniem. JAR został skopiowany do instancji Prism Launcher `GSE`, a jego suma SHA-256 zgadzała się z artefaktem builda. Gra uruchomiła się z modem. Nie udało się ręcznie sprawdzić interakcji w świecie, dlatego wsiadanie, kolizje oraz ustawianie F18 przedmiotem pozostają **nieprzetestowane**. Szczegóły wcześniejszych sesji i następne kroki: [WORK_HISTORY.md](WORK_HISTORY.md).

Użytkownik dostarczył archiwa od v0.2 do v0.2.5. Najnowsze v0.2.5 nie zostało jeszcze porównane ani scalone z tym drzewem; nie przypisujemy mu autorstwa obecnego kodu.

