# Changelog

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
