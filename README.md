# Space Telemetry Monitor (STM)

A focused Java 17 command‑line project that:

1. Streams telemetry rows from a CSV (timestamp, satellite id, numeric sensor readings).
2. Wraps each row in an immutable `TelemetryPacket`.
3. Optionally loads limit rules (yellow / red thresholds) from a JSON file.
4. Evaluates each packet; emits alerts for out‑of‑range values.

It’s intentionally small but structured like a production service. Good for demonstrating core skills: build tooling (Maven + Shade), clean packages, defensive modeling, streaming IO, JSON config, a tiny rule engine, unit tests (JUnit 5), and reproducible packaging.

---

## Quick Start

```bash
# Build (runs tests + creates fat jar target/stm.jar)
mvn clean package

# Run without rules (just echo packets)
java -jar target/stm.jar telemetry-samples/sample.csv

# Run with limits
java -jar target/stm.jar telemetry-samples/sample.csv --limits limits-example.json
```

If you only want to re-run tests:

```bash
mvn test
```

---

## Example Limits (JSON)

`limits-example.json`:

```json
[
  { "sensor": "power_bus_voltage", "yellowLow": 33.0, "redLow": 32.0 },
  { "sensor": "board_temp", "yellowHigh": 45.0, "redHigh": 50.0 },
  { "sensor": "cpu_load", "yellowHigh": 85.0, "redHigh": 95.0 }
]
```

Threshold semantics:

- _yellowLow_: below this -> YELLOW (early warning).
- _redLow_: below this -> RED (critical).
- _yellowHigh_ / _redHigh_: analogous on the high side.
- Any field may be `null` (no bound on that side).

---

## Sample CSV

`telemetry-samples/sample.csv`:

```
timestamp,sat_id,power_bus_voltage,board_temp,cpu_load
2025-07-16T18:00:01Z,SAT-42,34.8,41.3,78.1
2025-07-16T18:00:11Z,SAT-42,34.7,41.4,79.0
2025-07-16T18:00:21Z,SAT-42,34.6,41.2,78.6
```

All nominal with the default limits (no alerts). To demo alerts, temporarily edit a line, e.g.:

```
2025-07-16T18:00:11Z,SAT-42,31.5,41.4,91.0
```

Then run with limits to see:

```
TelemetryPacket{2025-07-16T18:00:01Z,SAT-42,{power_bus_voltage=34.8, cpu_load=78.1, board_temp=41.3}}
TelemetryPacket{2025-07-16T18:00:11Z,SAT-42,{power_bus_voltage=31.5, cpu_load=91.0, board_temp=41.4}}
RED ALERT sat=SAT-42 sensor=power_bus_voltage value=31.500 Y[33.0,null] R[32.0,null]
YELLOW ALERT sat=SAT-42 sensor=cpu_load value=91.000 Y[null,85.0] R[null,95.0]
...
```

(Formatting depends on the `Alert.toString()` implementation.)

---

## Code Layout

```
src/main/java/com/example/stm/
  Main.java                # CLI entry
  model/TelemetryPacket.java
  parser/CsvPacketParser.java
  rules/
    LimitRule.java
    Alert.java
    AlertSink.java
    ConsoleAlertSink.java
    RuleEngine.java
  util/JsonConfigLoader.java
src/test/java/com/example/stm/
  parser/CsvPacketParserTest.java
  rules/LimitRuleTest.java
  rules/RuleEngineTest.java
telemetry-samples/sample.csv
limits-example.json
```

---

## Key Classes (brief)

**TelemetryPacket** – Immutable (timestamp, satId, Map\<String,Double> measurements). Defensively copies map to prevent external mutation.

**CsvPacketParser** – Uses OpenCSV under the hood. Exposes a `Stream<TelemetryPacket>` so large files can be processed lazily. Caller closes via try-with-resources.

**LimitRule** – Holds optional low/high yellow/red bounds; `evaluate(double)` returns `"RED"`, `"YELLOW"`, or `null`.

**RuleEngine** – Maps sensor → LimitRule once (HashMap). For each packet, checks only sensors present in the packet to keep evaluation O(nSensorsInPacket).

**Alert / AlertSink** – Simple record + pluggable interface; current sink prints to console. (Easy extension: file, HTTP, queue, etc.)

**JsonConfigLoader** – One static method using Jackson to map JSON array → `List<LimitRule>`.

---

## Testing

Tests cover:

- CSV parsing happy path (row count + specific values).
- Limit rule evaluation (red/yellow boundary logic both sides).
- Rule engine producing alerts (captures alerts via an in-memory sink).

Run with `mvn test`. Surefire is configured so relative test resources resolve consistently.

---

## Build & Packaging

The Shade plugin creates a self‑contained `stm.jar` including dependencies and a manifest `Main-Class`. This makes execution uniform across machines (no classpath wrangling). The original thin jar is still produced but not needed for normal use.

---

## Design Choices (short notes)

- **Records** for simple data carriers (`TelemetryPacket`, `LimitRule`, `Alert`) keep code concise and clarify immutability.
- **Streaming parse** avoids loading entire file into memory; scales if the CSV grows.
- **Explicit thresholds** instead of dynamic stats keeps logic easy to inspect.
- **Pluggable sink** demonstrates inversion of control for side effects.
- **Fat jar** chosen over shell scripts to simplify running on another reviewer’s machine.

---

## Possible Extensions

| Idea                  | Description                                    | Effort |
| --------------------- | ---------------------------------------------- | ------ |
| FileAlertSink         | Append alerts to `alerts.log`                  | Low    |
| JSON/CSV alert report | Write summary at end                           | Low    |
| Rate-of-change rule   | Trigger if delta exceeds threshold             | Medium |
| Rolling stats         | Compute mean / stddev to flag anomalies        | Medium |
| REST ingestion        | Wrap with Spring Boot, accept live POSTs       | Higher |
| Metrics export        | Expose Prometheus counters (alert counts etc.) | Medium |
| CI workflow           | GitHub Actions running `mvn test` on push      | Low    |

---

## Usage Recap

```bash
mvn clean package
java -jar target/stm.jar telemetry-samples/sample.csv --limits limits-example.json
```

Edit the CSV to force alerts if you want to showcase the engine.

---

## License / Status

No license specified yet (personal demo / interview project). Feel free to read; not production‑hardened.

---

## Contact

Questions / feedback welcome.
