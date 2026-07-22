# Testing

## Build

Run:

```bat
gradlew.bat clean build
```

Expected:

- Build succeeds.
- Jar exists under `build/libs/advanced-quantum-engineering-2.0.0.jar`.

## Registration

Commands:

```mcfunction
/give @s advanced_quantum_engineering:modified_quantum_core
/give @s advanced_quantum_engineering:modified_quantum_storage
/give @s advanced_quantum_engineering:modified_quantum_accelerator
/give @s advanced_quantum_engineering:modified_quantum_multi_threader
/give @s advanced_quantum_engineering:modified_data_entangler
/give @s advanced_quantum_engineering:experimental_quantum_core
/give @s advanced_quantum_engineering:big_integer_quantum_core
```

Expected:

- Items are obtained.
- Blocks can be placed.
- Blocks can be broken.
- Blocks drop themselves.
- Pick Block returns the matching item.
- Items appear in the Advanced Quantum Engineering creative tab.
- EMI shows the items and JSON recipes.

## Structure

Use an otherwise valid Advanced AE Quantum Computer.

Valid core choices:

- `advanced_ae:quantum_core` x1
- `advanced_quantum_engineering:modified_quantum_core` x1
- `advanced_quantum_engineering:experimental_quantum_core` x1

Invalid core choices:

- `advanced_ae:quantum_core` x2
- `advanced_quantum_engineering:modified_quantum_core` x2
- `advanced_ae:quantum_core` x1 plus `advanced_quantum_engineering:modified_quantum_core` x1
- `advanced_quantum_engineering:modified_quantum_core` x1 plus `advanced_quantum_engineering:experimental_quantum_core` x1
- `advanced_quantum_engineering:big_integer_quantum_core` x2
- `advanced_quantum_engineering:big_integer_quantum_core` x1 plus any other core x1
- no core

Expected:

- Modified core forms the structure in the original core position.
- Modified storage, accelerator, Multi-Threader, and Data Entangler occupy the same structure roles as their Advanced AE originals.
- Normal plus modified core remains invalid because Advanced AE counts both as `QUANTUM_CORE`.
- Experimental core also remains invalid when mixed with any other core because it uses the same `QUANTUM_CORE` role.

## Capacity

Expected:

- Modified core storage is 256 MiB by default.
- Modified storage block storage is 35,184,372,088,831 bytes, about 32 TiB, by default.
- Modified Data Entangler multiplies storage by 8 by default.
- One modified core, one modified storage block, and one modified Data Entangler provide about 256 TiB.
- Experimental core alone provides 9,223,372,036,854,775,806 bytes by default.
- BigInteger core with a modified Data Entangler retains the exact multiplied capacity internally and exposes a non-negative saturated long value to Advanced AE.
- Jobs above available storage are rejected as CPU too small.
- Rejection does not crash.
- Advanced AE's crafting CPU selection tooltip can be opened with TiB/PiB/EiB-scale AQE CPU values without an AE2 `Tooltips.getByteAmount` array bounds crash.

## Co-Processors

Expected:

- Existing `advanced_ae:quantum_multi_threader` is usable.
- `advanced_quantum_engineering:modified_quantum_multi_threader` is usable in the same structure role.
- Placement rules and maximum count remain Advanced AE defaults.
- Original Multi-Threader behavior remains unchanged.
- Modified core contributes 4,096 base co-processors by default.
- Each modified accelerator contributes 512 co-processors by default.
- Modified Multi-Threader multiplies co-processors by 8 by default.
- With 121 modified accelerators and the modified Multi-Threader multiplier of 8, the intended effective co-processor count is 528,384.
- Experimental core contributes 2,147,483,646 co-processors by default.
- Experimental core with a Multi-Threader should still report a non-negative effective value because AQE clamps Advanced AE's return value before int overflow.
- Raising `baseCoprocessors` or `acceleratorThreads` should be tested with live TPS profiling before regular server use.

## Data Entangler

Expected:

- Existing `advanced_ae:data_entangler` is usable.
- `advanced_quantum_engineering:modified_data_entangler` is usable in the same structure role.
- Placement rules and maximum count remain Advanced AE defaults.
- Original storage multiplier behavior remains Advanced AE defaults.

## Persistence

Expected:

- Save and reload keeps the structure.
- Server restart keeps the structure.
- Chunk unload and reload keeps the structure.
- Replacing normal core with modified core reforms the structure.
- Replacing modified core with normal core reforms the structure.

## KubeJS

Recipe removal:

```js
ServerEvents.recipes(event => {
  event.remove({ output: 'advanced_quantum_engineering:modified_quantum_core' })
  event.remove({ output: 'advanced_quantum_engineering:modified_quantum_storage' })
  event.remove({ output: 'advanced_quantum_engineering:modified_quantum_accelerator' })
  event.remove({ output: 'advanced_quantum_engineering:modified_data_entangler' })
  event.remove({ output: 'advanced_quantum_engineering:experimental_quantum_core' })
})
```

Expected:

- EMI no longer shows the default recipes after reload/restart.
- Replacement KubeJS recipes can produce the same outputs.

## AE2 Optimization Separation

AQE does not include AE2 crafting/network optimization code. Test AE2 optimization behavior in the separate `ae2-crafting-optimizer` project.

Expected for AQE:

- AE2 storage watcher behavior is unchanged by AQE.
- ME terminals and monitors update exactly as AE2/other installed mods provide.
- Startup logs include detected AE2, Advanced AE, and AE2 Omni Cells versions.
- Startup logs include AQE unit type checks for core, storage, accelerator, and Data Entangler roles.
- Startup logs include estimated storage and co-processor values for the diagnostic structure.

## BigInteger and Optional ACO Matrix

The exact AQE/ACO shared capacity ceiling is `10^16384 - 1`. Verify that this
value round-trips and that `10^16384` fails closed without truncation. The raw
core Config stops at 16,372 digits to leave 12 digits for structure aggregation
and Data Entangler multiplication.

Run every case on a copied world before enabling experimental ACO execution paths. Do not treat `gradlew test` alone as full runtime qualification.

1. AQE 2.0.0 without ACO:
   - client and dedicated server start;
   - BigInteger core forms the original Advanced AE structure;
   - multiple standard crafting jobs run concurrently;
   - save/restart restores every job and exact remaining capacity;
   - tooltip reports the local long-compatible backend.
2. AQE 2.0.2 with ACO 1.3.1 on both sides:
   - startup logs report `aco:big_crafting_v3`;
   - normal jobs and ACO-native reservations share one capacity;
   - save/restart restores the same runtime and job IDs;
   - chunk unload/reload does not duplicate or release reservations;
   - client status packets do not exceed their configured page/byte limits.
   - AQE reads `config/advanced_quantum_engineering.toml` on both sides;
   - an old per-world AQE server config is imported once and renamed with a
     `.migrated` suffix.
3. Remove ACO after saving native state:
   - AQE still starts;
   - opaque state remains in `aqeBigCraftingHost`;
   - paused reservation remains unavailable to standard jobs;
   - no item or job is silently completed, cancelled, or deleted.
4. Reinstall compatible ACO 1.3.x:
   - the preserved state restores;
   - standard reservations are reconciled from Advanced AE;
   - no reservation is counted twice.
5. Install an unsupported ACO version:
   - default diagnostics fail with an explicit compatibility message;
   - no saved payload is overwritten.

Automated tests cover checked capacity arithmetic, malformed/canonical NBT, local fallback preservation, optional API reflection, shared host reservations, and host save/load. They do not replace Forge/Arclight startup, transformed-Mixin, in-world structure, multiplayer, or crash-recovery tests.

## GameTest

No GameTest is included. Advanced AE's Quantum Computer depends on its live AE network and multiblock initialization path, so manual integration testing is the meaningful test path for this addon.
