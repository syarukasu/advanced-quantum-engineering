# Implementation

## Integration Method

AQE parts are implemented as Advanced AE crafting unit subclasses. They reuse Advanced AE's existing `AAECraftingUnitType` values, so Advanced AE's own Quantum Computer validator treats them as the same structure roles as the original blocks.

No independent Quantum Computer, fake crafting CPU, or replacement structure validator is implemented.

## Classes

- Main mod: `com.syaru.advancedquantumengineering.AdvancedQuantumEngineering`
- Core block: `ModifiedQuantumCoreBlock extends AAEAbstractCraftingUnitBlock<ModifiedQuantumCoreBlockEntity>`
- Storage block: `ModifiedQuantumStorageBlock extends AAEAbstractCraftingUnitBlock<ModifiedQuantumStorageBlockEntity>`
- Accelerator block: `ModifiedQuantumAcceleratorBlock extends AAEAbstractCraftingUnitBlock<ModifiedQuantumAcceleratorBlockEntity>`
- Multi-Threader block: `ModifiedQuantumMultiThreaderBlock extends AAEAbstractCraftingUnitBlock<ModifiedQuantumMultiThreaderBlockEntity>`
- Data Entangler block: `ModifiedDataEntanglerBlock extends AAEAbstractCraftingUnitBlock<ModifiedDataEntanglerBlockEntity>`
- Experimental core block: `ExperimentalQuantumCoreBlock extends AAEAbstractCraftingUnitBlock<ExperimentalQuantumCoreBlockEntity>`
- Block entities: subclasses of `AdvCraftingBlockEntity`

## Advanced AE Additions

`AdvancedAEIntegration.bindBlockEntity()` calls `setBlockEntity` on each AQE block so AE2's base entity block path can create the matching AQE block entity.

The Advanced AE JAR is not modified. One targeted Mixin is used for the per-unit thread guard. No Access Transformer is used.

`AQEDiagnostics.runStartupChecks()` runs after block entity binding. It verifies the expected Advanced AE methods, required `AAECraftingUnitType` constants, and the AQE block unit roles. This catches likely causes of non-forming Quantum Computers or wrong role counting before players discover fake-looking blocks in-world. By default, detected integration mismatches stop loading with a clear error.

## Differences From Advanced AE Parts

- `advanced_quantum_engineering:modified_quantum_core`
  - Unit type: `AAECraftingUnitType.QUANTUM_CORE`
  - `getStorageBytes()` default: 256 MiB
  - `getAcceleratorThreads()` default: 4,096
- `advanced_quantum_engineering:modified_quantum_storage`
  - Unit type: `AAECraftingUnitType.STORAGE_256M`
  - `getStorageBytes()` default: 35,184,372,088,831 bytes, about 32 TiB
- `advanced_quantum_engineering:modified_quantum_accelerator`
  - Unit type: `AAECraftingUnitType.QUANTUM_ACCELERATOR`
  - `getAcceleratorThreads()` default: 512
- `advanced_quantum_engineering:modified_quantum_multi_threader`
  - Unit type: `AAECraftingUnitType.MULTI_THREADER`
  - `getAccelerationMultiplier()` default: 8
- `advanced_quantum_engineering:modified_data_entangler`
  - Unit type: `AAECraftingUnitType.STORAGE_MULTIPLIER`
  - `getStorageMultiplier()` default: 8
- `advanced_quantum_engineering:experimental_quantum_core`
  - Unit type: `AAECraftingUnitType.QUANTUM_CORE`
  - `getStorageBytes()` default: 9,223,372,036,854,775,806 bytes, `Long.MAX_VALUE - 1`
  - `getAcceleratorThreads()` default: 2,147,483,646

The original Advanced AE parts are not changed.

## Storage Path

Advanced AE's `AdvCraftingCPUCluster.addBlockEntity` calls `getStorageBytes()` on each `AdvCraftingBlockEntity`. Java virtual dispatch makes AQE block entities return their configured values, and Advanced AE adds those values to the normal cluster storage.

`AdvCraftingCPUCluster.recalculateRemainingStorage` then multiplies the summed storage by the summed storage multiplier. With one modified core, one modified storage block, and one modified Data Entangler, the default target is about 256 TiB.

## Co-Processor Path

Advanced AE's `AdvCraftingCPUCluster.addBlockEntity` calls `getAcceleratorThreads()` on each block entity and sums the results. `getCoProcessors()` then multiplies the sum by the Multi-Threader multiplier when one is present.

The intended default large structure is:

```text
(4,096 + 121 * 512) * 8 = 528,384
```

This profile is aimed at Astral Mekanism late-game automation. Astral Mekanism 1.7.17's `AMETier.ASTRAL` exposes 256 processes, so one modified accelerator defaults to 512 threads and a full 121-accelerator Quantum Computer lands above 256k effective co-processors with the modified Multi-Threader x8 multiplier.

## Multi-Threader

The original Quantum Multi-Threader is unchanged. AQE registers a modified Multi-Threader that uses the same `AAECraftingUnitType.MULTI_THREADER` structure role and returns a configurable `getAccelerationMultiplier()` value. It occupies Advanced AE's normal Multi-Threader slot and does not add a second multiplier slot.

## Data Entangler

The original Quantum Data Entangler is unchanged. AQE registers a modified Data Entangler that uses the same structure role and returns a configurable storage multiplier.

## Omni Cells Material

The recipes use `ae2omnicells:quantum_omni_cell_component_64m`. The installed Omni Cells JAR shows it as the highest Quantum Omni Cell component and its own recipe upgrades from lower Quantum components.

## Mixin and Access Transformer

- Mixin: `AdvCraftingCPUClusterMixin`
- Mixin: `TooltipsMixin`
- Access Transformer: not used.

The Mixin changes only the `16` constant used by Advanced AE's single-unit thread guard inside `AdvCraftingCPUCluster.addBlockEntity`. It returns `max(16, AQEConfig.getMaxSingleUnitCoprocessors())`, which allows the modified core and modified accelerators to contribute their configured values while preserving the rest of the cluster calculation.

The same Mixin injects at the head of `AdvCraftingCPUCluster.recalculateRemainingStorage()`. It recomputes storage and storage multipliers from the block list using saturating arithmetic, then clamps final effective storage to `Long.MAX_VALUE - 1`. This prevents Advanced AE's storage addition or Data Entangler multiplication from overflowing when the experimental core is used.

It also injects at the head of `AdvCraftingCPUCluster.getCoProcessors()` and returns a long-calculated, clamped value. This prevents Advanced AE's `accelerator * acceleratorMultiplier` int multiplication from overflowing when the experimental core is combined with a Multi-Threader. The clamp is `Integer.MAX_VALUE - 1`, which lets AE2 add one execution slot without overflowing.

`TooltipsMixin` injects at the head of AE2's `Tooltips.getByteAmount(long)` only for values at or above one TiB. AE2 15.4.10's byte divisor table stops before TiB-scale values, while Advanced AE's CPU selection tooltip calls `Tooltips.ofBytes` for CPU storage. The mixin returns a normal `Tooltips.Amount` using T/P/E units, so very large AQE CPU values render without changing actual storage or crafting behavior.

AE2 crafting/network optimizations are not implemented in AQE. They are kept in the separate `ae2-crafting-optimizer` project to keep Quantum Computer behavior and AE2 optimization behavior independently testable.

## Known Risk

The cluster Mixin targets Advanced AE 1.3.5 bytecode by method name. If Advanced AE changes `AdvCraftingCPUCluster.addBlockEntity`, this mod should fail loudly rather than silently becoming a fake visual block. Startup diagnostics also check that the expected method still exists and that AQE blocks still expose the intended Advanced AE unit roles. The tooltip Mixin targets AE2 15.4.10's public `Tooltips.getByteAmount(long)` method and should be retested after AE2 updates.
