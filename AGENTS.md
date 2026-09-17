# Reglas del Proyecto - Dominotes

## Control de Versiones Obligatorio
- **Cada vez que se realice un cambio o mejora en el código**, se **DEBE** incrementar la versión en `app/build.gradle.kts`:
  - `versionCode` debe incrementarse en 1 (ejemplo: 4 -> 5 -> 6 -> ...)
  - `versionName` debe aumentarse al siguiente número de versión semántica (ejemplo: "1.3" -> "1.3.1" -> "1.4" -> ...)
- Esto asegura que al hacer push a GitHub:
  1. El workflow de GitHub Actions detecte la nueva versión.
  2. Cree el Release con la nueva etiqueta (ejemplo: `v1.4`).
  3. La app instalada en el dispositivo móvil del usuario reconozca automáticamente que hay una nueva versión disponible mediante su comprobador de actualizaciones (`AppUpdateChecker`).
