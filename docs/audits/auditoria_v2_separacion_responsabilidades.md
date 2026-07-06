# Auditoría V2 — Separación de responsabilidades del backend

Estado base: commit `1837c8f` — refactor: move ShellService to services/shell package

---

## 1. Responsabilidades mezcladas actualmente

### ShellService.kt — 477 líneas, proceso shell

Contiene **cuatro responsabilidades distintas**:

| # | Responsabilidad | Líneas | Descripción |
|---|----------------|--------|-------------|
| A | **Lifecycle Shizuku** | 68–156 | Constructores `@Keep`, log de inicio, contrato AIDL |
| B | **Lanzamiento de proceso** | 179–258 | Verificación del JAR, `ProcessBuilder`, `app_process` |
| C | **Pipeline de audio** | 362–476 | Socket Unix, relay coroutine socket→pipe, `ParcelFileDescriptor` |
| D | **Gestión de sesión** | 272–320 | `stopRecording()`, limpieza ordenada, `AtomicBoolean`, nulificación |

Además:

| E | **AppOps shell** | 325–339 | `grantAppOps()` — ejecuta `appops set` por shell |

---

### AudioRecordingEngine.kt — 282 líneas, proceso app

Mezcla **tres responsabilidades**:

| # | Responsabilidad | Líneas | Descripción |
|---|----------------|--------|-------------|
| F | **Orquestación pipeline** | 118–223 | SAF, extracción server, llamada IPC, `ScrcpyClient` |
| G | **Estado de sesión** | 55–112 | `scrcpyClient`, `scrcpyAudioMuxer`, `audioReadPipePfd`, `outputPfd`, `isPaused`, etc. |
| H | **Limpieza y cancelación** | 237–270 | `release()`, `cancel()`, espera de job, cierre de recursos |

---

### RecordingForegroundService.kt — 461 líneas

| # | Responsabilidad | Notas |
|---|----------------|-------|
| I | **Lifecycle Android Service** | Correcto — no mover |
| J | **Orquestación Shizuku** | `waitForServer()`, `getShellService()`, `onBinderDied` — podría delegar |
| K | **Fallback número por CallLog** | `tryGetFinalNumberFromLog()` (L393–422) — lógica de negocio mezclada en Service |

---

## 2. Qué debe permanecer en ShellService

Únicamente lo que **solo puede existir en el proceso shell privilegiado**:

```
ShellService (proceso shell)
├── Constructores @Keep + log de inicio de proceso    [responsabilidad A]
├── startRecording() — coordina B + C, devuelve pipe  [contrato AIDL]
├── stopRecording()  — coordina limpieza              [contrato AIDL]
├── isRecording()                                      [contrato AIDL]
├── grantAppOps()   — requiere privilegios shell       [responsabilidad E]
└── destroy()       — contrato Shizuku obligatorio
```

Todo lo que hoy vive **dentro** de `startRecording()` y `stopRecording()` puede
delegarse a clases internas, dejando ShellService como coordinador delgado.

---

## 3. Qué código debería extraerse

### 3.1 ShellCommandExecutor — nuevo, en `services/shell/`

**Código actualmente en ShellService:**
- Verificación del JAR con `ServerExtractor.verifyServerHash()` (L200–204)
- Conversión String→enum (`ScrcpyAudioSource.fromKey`, `ScrcpyAudioCodec.fromKey`) (L231–232)
- Llamada a `ScrcpyConfig.buildServerArgs()` (L233)
- Construcción del `ProcessBuilder` con `app_process` (L234–242)
- Configuración de `CLASSPATH` y `redirectErrorStream`
- `scrcpyProcess = scrcpyBuilder.start()` (L243)

**Contrato propuesto:**
```kotlin
// services/shell/ShellCommandExecutor.kt
internal object ShellCommandExecutor {
    fun verifyServerJar(serverPath: String): Boolean
    fun launchScrcpyServer(
        serverPath: String,
        audioSource: String,
        audioCodec: String,
        audioBitRate: Int
    ): Process  // lanza app_process, devuelve Process o lanza excepción
}
```

---

### 3.2 ShellAudioPipeline — nuevo, en `services/shell/`

**Código actualmente en ShellService:**
- `serverSocket: LocalServerSocket` (L109)
- `clientConnection: LocalSocket` (L112)
- `audioWriteEnd: ParcelFileDescriptor` (L123)
- `shellScope: CoroutineScope` + `audioPipeRelayJob: Job` (L130, L136)
- `RELAY_BUFFER_SIZE`, `PROCESS_STOP_GRACE_PERIOD_SEC` (L81, L88)
- `spawnAudioRelayCoroutine()` completo (L362–420)
- `spawnLogConsumerCoroutine()` completo (L427–447)
- `spawnProcessMonitorCoroutine()` completo (L458–476)
- Secuencia de cierre en `stopRecording()`: process, scope, socket, pipe (L282–317)

**Contrato propuesto:**
```kotlin
// services/shell/ShellAudioPipeline.kt
internal class ShellAudioPipeline {
    // start(): crea socket, inicia relay/log/monitor coroutines,
    //          devuelve el read-end del pipe al llamador (ShellService)
    fun start(process: Process, verbose: Boolean): ParcelFileDescriptor
    fun stop()  // limpieza ordenada completa
    val isActive: Boolean
}
```

**Precondición:** `ShellService.startRecording()` crea el `Process` vía `ShellCommandExecutor`
y lo pasa a `ShellAudioPipeline.start()`.

---

### 3.3 RecordingSessionManager — nuevo, en `services/recording/`

**Código actualmente en AudioRecordingEngine:**
- Todas las variables de sesión (`scrcpyClient`, `scrcpyAudioMuxer`, `audioReadPipePfd`,
  `outputPfd`, `currentRecordingUri`, `currentCodecEnum`, `audioPipeReadScope`, `isPaused`) (L55–112)
- `startPipeline()` completo (L118–223)
- `release()` (L237–253)
- `cancel()` (L260–270)

**Código actualmente en RecordingForegroundService:**
- `tryGetFinalNumberFromLog()` (L393–422) — lógica de negocio ajena al lifecycle de Service
- Lógica de renombrado post-sesión con CallLog (L350–381)

**Contrato propuesto:**
```kotlin
// services/recording/RecordingSessionManager.kt
class RecordingSessionManager(private val context: Context) {
    suspend fun startSession(service: IShellService, metadata: EnrichedCallData)
    fun stopSession(service: IShellService?)
    fun cancelSession(service: IShellService?)
    val currentEngine: AudioRecordingEngine?
    // tryGetFinalNumberFromLog() y lógica de renombrado se mueven aquí
}
```

`AudioRecordingEngine` quedaría reducido a un **data holder puro**
(solo variables, sin métodos de orquestación).

---

## 4. Riesgo de cada extracción

### ShellCommandExecutor — 🟢 Bajo

| Riesgo | Nivel |
|--------|-------|
| Lógica casi stateless — extracción mecánica | 🟢 |
| Depende de clases ya independientes (`ScrcpyConfig`, enums) | 🟢 |
| La verificación del JAR duplica la de `AudioRecordingEngine` — duplicación intencional (TOCTOU) | 🟢 |
| Sin cambio de contrato AIDL | 🟢 |

---

### ShellAudioPipeline — 🟡 Medio

| Riesgo | Nivel | Acción requerida |
|--------|-------|-----------------|
| `spawnAudioRelayCoroutine.finally` llama `stopRecording()` de ShellService | 🟡 | Reemplazar por callback/lambda inyectado |
| El relay lee `isRecordingActive` de ShellService | 🟡 | Inyectar el `AtomicBoolean` o un getter |
| Secuencia de cierre tiene orden crítico documentado | 🟡 | Preservar exactamente: process → scope → socket → pipe |
| El `shellScope` hoy vive en ShellService | 🟢 | Se puede mover directamente a la pipeline |

---

### RecordingSessionManager — 🔴 Alto

| Riesgo | Nivel | Acción requerida |
|--------|-------|-----------------|
| `AudioRecordingEngine` mezcla state holder + orchestrator | 🔴 | Separación cuidadosa en dos clases |
| `RecordingForegroundService` accede directamente a internals de Engine (`engine.audioPipeReadJob?.isActive`) | 🟡 | Exponer estado observable desde SessionManager |
| `tryGetFinalNumberFromLog()` usa `CoroutineScope(Dispatchers.IO)` suelto | 🟡 | Decidir scope owner antes de mover |
| Es el archivo con mayor impacto en RecordingForegroundService | 🔴 | Alto riesgo de regresión en la máquina de estados Standby/Starting/Active |

---

## 5. Orden recomendado de commits

Criterio: **menor riesgo primero, commits atómicos, sin cambio de lógica por commit**.

```
V2-1  ShellCommandExecutor
      Extraer launchScrcpyServer() + verifyServerJar() de ShellService.
      ShellService delega. Sin cambio de contrato AIDL. Sin cambio de lógica.
      Riesgo: BAJO

V2-2  ShellAudioPipeline
      Extraer socket + relay + log + process-monitor coroutines de ShellService.
      Resolver callback inverso relay→stopRecording antes de empezar.
      Riesgo: MEDIO

      [pausa — build + test antes de continuar app-side]

V2-3  tryGetFinalNumberFromLog fuera de RecordingForegroundService
      Mover a CallLogHelper (utils/) o inline en RecordingSessionManager.
      Cambio de ubicación pura, sin cambio de lógica.
      Riesgo: BAJO-MEDIO

V2-4  RecordingSessionManager
      Extraer startPipeline/release/cancel de AudioRecordingEngine.
      AudioRecordingEngine queda como data class/state holder.
      RecordingForegroundService consume RecordingSessionManager.
      Riesgo: ALTO — dejar para último, build + smoke test obligatorio.
```

> V2-1 y V2-2 son exclusivamente del **proceso shell** — tocan solo `services/shell/`.
> V2-3 y V2-4 son **app-side** — comparten referencias en `RecordingForegroundService`
> y deben ir seguidos para no dejar el Service en estado intermedio.

---

## Arquitectura propuesta al finalizar V2

```
services/shell/                          (proceso shell, UID 2000/0)
  ShellService.kt         — coordinador AIDL, @Keep, destroy()
  ShellCommandExecutor.kt — lanzar app_process, verificar JAR  [V2-1]
  ShellAudioPipeline.kt   — socket Unix, relay, pipe write-end [V2-2]

services/recording/                      (proceso app)
  RecordingForegroundService.kt — lifecycle Android, máquina de estados
  RecordingSessionManager.kt   — orquestación sesión completa   [V2-4]
  AudioRecordingEngine.kt      — state holder puro              [V2-4]
  RecordingNotificationHelper.kt
  RecordingServiceState.kt

utils/
  CallLogHelper.kt (o inline en SessionManager)                  [V2-3]
```
