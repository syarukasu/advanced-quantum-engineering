# Advanced Quantum Engineering

Advanced Quantum Engineering 2.1.2 adds upgraded Advanced AE Quantum Computer parts that use the original Quantum Computer multiblock system.

This mod targets Minecraft 1.20.1, Forge 47.4.18+, Java 17, Applied Energistics 2 15.4.10, Advanced AE 1.3.5-1.20.1, and AE2 Omni Cells 1.1.6.

AE2 crafting and synchronization optimization code is intentionally not part of this mod. It lives in the separate `ae2-crafting-optimizer` project so Quantum Computer block behavior and AE2 optimization behavior can be tested independently. Compatible ACO 1.3.x and 1.4.x releases are optional: AQE runs without ACO and uses its versioned BigInteger host API when it is present and enabled.

## Blocks

- `advanced_quantum_engineering:modified_quantum_core`
  - Core slot replacement for `advanced_ae:quantum_core`
  - Default storage: 256 MiB
  - Default base co-processors: 4,096
- `advanced_quantum_engineering:modified_quantum_storage`
  - Storage slot replacement for `advanced_ae:quantum_storage_256`
  - Default storage: about 32 TiB per block
- `advanced_quantum_engineering:modified_quantum_accelerator`
  - Accelerator slot replacement for `advanced_ae:quantum_accelerator`
  - Default co-processors per block: 512
- `advanced_quantum_engineering:modified_quantum_multi_threader`
  - Multi-Threader slot replacement for `advanced_ae:quantum_multi_threader`
  - Default co-processor multiplier: x8
- `advanced_quantum_engineering:modified_data_entangler`
  - Data Entangler slot replacement for `advanced_ae:data_entangler`
  - Default storage multiplier: 8
- `advanced_quantum_engineering:experimental_quantum_core`
  - Experimental core slot replacement for `advanced_ae:quantum_core`
  - Default storage: 9,223,372,036,854,775,806 bytes, `Long.MAX_VALUE - 1`
  - Default co-processors: 2,147,483,646
  - Intended for stress testing, not normal server progression
- `advanced_quantum_engineering:big_integer_quantum_core`
  - Experimental core slot replacement for `advanced_ae:quantum_core`
  - Default raw storage: `10^64 - 1` bytes
  - Configurable by decimal digit count, from 20 to 16,372 digits
  - The complete structure has an exact `10^16384 - 1` ceiling; 12 decimal
    digits are reserved for summed storage and Data Entangler multiplication
  - Default co-processors: 2,147,483,646
  - Has no survival recipe

The blocks subclass Advanced AE's crafting unit block path and reuse Advanced AE's existing unit types. They are not extra structure slots and do not create a separate crafting CPU system.

## Structure Rules

The Advanced AE Quantum Computer rules remain owned by Advanced AE:

- One structure may contain exactly one core.
- The valid core may be either `advanced_ae:quantum_core` or `advanced_quantum_engineering:modified_quantum_core`.
- Two normal cores, two modified cores, normal plus modified, or no core are invalid.
- Normal Quantum Multi-Threader behavior is unchanged.
- Normal Quantum Data Entangler behavior is unchanged.
- Minimum size, maximum size, wall, frame, internal, and network rules are unchanged.
- The upgraded storage, accelerator, Multi-Threader, and entangler occupy the same structure roles as their Advanced AE originals.
- The experimental core uses the same core role and still counts toward the one-core limit.
- The BigInteger core uses the same core role and still counts toward the one-core limit.

## Target Numbers

With the intended late-game structure:

- Modified Quantum Core x1
- Modified Quantum Storage x1
- Modified Data Entangler x1
- Modified Quantum Computer Multi-Threader x1
- Modified Quantum Accelerator x121

The defaults produce approximately:

- Storage: `(256 MiB + 35,184,372,088,831 bytes) * 8`, about 256 TiB
- Co-processors: `(4,096 + 121 * 512) * 8 = 528,384`

These defaults are tuned for packs where Astral Mekanism late-game machines are present. They avoid `Long.MAX_VALUE` storage and `Integer.MAX_VALUE` co-processors because Advanced AE still has to multiply and schedule those values.

The experimental core exposes maximum test values:

- Storage: `9,223,372,036,854,775,806` bytes, `Long.MAX_VALUE - 1`
- Co-processors: `2,147,483,646`, `Integer.MAX_VALUE - 1`

AQE 2.1.2 calculates the complete structure capacity, including summed storage and Data Entangler multipliers, with checked `BigInteger` arithmetic. Advanced AE's existing `long` API receives a saturated facade, while AQE keeps the exact physical, reserved, and available totals internally. Co-processors remain bounded to `2,147,483,646` because AE2 and Advanced AE expose that value as `int`.

One standard AE2 crafting plan is still limited by AE2 15.4.10's signed-`long` contracts. The BigInteger total is useful because one Advanced AE Quantum Computer can own multiple active jobs: AQE accounts all of those normal jobs against the same exact capacity without overflowing their sum. Native jobs larger than `long` require the optional ACO BigInteger execution API; AQE does not replace the normal AE2 terminal or pretend that a standard AE2 plan has a wider count type.

BigInteger Quantum CPU screens use the server's current capacity Ledger rather than rebuilding `10^N - 1 B` from Config. The CPU list shows the capacity reserved by active jobs, and tooltips show the complete physical, reserved, and available totals after storage aggregation and Data Entangler multiplication. They also show total active jobs and native BigInteger parent jobs; temporary ACO child windows are excluded from the total to avoid double display. Values through 19 decimal digits are shown exactly. Larger values show their leading grouped digits and total digit count. The hidden synchronization marker remains bounded even at the 16,384-digit ceiling; it never sends the full huge decimal value to every client.

## Recipe

Each normal or long-core recipe upgrades the matching Advanced AE part:

- `advanced_ae:quantum_core`
- `advanced_ae:quantum_storage_256`
- `advanced_ae:quantum_accelerator`
- `advanced_ae:quantum_multi_threader`
- `advanced_ae:data_entangler`
- `ae2omnicells:quantum_omni_cell_component_64m`
- `advanced_ae:quantum_storage_component`

They are normal JSON recipes, so KubeJS can remove them:

```js
ServerEvents.recipes(event => {
  event.remove({ output: 'advanced_quantum_engineering:modified_quantum_core' })
  event.remove({ output: 'advanced_quantum_engineering:modified_quantum_storage' })
  event.remove({ output: 'advanced_quantum_engineering:modified_quantum_accelerator' })
  event.remove({ output: 'advanced_quantum_engineering:modified_quantum_multi_threader' })
  event.remove({ output: 'advanced_quantum_engineering:modified_data_entangler' })
})
```

The BigInteger Quantum Core intentionally has no recipe. It is an operator-controlled integration and stress-test block.

## Config

Global config: `config/advanced_quantum_engineering.toml`

```toml
[quantum_computer]
core_storage_bytes = 268435456
storage_block_bytes = 35184372088831
core_coprocessors = 4096
accelerator_threads = 512
multi_threader_multiplier = 8
data_entangler_multiplier = 8

[endgame_cores]
long_core_storage_bytes = 9223372036854775806
long_core_coprocessors = 2147483646
# Exact capacity is 10^digits - 1 bytes.
big_integer_storage_digits = 64
big_integer_coprocessors = 2147483646

[safety_and_diagnostics]
fail_fast_on_integration_mismatch = true
warn_on_extreme_values = true
diagnostic_accelerator_count = 121
```

AQE 2.0.2 moves this file out of each world's `serverconfig` directory. On the
first server start, an existing `advanced_quantum_engineering-server.toml` is
migrated and renamed with a `.migrated` suffix.

Advanced AE 1.3.5 normally rejects more than 16 accelerator threads from a single unit block. This mod applies one targeted Mixin to raise that validation constant to the larger configured AQE core/accelerator value. The same Mixin performs checked BigInteger storage aggregation, keeps a shared reservation ledger for every active Quantum Computer job, exposes a saturated `long` facade to Advanced AE, and clamps effective co-processors to `Integer.MAX_VALUE - 1`.

AQE also patches AE2's byte tooltip formatter for TiB/PiB/EiB-scale crafting CPU values. This is display-only and prevents Advanced AE CPU selection tooltips from crashing when the experimental core reports values above AE2's default byte unit table.

`safety_and_diagnostics` does not change gameplay values. It verifies the expected Advanced AE API and AQE unit roles during common setup, logs detected dependency versions, and prints estimated storage/co-processor values. `fail_fast_on_integration_mismatch = true` makes the mod stop loading if Advanced AE no longer exposes the expected integration points.

AE2 network/crafting optimizations are handled by the separate `ae2-crafting-optimizer` mod.

## Optional ACO Integration

`ae2-crafting-optimizer` is not a required dependency.

- AQE without ACO: the BigInteger core forms normally, exact aggregate capacity is retained, and standard AE2/Advanced AE jobs remain supported.
- AQE with compatible ACO `[1.3.0,1.5.0)` releases: AQE reflectively activates ACO BigInteger host API v3. Standard long jobs and exact BigInteger parent jobs share one physical capacity ledger. ACO divides a deterministic parent into recipe-specific checked-long child windows and keeps their reservations tied to the parent.
- ACO present but disabled: AQE uses its local long-compatible backend.
- ACO removed while native BigInteger state exists: AQE preserves the opaque versioned NBT and keeps its reservation unavailable, preventing double spending. Reinstalling compatible ACO restores the state.
- Unsupported ACO version: the default fail-fast diagnostic stops loading instead of discarding or misreading saved state.

AQE contains no direct ACO class reference or Gradle dependency. The optional adapter resolves and validates API v3 only after Forge reports a compatible ACO `[1.3.0,1.5.0)` release as loaded.

## Build

This project currently resolves mod dependencies from the Prism instance `mods` folder via Gradle `flatDir`.

Run:

```bat
gradlew.bat clean build
```

The generated jar is written under `build/libs/advanced-quantum-engineering-2.1.2.jar`.

When cloning outside the original Prism instance, either recreate the expected local `mods` folder layout or replace the dependency coordinates in `build.gradle` with public Maven coordinates.

## Compatibility Note

This is an addon. Do not report issues caused by this mod directly to the Advanced AE or AE2 Omni Cells authors without first reproducing the issue without Advanced Quantum Engineering and checking this mod's issue context.

## License

Advanced Quantum Engineering is licensed under the GNU Lesser General Public License v3.0 only.

- `LICENSE` contains the LGPL v3.0 text.
- `COPYING` contains the GPL v3.0 text referenced by the LGPL.
- SPDX identifier: `LGPL-3.0-only`

## Asset Note

AQE does not include copied Advanced AE texture PNGs. Its block models reference Advanced AE models as parents, and the upgraded items render with enchantment glint. Advanced AE remains a required runtime dependency and supplies those referenced assets.
