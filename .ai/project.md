# ShizukuCallRecorder

## Stack
- Kotlin
- Android
- Jetpack Compose
- Shizuku API

## Arquitectura

manager:
- UI
- configuración
- interacción usuario

server:
- servicios privilegiados
- comunicación Shizuku

## Reglas del proyecto

- No modificar permisos/autorizaciones sin análisis.
- Mantener traducciones EN/ES sincronizadas.
- Cambios pequeños y auditables.
- Priorizar compatibilidad Android.

## Estado actual

Últimos trabajos:
- Application Management polish
- TV loading fix
- strings EN/ES cleanup
- AdbPairingTutorialActivity UTF-8 fix

Pendiente:
- analizar mejoras upstream
- optimización de memoria de contexto AI
