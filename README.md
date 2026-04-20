# Craigslist Housing UI tests (Java + Playwright)

This repository contains a small, maintainable **Java + Playwright** UI automation framework targeting:

- Site: `https://madrid.craigslist.org/`
- Page: **Housing**
- Verification: open site → English → Housing → sort **lowest→highest** & **highest→lowest** → read visible € prices → compare to that list sorted **ascending** & **descending** respectively (must match).
- Verification: open site → English → Housing → sort **Neswest-First** & **Oldest-First** → retrieve dates → compare to that list sorted **ascending** & **descending** respectively (must match).
## Prerequisites
- Java 21+
- Maven 3.9+

## Install Playwright browsers (one-time)

```bash
mvn -q test -DskipTests
mvn -q exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

## Run tests

```bash
mvn -q test
```

### Debug mode (headed + slow motion)

```bash
mvn -q test -Dheadless=false -DslowMo=100
```

## Artifacts (debugging failures)
- On test failure, the framework saves:
  - `screenshot.png`
  - `trace.zip` (open via `npx playwright show-trace trace.zip`)
  - Playwright **video** directory (recorded during the test)

Artifacts are stored under:
- `target/artifacts/<testName>/<timestamp>/`

## Configuration
All settings are available as system properties:
- `-DbaseUrl=https://madrid.craigslist.org`
- `-Dheadless=true|false`
- `-DslowMo=0`
- `-DtimeoutMs=30000`
- `-DartifactsDir=target/artifacts`

