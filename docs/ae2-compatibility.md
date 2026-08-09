# AE2 compatibility profiles

AQE supports two Forge 1.20.1 AE2 implementations through the same `ae2` mod
id:

- `upstream`: `appeng:appliedenergistics2-forge:15.4.10`
- `uelm`: `appeng:appliedenergistics2-forge:15.5.0-uelm`

AE2 Unofficial Extended Life Modern is a replacement fork, not an additional
mod. Do not install it alongside upstream AE2, and do not add an `ae2_uelm`
dependency. The UELM source is the `forge/1.20.1` branch at
<https://github.com/AE2-Unofficial-Extended-Life-Modern/AE2-UELM> and its
published Maven repository is <https://repo.expandium.net/releases>.

The default build keeps the existing upstream profile:

```text
gradlew.bat check --no-daemon
```

The UELM profile is selected explicitly:

```text
gradlew.bat check --no-daemon -Pae2Variant=uelm
```

For local dependency testing, place the matching artifact in the configured
mods directory or pass `-PaqeLocalModsDir=...`. The UELM artifact name is
`appliedenergistics2-forge-15.5.0-uelm.jar`.

AQE's AE2-facing mixins were checked against both source trees. UELM changes
the request amount from `int` to `long` in its own crafting menus, screen, and
packet. AQE does not inject into those amount methods, so it leaves that
surface to AE2 and only validates the detected shared `ae2` implementation at
startup.
