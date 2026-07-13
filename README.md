# Advanced Quantum Engineering

Advanced Quantum Engineering adds upgraded Advanced AE Quantum Computer parts that use the original Quantum Computer multiblock system.

This mod targets Minecraft 1.20.1, Forge 47.4.18+, Java 17, Applied Energistics 2 15.4.10, Advanced AE 1.3.5-1.20.1, and AE2 Omni Cells 1.1.6.

AE2 crafting and synchronization optimization code is intentionally not part of this mod. It lives in the separate `ae2-crafting-optimizer` project so Quantum Computer block behavior and AE2 optimization behavior can be tested independently.

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

AQE clamps Advanced AE's effective storage value to `Long.MAX_VALUE - 1` after storage additions and Data Entangler multipliers. It also clamps the effective co-processor return value to `2,147,483,646` so Multi-Threader multiplication cannot overflow into a negative value. AE2 optimization and execution pacing are still handled by the separate `ae2-crafting-optimizer` project.

## Recipe

Each recipe upgrades the matching Advanced AE part:

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

## Config

Server config:

```toml
[modifiedQuantumCore]
coreStorage = 268435456
baseCoprocessors = 4096

[experimentalQuantumCore]
experimentalCoreStorage = 9223372036854775806
experimentalCoreCoprocessors = 2147483646

[modifiedQuantumStorage]
storageBlockBytes = 35184372088831

[modifiedQuantumAccelerator]
acceleratorThreads = 512

[modifiedQuantumMultiThreader]
multiThreaderMultiplier = 8

[modifiedDataEntangler]
dataEntanglerMultiplier = 8

[diagnostics]
failFastOnIntegrationMismatch = true
warnOnExtremeConfigValues = true
diagnosticModifiedAcceleratorCount = 121
```

Advanced AE 1.3.5 normally rejects more than 16 accelerator threads from a single unit block. This mod applies one targeted Mixin to raise that validation constant to the larger configured AQE core/accelerator value. The same Mixin also clamps effective storage to `Long.MAX_VALUE - 1` and effective co-processors to `Integer.MAX_VALUE - 1` before returning to AE2.

AQE also patches AE2's byte tooltip formatter for TiB/PiB/EiB-scale crafting CPU values. This is display-only and prevents Advanced AE CPU selection tooltips from crashing when the experimental core reports values above AE2's default byte unit table.

`diagnostics` does not change gameplay values. It verifies the expected Advanced AE API and AQE unit roles during common setup, logs detected dependency versions, and prints estimated storage/co-processor values. `failFastOnIntegrationMismatch = true` makes the mod stop loading if Advanced AE no longer exposes the expected integration points.

AE2 network/crafting optimizations are handled by the separate `ae2-crafting-optimizer` mod.

## Build

This project currently resolves mod dependencies from the Prism instance `mods` folder via Gradle `flatDir`.

Run:

```bat
gradlew.bat clean build
```

The generated jar is written under `build/libs/`.

When cloning outside the original Prism instance, either recreate the expected local `mods` folder layout or replace the dependency coordinates in `build.gradle` with public Maven coordinates.

## Compatibility Note

This is an addon. Do not report issues caused by this mod directly to the Advanced AE or AE2 Omni Cells authors without first reproducing the issue without Advanced Quantum Engineering and checking this mod's issue context.

## License

Advanced Quantum Engineering is licensed under the GNU Lesser General Public License v3.0 only.

- `LICENSE` contains the LGPL v3.0 text.
- `COPYING` contains the GPL v3.0 text referenced by the LGPL.
- SPDX identifier: `LGPL-3.0-only`

## Asset Note

The Modified Quantum Core texture is based on Advanced AE's Quantum Core texture and the item renders with enchantment glint. Other upgraded parts reuse Advanced AE models as parents. Keep Advanced AE's LGPL-3.0 license in mind if redistributing a pack or source package that includes copied or referenced Advanced AE assets.
