# Changelog

## [Unreleased]

## [2.2.5] - 2026-08-12

### Changed

- Moved Quantum Computer capacity, multiplier, accelerator, and reservation
  accounting to separate revisions. Structure values are rebuilt once after a
  formation change; repeated coprocessor and GUI reads reuse the cached result.
- Added an atomic host snapshot boundary for ACO v3. When available, AQE reads
  capacity, reservations, availability, job counts, and backend state together
  instead of issuing separate reflection getters.
- Added bounded revision diagnostics for structure scans, reservation rebuilds,
  host snapshot reads, host reconciles, and presentation reuse.

### Fixed

- Delayed optional ACO host release until final world persistence completes.
- Kept closed hosts readable for final capacity snapshots and NBT persistence
  while rejecting new reconciliation work.

## [2.2.4] - 2026-08-10

### Changed

- Synchronized the NeoForge 1.21.1 artifact version with the Forge 1.20.1
  AQE 2.2.4 maintenance release.
- Kept the NeoForge runtime implementation unchanged because the AE2-UELM
  compatibility profile added by 2.2.4 applies only to Forge 1.20.1.
- Raised the optional compatible ACO minimum to 1.5.9.

## [2.2.3] - 2026-08-08

### Changed

- Published the NeoForge 1.21.1 build with the same AQE mod version `2.2.3`.
- Kept the Minecraft version (`1.21.1`) separate from the AQE mod version and
  artifact suffix.

## 2.3.1 - 2026-08-08

### Changed

- Added lifecycle ownership and opaque-state quarantine for the optional
  BigInteger crafting host.

## 2.3.0 - 2026-08-02

### Added

- Added the NeoForge 1.21.1 release line using Java 21.
- Added pinned support for AE2 19.2.17, Advanced AE 1.6.11-1.21.1, and
  AE2 Omni Cells 1.1.6.

### Changed

- Migrated the production build and resource layout to NeoForge 21.1.247.
- Updated the optional BigInteger host integration range to ACO 1.6.x.
- Kept AQE 2.2.x and its release artifacts as the Forge 1.20.1 line.

## 2.2.2 - 2026-07-29

### Fixed

- Validated the server-authoritative capacity display ledger before encoding
  it, including non-negative bounds, reservation limits, and
  `total = used + available`.
- Rejected inconsistent exact long-range display markers during decoding.
- Kept an overbooked backend from emitting a negative or internally
  inconsistent GUI snapshot while logging the backend mismatch once.
- Logged AQE's own loaded version and selected optional backend contract during
  startup so a stale or locally replaced JAR is identifiable.

## 2.2.1 - 2026-07-28

### Changed

- Increased the default BigInteger Quantum Core raw capacity from
  `10^64 - 1` bytes to `10^1024 - 1` bytes while preserving the configurable
  16,372-digit raw-core limit and exact 16,384-digit structure ceiling.
- Reobfuscated the production JAR as part of the normal Gradle build so
  Minecraft references use their runtime names in distributed artifacts.

## 2.2.0 - 2026-07-24

### Added

- Added server-authoritative active-job and BigInteger-parent-job counts to the
  bounded Quantum Computer display snapshot.
- Added optional reflection support for ACO runtime job-count methods while
  preserving compatibility with earlier API v3 implementations that do not
  expose them.
- Extended optional ACO API v3 compatibility to `[1.3.0,1.6.0)`, including
  ACO 1.5.x plan-sidecar and exact-inventory implementations.

### Changed

- Kept ACO optional. When compatible ACO 1.4.x or 1.5.x is present and
  enabled, AQE can own exact BigInteger parent jobs and their checked-long
  Advanced AE child windows; without ACO, the existing saturated-long fallback
  remains active.
- Excluded ACO-managed child windows from the displayed job total so one parent
  is not presented as both a BigInteger job and a temporary standard job.

## 2.1.2 - 2026-07-23

### Changed

- Replaced AE2's fixed `k` crafting CPU capacity label with automatic
  `B / k / M / G / T / P / E` promotion throughout the signed-long range.
- Applied the same long-capacity formatting to AE2 and Advanced AE CPU lists.
- Changed values above `Long.MAX_VALUE` to a bounded scientific notation based
  on the actual server-synchronized capacity, such as `9.999 x 10^63 B`.
- Kept capacity accounting, reservations, and crafting behavior unchanged.

## 2.1.1 - 2026-07-23

### Changed

- Replaced the fixed `10^N - 1 B` BigInteger CPU label with a server-authoritative
  live snapshot of total, reserved, and available Quantum Computer capacity.
- Made the CPU list show currently reserved capacity while tooltips show the
  complete total/reserved/available state.
- Included summed storage blocks and Data Entangler multiplication in the value
  shown to players instead of formatting the raw core Config alone.
- Kept the display marker bounded to at most 19 leading digits per value, so a
  16,384-digit capacity does not produce a 16,384-character sync payload.
- Kept values through the signed-long range exact and used a leading-digit plus
  total-digit representation only for larger values.

## 2.1.0 - 2026-07-22

### Changed

- Aligned AQE with ACO's exact `10^16384 - 1` BigInteger ceiling.
- Limited the raw BigInteger core Config to 16,372 digits, reserving 12 decimal
  digits for summed structure storage and Data Entangler multiplication.
- Applied the exact ceiling to capacity arithmetic, local fallback accounting,
  and AQE host-state NBT instead of relying on a bit-length approximation.
- Extended optional ACO API v3 compatibility to `[1.3.0,1.5.0)`, including
  ACO 1.4.x, without making ACO a required dependency.

## 2.0.2 - 2026-07-19

### Changed

- Moved AQE configuration from per-world `serverconfig` directories to
  `config/advanced_quantum_engineering.toml`.
- Consolidated the generated TOML into three readable sections:
  `quantum_computer`, `endgame_cores`, and `safety_and_diagnostics`.
- Added one-time migration of existing per-world AQE values. The legacy file is
  retained with a `.migrated` suffix after a successful migration.
- Kept all registry IDs and default gameplay values unchanged.

## 2.0.1 - 2026-07-18

### Fixed

- Accepted compatible optional ACO releases across the declared
  `[1.3.0,1.4.0)` range instead of rejecting every version except `1.3.0`.
- Kept ACO optional while retaining the API v3 field and reflected-method
  validation as the authoritative compatibility boundary.
- Updated documentation for ACO `1.3.1` interoperability.

## 2.0.0 - 2026-07-18

The release artifact was clean-built and qualified through Forge client bootstrap
and Arclight dedicated-server startup with ACO 1.3.0 on the documented stack.

### Added

- Added `advanced_quantum_engineering:big_integer_quantum_core`.
- Added checked BigInteger aggregation for the complete Advanced AE Quantum Computer capacity.
- Added exact physical, reserved, and available capacity accounting across multiple active Quantum Computer jobs.
- Added a configurable `10^digits - 1` capacity with a default of 64 decimal digits.
- Added versioned BigInteger host NBT with strict magnitude and canonical encoding checks.
- Added optional ACO 1.3.0 BigInteger host API v3 integration.
- Added opaque state preservation when ACO is absent or disabled.
- Added exact `10^digits - 1 B` capacity display for the BigInteger Quantum CPU
  in the CPU list, tooltip, and crafting confirmation screen.
- Added unit tests for arithmetic boundaries, fallback persistence, and the reflected optional API contract.

### Changed

- Renamed the displayed experimental core to Long Quantum Core / long型量子コア while retaining its registry ID for world compatibility.
- Replaced long-only cluster aggregation with a checked BigInteger ledger and a saturated Advanced AE facade.
- Kept co-processors on the safe signed-int ceiling used by AE2 and Advanced AE.

### Compatibility

- ACO is optional, not a required dependency.
- ACO 1.3.0 is used only when installed and enabled.
- The BigInteger core has no survival recipe.
- Existing AQE registry IDs remain unchanged.
