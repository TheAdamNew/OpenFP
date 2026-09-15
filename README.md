# OpenFortunePillars

An open-source Gradle build of [FortunePillars](https://modrinth.com/plugin/fortunepillars) - an advanced Minecraft minigame plugin for Paper 1.21+ servers.

## About

FortunePillars is a high-altitude Battle Royale minigame where players fight on floating pillars with random loot drops, multiple game modes, and cosmetics. This repository contains a buildable Gradle project reconstructed from the original JAR, allowing the community to compile, study, and contribute to the plugin.

**Original Plugin:** [modrinth.com/plugin/fortunepillars](https://modrinth.com/plugin/fortunepillars)

## Features

- **Multiple Game Modes** - Speed UHC, Lava Rising, Border Shrink, TNT Rain, Swapper, Shuffle
- **Voting System** - Players vote for game modes and loot modes before each match
- **Custom Loot Tables** - Configurable loot with Normal, Balanced, and OP tiers
- **Arena Management** - Create, manage, and auto-reset arenas with schematics
- **Cosmetics** - Cage colors and win effects
- **Leaderboards & Stats** - SQLite/MySQL-backed player statistics
- **Scoreboard** - Real-time game info display
- **PlaceholderAPI** support

## Building

Requires **Java 21+** and an internet connection (for dependency resolution).

```bash
./gradlew build
```

The output JAR will be at `build/libs/FortunePillars-4.5.0.jar`.

## Installation

1. Build the JAR or download it from [Releases](https://github.com/TheAdamNew/OpenFP/releases)
2. Place the JAR in your server's `plugins/` folder
3. Restart or reload the server
4. Configure via `plugins/FortunePillars/config.yml`

## Commands

| Command | Description |
|---------|-------------|
| `/tof` | Main command (aliases: `/fortunepillars`, `/fp`, `/arena`) |
| `/tof join` | Join an arena |
| `/tof leave` | Leave current arena |
| `/tof vote` | Vote for a game mode |
| `/tof create <name>` | Create a new arena (admin) |
| `/tof reload` | Reload configuration (admin) |
| `/lootdebug` | Loot system debug (admin) |
| `/dbdebug` | Database debug (admin) |

## Tech Stack

- **Java 21** (target bytecode)
- **Paper API 1.21.1** (server platform)
- **Gradle 9.7** (build system)
- **HikariCP** (database connection pooling)
- **SQLite / MySQL** (player data storage)
- **PlaceholderAPI** (optional, for placeholder support)

## License

This is a community reconstruction for educational purposes. The original FortunePillars plugin is developed by **geturplugins**.
