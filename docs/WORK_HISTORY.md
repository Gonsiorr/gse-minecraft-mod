# GSE work history

This project continues work from the approved [GSE Specification 1.0](GSE-Specification-1.0.md). Detailed implementation and verification status is tracked in [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md).

## Decisions carried forward

- Build a Fabric mod for Minecraft Java 1.21.1 with Java 21, focused on the F18 catamaran.
- Preserve and inspect existing work before editing; do not restart the project without cause.
- Keep authoritative boat physics and state on the server, with client-side input, rendering, and animation.
- Current helm controls: arrow keys steer/trim; Ctrl + right-click selects or transfers helm; regular right-click boards as passenger.
- Report design, implementation, successful compilation, and in-game verification as distinct states.

## Recent implementation checkpoint (2–3 November 2026)

The previous work added server-validated boarding, reduced unwanted player pushing, widened boarding range checks for the long hull, corrected the workshop recipe JSON, and updated F18 item placement and its default name. The Gradle build succeeded and the JAR was copied to the Prism Launcher instance named GSE. The game started with the mod, but manual interaction tests in the world remain outstanding.

The user supplied source archives v0.2 through v0.2.5. The newest archive has not been merged; compare it with this project and the approved specification before using it as a baseline.
