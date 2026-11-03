# Gonsiorr Sailing Engine (GSE)

A Fabric mod for Minecraft Java 1.21.1 focused on a two-seat Formula 18 catamaran and sailing simulation.

## Project status

The approved feature baseline is [GSE Specification 1.0](docs/GSE-Specification-1.0.md). Current implementation and verification status is tracked in [IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md), with handoff notes in [WORK_HISTORY.md](docs/WORK_HISTORY.md).

Source archives for releases v0.2 through v0.2.5 were supplied separately. The latest archive has not yet been merged; see the status document before using it as a code baseline.

## Build

Requirements: Java 21 and the Gradle wrapper included in this repository.

```sh
./gradlew build
```

The mod JAR is produced under `build/libs/`.
