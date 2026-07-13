# Publishing

This project is ready to be placed in a GitHub repository as source code.

## Recommended Repository

Use one repository for this mod:

```text
advanced-quantum-engineering
```

Do not commit generated Gradle output, built jars, or local runtime folders.

License files to keep in the repository:

- `LICENSE` - GNU Lesser General Public License v3.0
- `COPYING` - GNU General Public License v3.0, referenced by the LGPL

The project metadata uses SPDX identifier `LGPL-3.0-only`.

Ignored by `.gitignore`:

- `.gradle/`
- `build/`
- `run/`
- `run-server/`
- IDE metadata

## Local Dependency Note

`build.gradle` resolves dependency jars from the Prism instance `mods` folder with `flatDir`.

That is intentional for this local pack workspace. A fresh GitHub clone will need either:

- the same local `mods` folder layout near the project, or
- public Maven dependency coordinates replacing the local `flatDir` entries.

## First Push

From this project directory:

```bat
git init
git add .
git commit -m "Initial source import"
git branch -M main
git remote add origin https://github.com/<owner>/advanced-quantum-engineering.git
git push -u origin main
```

Do not report issues caused by this addon directly to Advanced AE or AE2 Omni Cells without reproducing them without this mod first.
