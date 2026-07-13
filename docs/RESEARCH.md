# Research

## Applied Energistics 2

- JAR: `appliedenergistics2-forge-15.4.10.jar`
- modId: `ae2`
- Version: `15.4.10`
- The relevant common API used here is `appeng.block.crafting.ICraftingUnitType`.
- `ICraftingUnitType` exposes `getStorageBytes()`, `getAcceleratorThreads()`, and `getItemFromType()`.

## Advanced AE

- JAR: `AdvancedAE-1.3.5-1.20.1.jar`
- modId: `advanced_ae`
- Version: `1.3.5-1.20.1`
- License in `META-INF/mods.toml`: LGPL-3.0
- Normal Quantum Core registry ID: `advanced_ae:quantum_core`
- Normal Quantum Core block class: `net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitBlock`
- Shared crafting unit base block: `net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock`
- Quantum Computer block entity class: `net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity`
- Quantum Computer structure calculator: `net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCalculator`
- Quantum Computer cluster: `net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster`
- Quantum Computer block entity type: `advanced_ae:quantum_core`
- Data Entangler registry ID: `advanced_ae:data_entangler`
- Quantum Multi-Threader registry ID: `advanced_ae:quantum_multi_threader`

### Core Validation

`AdvCraftingCPUCalculator.verifyInternalStructure` scans every block entity in the multiblock bounds. It requires every internal block entity to be an `AdvCraftingBlockEntity`, then switches on `getUnitBlock().type` cast to `AAECraftingUnitType`.

`AAECraftingUnitType.QUANTUM_CORE` is the sole core type. The calculator allows exactly one internal non-edge core. Because the modified core block uses the existing `QUANTUM_CORE` unit type, Advanced AE counts it as the same core slot.

### Storage Path

`AdvCraftingCPUCluster.addBlockEntity` calls `AdvCraftingBlockEntity.getStorageBytes()` and adds the returned `long` to cluster storage. The cluster later uses `getAvailableStorage()` in `submitJob` to reject jobs that exceed available bytes. Overriding `getStorageBytes()` in a subclass is therefore the real storage path, not a UI-only change.

### Basic Compute Path

`AdvCraftingCPUCluster.addBlockEntity` calls `AdvCraftingBlockEntity.getAcceleratorThreads()`. If the result is above 16, Advanced AE throws `IllegalArgumentException: Co-processor threads may not exceed 16 per single unit block.`

Advanced AE's common config defaults `quantumComputerAcceleratorThreads` to 8 with range 4-16. Values above 16 cannot be returned by a single block unless the single-unit validation constant is raised. This implementation uses one targeted Mixin on `AdvCraftingCPUCluster.addBlockEntity` to raise only that constant to the larger configured AQE core or accelerator value. The current defaults distribute performance across the multiblock: 4,096 from the modified core and 512 from each modified accelerator.

### Multi-Threader Path

`AAECraftingUnitType.MULTI_THREADER` contributes `getAccelerationMultiplier()` from Advanced AE config. The AQE modified Multi-Threader uses the same structure role but overrides `getAccelerationMultiplier()` in its block entity to return the AQE config value, default 8. Advanced AE's own `getQuantumComputerMaxMultiThreaders()` limit still controls how many Multi-Threader-role blocks can exist in one structure.

### Data Entangler Path

`AAECraftingUnitType.STORAGE_MULTIPLIER` contributes `getStorageMultiplier()` from Advanced AE config. The AQE modified Data Entangler uses the same structure role but overrides `getStorageMultiplier()` in its block entity to return the AQE config value, default 8.

### Crafting CPU Registration

`AdvCraftingBlockEntity` registers as an AE network multiblock through `IGridMultiblock` and the Advanced AE cluster. The modified core block entity subclasses `AdvCraftingBlockEntity`, so it follows the same registration, save, restore, and network event paths.

### Persistence

Advanced AE stores active CPU state in `AdvCraftingCPUCluster.writeToNBT` and restores it from the core block entity's previous state in `AdvCraftingCPUCluster.done`. This mod does not add duplicate NBT for cluster state.

### Model Resources

- Blockstate: `assets/advanced_ae/blockstates/quantum_core.json`
- Block model: `assets/advanced_ae/models/block/quantum_core.json`
- Powered formed model: `assets/advanced_ae/models/block/quantum_core_formed_on.json`
- Item model: `assets/advanced_ae/models/item/quantum_core.json`
- Textures:
  - `assets/advanced_ae/textures/block/crafting/quantum_core.png`
  - `assets/advanced_ae/textures/block/crafting/quantum_core_nucleus.png`
  - `assets/advanced_ae/textures/block/crafting/quantum_core_out.png`

## AE2 Omni Cells

- JAR: `ae2omnicells-1.20.1-forge-1.1.6.jar`
- modId: `ae2omnicells`
- Version: `1.1.6`
- License in `META-INF/mods.toml`: LGPL-3.0-or-later
- Selected material: `ae2omnicells:quantum_omni_cell_component_64m`
- English name: `64M Quantum Storage Component`
- Recipe path: `data/ae2omnicells/recipes/components/shaped/quantum_omni_cell_component_64m.json`

The selected component is the highest listed Quantum Omni Cell component in the installed JAR. Quantum crafting storage blocks also exist, but the component fits the requested material role better than using Omni Cells blocks as structure parts.

## Astral Mekanism Scale Check

- JAR: `astral_mekanism-1.7.17.jar`
- modId: `astral_mekanism`
- `astral_mekanism.AMETier` defines late-game tiers with very large fixed values.
- `AMETier.ASTRAL` has `processes = 256`, `intValue = 2,147,483,647`, and `longValue = 9,223,372,036,854,775,807`.
- `AMETier.COSMIC` has `longValue = 281,474,976,710,656`, which is 256 TiB.

AQE does not depend on Astral Mekanism at load time, but the default profile is tuned for packs where Astral Mekanism is present. The modified storage block defaults to the largest per-unit value that stays under the overflow guard for a heavily populated Advanced AE structure. With one modified Data Entangler at x8, one modified storage block yields about 256 TiB, matching the Astral-scale storage band without using `Long.MAX_VALUE`.

The modified accelerator defaults to 512 co-processors per block. In the intended 121-accelerator Quantum Computer with the modified Multi-Threader x8 multiplier, this produces 528,384 effective co-processors while staying far below `Integer.MAX_VALUE`.

## Implementation Decision

- Public API alone is not enough to register a new core type.
- Full structure replacement is unnecessary because Advanced AE already keys core validity from `AAECraftingUnitType.QUANTUM_CORE`.
- Class inheritance is possible and used:
  - Block: subclass `AAEAbstractCraftingUnitBlock`
  - Block entity: subclass `AdvCraftingBlockEntity`
- Access Transformer: not required.
- Mixin: required only for the per-unit thread validation constant in `AdvCraftingCPUCluster.addBlockEntity`.
- Minimum changed points: register new blocks using the existing Advanced AE unit types, override storage/thread/multiplier methods in subclassed block entities, and raise the single-unit thread validation constant so configured values can form a cluster.

## Compatibility Risk

This implementation relies on Advanced AE 1.3.5 keeping `AAEAbstractCraftingUnitBlock.type`, `AAECraftingUnitType.QUANTUM_CORE`, and the virtual `AdvCraftingBlockEntity.getStorageBytes()` / `getAcceleratorThreads()` path. The dependency range is intentionally narrow.
