# UltimateRandomRoulette v1.4.0 (MC 1.21.1 / Java 21)

- Tested for Spigot/Paper 1.21.1 API; loads on Arclight (Bukkit layer) as well.
- Java 21 target (maven-compiler release=21)
- Spinner GUI protection, weight-based roulette, package rewards
- Commands: `/랜덤`, `/패키지` (aliases: rr, rnd, pkg)

**Build locally**
```bash
mvn -B -DskipTests package
```

**GitHub Actions**
- Uses Temurin 21 and uploads the built JAR as an artifact.

**Server compatibility**
- Any Bukkit-compatible server for 1.21.1 (Paper/Spigot).  
- Arclight runs Bukkit plugins; place the built JAR into `plugins/`.
