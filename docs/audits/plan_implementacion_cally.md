# Plan de Implementación: ShizuCallRecorder-joselo ← Ideas de Cally

> **Decisión firmada**: Base = ShizuCallRecorder-joselo · Motor = scrcpy-server · WrappedShellContext = experimental futuro
> Generado: 2026-07-06 · Modelo: Claude Sonnet 4.6 Thinking

---

## Estado Actual del Backend (Auditoría Pre-Commit)

| Archivo | Ubicación actual | Problema detectado |
|---|---|---|
| `ShellService.kt` | `services/` (raíz) | Debe estar en `services/shell/` |
| `AudioRecordingEngine.kt` | `services/recording/` | Correcto, no mover |
| `IShellService.aidl` | `aidl/…/shizucallrecorder/` | OK, falta regla ProGuard explícita |
| `proguard-rules.pro` | raíz del módulo | Falta `-keep` para AIDL Stub y ShellService |
| `ScrcpyAudioSource.kt` | `integrations/scrcpy/` | Ya tiene VOICE_CALL/UPLINK/DOWNLINK/MIC — base perfecta para fallback |
| `AppPreferences.kt` | `data/` | Necesitará claves para estrategia por dispositivo |

### Lo que NO existe aún

- `AudioLevelMeter` — medir RMS del stream entrante
- `RecordingCapabilitiesStore` — persistir estrategia exitosa por `Build.FINGERPRINT`
- Lógica de fallback entre `ScrcpyAudioSource` en `AudioRecordingEngine`
- Soporte para grabación dual (dos pistas)
- `VolumeMixer` en `PlaybackScreen`
- Waveform en `PlaybackScreen`

---

## V1 — Refactor de Estructura (Sin Cambios Funcionales)

**Objetivo**: Mover `ShellService` a su paquete correcto, separar responsabilidades, reforzar ProGuard.
El build debe quedar **bit-a-bit idéntico** en comportamiento.

---

### Commit V1-A
```
refactor: move ShellService to services/shell package
```

**Archivos a tocar**:
- Mover `services/ShellService.kt` → `services/shell/ShellService.kt`
- Actualizar `package`: `com.kitsumed.shizucallrecorder.services` → `…services.shell`
- Actualizar todos los `import` que referencian la clase vieja

**Riesgo**: Bajo — solo cambio de paquete.

**Prueba mínima**:
```bash
./gradlew assembleDebug
grep -r "services.ShellService" app/src/main/java/
# Debe devolver 0 resultados
```

**Rollback**: `git revert HEAD`

---

### Commit V1-B
```
refactor: extract ShellCommandExecutor from ShellService
```

**Objetivo**: Crear `ShellCommandExecutor.kt` con la lógica de `grantAppOps()`. `ShellService` delega.

**Archivos a tocar**:
- Crear `services/shell/ShellCommandExecutor.kt`
- Extraer cuerpo de `grantAppOps()` desde `ShellService.kt`

**Contenido de ShellCommandExecutor.kt**:
```kotlin
package com.kitsumed.shizucallrecorder.services.shell

import com.kitsumed.shizucallrecorder.utils.AppLogger

object ShellCommandExecutor {
    private const val TAG = "SCR:ShellCommandExecutor"

    fun grantAppOps(packageName: String, opName: String, userProfileId: Int): Boolean {
        return try {
            val process = ProcessBuilder(
                "appops", "set", "--user", userProfileId.toString(),
                packageName, opName, "allow"
            ).start()
            val error  = process.errorStream.bufferedReader().readText().trim()
            val output = process.inputStream.bufferedReader().readText().trim()
            val code   = process.waitFor()
            AppLogger.i(TAG, "exit=$code out=${output.ifBlank{"Empty"}} err=${error.ifBlank{"Empty"}}")
            code == 0 && error.isBlank()
        } catch (e: Exception) {
            AppLogger.e(TAG, "grantAppOps failed: ${e.message}", e)
            false
        }
    }
}
```

**Riesgo**: Bajo — comportamiento idéntico.

**Prueba mínima**:
```bash
./gradlew assembleDebug
# Conceder un AppOp manualmente desde la app y verificar que funciona.
```

**Rollback**: `git revert HEAD`

---

### Commit V1-C
```
refactor: extract ShellAudioPipeline from ShellService
```

**Objetivo**: Crear `ShellAudioPipeline.kt` con los tres métodos de corrutinas:
`spawnAudioRelayCoroutine`, `spawnLogConsumerCoroutine`, `spawnProcessMonitorCoroutine`.
`ShellService` crea una instancia de `ShellAudioPipeline` y le delega `start/stop`.

**Archivos a tocar**:
- Crear `services/shell/ShellAudioPipeline.kt`
- Extraer de `ShellService.kt`: los tres métodos `spawn*` y sus propiedades de estado
- `ShellService` pasa a tener `private val pipeline = ShellAudioPipeline()`

**Firma de ShellAudioPipeline**:
```kotlin
class ShellAudioPipeline {
    val isActive: Boolean get() = isRecordingActive.get()

    fun start(config: PipelineStartConfig): ParcelFileDescriptor?
    fun stop()
}

data class PipelineStartConfig(
    val serverPath: String,
    val audioSource: ScrcpyAudioSource,
    val audioCodec: ScrcpyAudioCodec,
    val audioBitRate: Int,
    val verbose: Boolean
)
```

> **CRÍTICO**: No cambiar el orden de cierre establecido en `stopRecording()`:
> proceso → relay job → scope → clientConnection → serverSocket → audioWriteEnd

**Riesgo**: Medio — la lógica de cierre de sockets es delicada.

**Prueba mínima**:
```bash
./gradlew assembleDebug
# Iniciar llamada real y verificar que la grabación empieza y termina.
adb logcat | grep "SCR:"
```

**Rollback**: `git revert HEAD`

---

### Commit V1-D
```
fix: add ProGuard keep rules for ShellService and AIDL stubs
```

**Archivos a tocar**: `app/proguard-rules.pro`

**Reglas a añadir**:
```proguard
# ShellService: cargado por reflexión desde Shizuku.
# @Keep en source no es suficiente con R8 full mode en release.
-keep class com.kitsumed.shizucallrecorder.services.shell.ShellService {
    public <init>();
    public <init>(android.content.Context);
}

# AIDL Stub classes: usadas por Binder IPC entre procesos.
-keep class com.kitsumed.shizucallrecorder.IShellService$Stub { *; }
-keep class com.kitsumed.shizucallrecorder.ILogCallback$Stub  { *; }

# ShellCommandExecutor: invocado desde el proceso shell.
-keep class com.kitsumed.shizucallrecorder.services.shell.ShellCommandExecutor { *; }
```

**Riesgo**: Bajo — solo agrega reglas.

**Prueba mínima**:
```bash
./gradlew assembleRelease
# Verificar con jadx que ShellService mantiene su nombre real.
# Reconectar Shizuku con la app release y verificar que el UserService arranca.
```

**Rollback**: `git checkout HEAD~1 -- app/proguard-rules.pro`

---

## V2 — Watchdog de Audibilidad (Inspirado en Cally)

**Objetivo**: Detectar si el audio grabado es silencio. No romper grabaciones que sí funcionan.

---

### Commit V2-A
```
feat: add AudioLevelMeter utility for silence detection
```

**Nota técnica**: scrcpy-server entrega audio **ya codificado** (Opus/AAC), no PCM.
`AudioLevelMeter` mide el **tamaño promedio de paquetes** como proxy de energía.
Paquetes Opus de silencio DTX tienen 1–3 bytes; paquetes de voz activa tienen >20 bytes.

**Archivos a crear**: `services/recording/AudioLevelMeter.kt`

```kotlin
package com.kitsumed.shizucallrecorder.services.recording

class AudioLevelMeter(
    private val minAvgPacketSizeBytes: Int = 8,
    private val windowSize: Int = 50
) {
    private val sizes = ArrayDeque<Int>(windowSize)

    fun feed(packetSizeBytes: Int) {
        if (sizes.size >= windowSize) sizes.removeFirst()
        sizes.addLast(packetSizeBytes)
    }

    val hasAudio: Boolean
        get() = sizes.isNotEmpty() && sizes.average() >= minAvgPacketSizeBytes

    val isWindowFull: Boolean
        get() = sizes.size >= windowSize

    fun reset() = sizes.clear()
}
```

**Riesgo**: Ninguno — clase aislada sin dependencias.

**Prueba mínima**:
```bash
./gradlew testDebugUnitTest
# Test unitario: feed(2)×50 → hasAudio=false; feed(30)×50 → hasAudio=true
```

---

### Commit V2-B
```
feat: integrate AudioLevelMeter into AudioRecordingEngine
```

**Archivos a tocar**: `services/recording/AudioRecordingEngine.kt`

**Cambios concretos**:
1. Añadir: `private val levelMeter = AudioLevelMeter()`
2. En `onAudioPacket`: añadir `levelMeter.feed(packet.data.size)` antes del `writePacket`
3. Añadir propiedad pública: `var onSilenceDetected: (() -> Unit)? = null`
4. En `startPipeline`, dentro del scope de lectura, añadir corrutina watchdog:

```kotlin
scope.launch {
    delay(5_000)
    if (levelMeter.isWindowFull && !levelMeter.hasAudio) {
        AppLogger.w(TAG, "SILENCE WATCHDOG: no audio in first 5s. Source=${audioSourceEnum.cliKey}")
        onSilenceDetected?.invoke()
    }
}
```

**Riesgo**: Bajo — el callback es opcional; si es null no pasa nada.

**Prueba mínima**:
```bash
# Forzar fuente con silencio (ej: "output" en dispositivo sin llamada activa)
adb logcat | grep "SILENCE WATCHDOG"
# Verificar que la grabación NO se rompe después del log.
```

---

### Commit V2-C
```
feat: add RecordingCapabilitiesStore to persist best source per device
```

**Archivos a crear**: `data/RecordingCapabilitiesStore.kt`
**Archivos a tocar**: `data/AppPreferences.kt` (métodos de acceso)

```kotlin
package com.kitsumed.shizucallrecorder.data

import android.content.Context
import android.os.Build
import androidx.core.content.edit

class RecordingCapabilitiesStore(context: Context) {
    private val prefs = context.getSharedPreferences("recording_capabilities", Context.MODE_PRIVATE)
    private val key   = "best_source_${Build.FINGERPRINT.hashCode()}"

    fun getBestSource(): String? = prefs.getString(key, null)
    fun saveBestSource(cliKey: String) = prefs.edit { putString(key, cliKey) }
    fun clear() = prefs.edit { remove(key) }
}
```

**Riesgo**: Ninguno — SharedPreferences aislado.

**Prueba mínima**:
```bash
# Grabar una llamada exitosa.
# adb shell run-as com.kitsumed.shizucallrecorder \
#   cat shared_prefs/recording_capabilities.xml
# Verificar que aparece la clave con la fuente usada.
```

---

## V3 — Fallback de Fuente de Audio (Inspirado en Cally)

**Objetivo**: Si el watchdog dispara `onSilenceDetected`, reiniciar el pipeline con la siguiente fuente.

### Cadena de Fallback

```
VOICE_CALL (default actual)
    ↓ silencio 5 s
VOICE_CALL_DOWNLINK
    ↓ silencio
VOICE_COMMUNICATION (mic procesado)
    ↓ silencio
MIC  ← último recurso, advertir al usuario
```

> **Importante**: scrcpy-server soporta todas estas fuentes. No se necesita cambiar AIDL.

---

### Commit V3-A
```
feat: define RecordingSourceFallbackChain
```

**Archivos a crear**: `services/recording/RecordingSourceFallbackChain.kt`

```kotlin
package com.kitsumed.shizucallrecorder.services.recording

import com.kitsumed.shizucallrecorder.integrations.scrcpy.ScrcpyAudioSource

object RecordingSourceFallbackChain {

    val chain: List<ScrcpyAudioSource> = listOf(
        ScrcpyAudioSource.VOICE_CALL,
        ScrcpyAudioSource.VOICE_CALL_DOWNLINK,
        ScrcpyAudioSource.VOICE_COMMUNICATION,
        ScrcpyAudioSource.MIC
    )

    fun next(current: ScrcpyAudioSource): ScrcpyAudioSource? {
        val idx = chain.indexOf(current)
        return if (idx >= 0 && idx < chain.lastIndex) chain[idx + 1] else null
    }

    fun isLastResort(source: ScrcpyAudioSource): Boolean = source == ScrcpyAudioSource.MIC
}
```

**Riesgo**: Ninguno.

---

### Commit V3-B
```
feat: implement automatic source fallback in RecordingForegroundService
```

**Archivos a tocar**: `services/recording/RecordingForegroundService.kt`

**Lógica a añadir** (pseudocódigo):
```kotlin
var currentSource = audioSourceEnum // leído de preferencias

engine.onSilenceDetected = {
    val nextSource = RecordingSourceFallbackChain.next(currentSource)
    if (nextSource != null) {
        AppLogger.w(TAG, "Fallback: ${currentSource.cliKey} → ${nextSource.cliKey}")
        currentSource = nextSource
        restartPipelineWithSource(nextSource)   // reusar el mismo archivo SAF
    } else {
        AppLogger.w(TAG, "Sin fallback. Solo micrófono activo.")
        notifyUserPartialRecording()
    }
}

fun notifyUserPartialRecording() {
    // Actualizar texto de la notificación foreground con R.string.recording_warning_mic_only
    // NO detener la grabación — seguir con MIC.
}
```

> **CRÍTICO**: Usar un `Mutex` de corrutinas para serializar el acceso al reinicio del pipeline.
> Si `stopRecording()` tarda más de 2 s y el reinicio ya comenzó, puede haber corrupción de estado.

**Riesgo**: Medio — el reinicio de pipeline es complejo.

**Prueba mínima**:
```bash
# En dispositivo Samsung con VOICE_CALL_UPLINK bloqueado:
# 1. Forzar fuente "voice-call-uplink" en preferencias
# 2. Grabar llamada
adb logcat | grep "Fallback:"
# 3. Verificar que la grabación continúa con la siguiente fuente
```

---

### Commit V3-C
```
feat: persist successful source to RecordingCapabilitiesStore after recording
```

**Archivos a tocar**:
- `services/recording/RecordingForegroundService.kt`

**Lógica**: Al finalizar la grabación, si `levelMeter.hasAudio == true`, llamar:
```kotlin
RecordingCapabilitiesStore(context).saveBestSource(currentSource.cliKey)
```

Al iniciar la siguiente grabación, leer la fuente guardada como fuente inicial:
```kotlin
val savedSource = RecordingCapabilitiesStore(context).getBestSource()
val startSource = savedSource?.let { ScrcpyAudioSource.fromKey(it) } ?: defaultSource
```

**Riesgo**: Bajo.

---

## V4 — Evaluación Dual-Track

### Análisis Técnico Pre-Decisión

**¿Puede scrcpy-server hacer dual-track?**

**No directamente.** scrcpy-server abre **una sola fuente** por instancia. Para capturar
UPLINK + DOWNLINK se necesitarían dos instancias separadas del servidor.

**Opciones**:
1. Dos instancias de scrcpy-server → dos sockets → dos pipes → dos archivos
2. Backend Cally (WrappedShellContext + AudioRecord nativo) → V6 experimental futuro

---

### Commit V4-A
```
research: document dual-track feasibility assessment in AGENTS.md
```

**Archivos a tocar**: `AGENTS.md`

**Contenido a añadir**:
```markdown
## Dual-Track Audio — Decisión 2026-07-06

Dual-track con scrcpy-server requiere dos instancias simultáneas del servidor.
Implica: ampliar IShellService.aidl, gestionar dos ShellAudioPipeline en paralelo,
crear dos archivos SAF de salida.

PREREQUISITO: verificar en V3 que VOICE_CALL_UPLINK y VOICE_CALL_DOWNLINK funcionan
por separado en el dispositivo de prueba. Si solo funciona VOICE_CALL (mezcla única),
no hay beneficio real en el dual-track con scrcpy-server.

Si dual-track con scrcpy no es viable, mover a V6-experimental con WrappedShellContext.
```

**Riesgo**: Ninguno.

---

### Commit V4-B *(CONDICIONAL — solo si pruebas de V3 confirman viabilidad)*
```
feat: extend IShellService.aidl for dual-track recording
```

**Archivos a tocar**: `IShellService.aidl`

```aidl
// IDs existentes (NO modificar): start=1, stop=2, isRecording=3, grantAppOps=4, destroy=16777114
// Nuevos IDs para dual-track:
ParcelFileDescriptor startUplinkRecording(
    String audioCodec, int audioBitRate, String serverPath,
    boolean isDebuggingModeEnabled, ILogCallback appLoggerCallback
) = 5;

ParcelFileDescriptor startDownlinkRecording(
    String audioCodec, int audioBitRate, String serverPath,
    boolean isDebuggingModeEnabled, ILogCallback appLoggerCallback
) = 6;

void stopDualRecording() = 7;
boolean isDualRecording() = 8;
```

> **CRÍTICO**: Los IDs de transacción AIDL son contrato binario inmutable.
> Una vez publicados, nunca cambiar ni reusar IDs existentes.
> Este commit obliga a reinstalar el UserService de Shizuku.

**Riesgo**: Alto.

**Prueba mínima**:
```bash
./gradlew assembleDebug
# Desconectar y reconectar Shizuku.
adb logcat | grep "Binder\|ShellService\|AIDL"
# No debe haber errores de versión de transacción.
```

---

## V5 — Mejoras al Reproductor (PlaybackScreen)

**Objetivo**: Waveform simple primero. VolumeMixer solo cuando `isDual == true`.

---

### Commit V5-A
```
feat: add single-track waveform to PlaybackScreen
```

**Archivos a crear**: `services/recording/WaveformBuilder.kt`
**Archivos a tocar**: `ui/screens/PlaybackScreen.kt`, `ui/viewmodels/PlaybackViewModel.kt`

**Firma de WaveformBuilder.kt**:
```kotlin
package com.kitsumed.shizucallrecorder.services.recording

import android.media.MediaExtractor
import java.io.FileDescriptor

object WaveformBuilder {
    private const val BIN_COUNT = 120

    /**
     * Decodifica el archivo y devuelve [BIN_COUNT] amplitudes normalizadas [0, 1].
     * Ejecutar en Dispatchers.Default. Retorna null si falla la decodificación.
     */
    fun buildBins(fd: FileDescriptor): FloatArray? = runCatching {
        // Implementación completa: MediaExtractor + MediaCodec PCM → RMS por ventana → normalizar
        FloatArray(BIN_COUNT) // placeholder — implementar en el commit real
    }.getOrNull()
}
```

**Riesgo**: Bajo — puramente de lectura, composable opcional.

---

### Commit V5-B
```
feat: add dual-track VolumeMixer to PlaybackScreen (gated by isDual)
```

**Archivos a tocar**: `ui/screens/PlaybackScreen.kt`, `ui/viewmodels/PlaybackViewModel.kt`

**En PlaybackViewModel**:
```kotlin
val isDual: Boolean
    get() = uplinkPath != null && downlinkPath != null
```

**VolumeMixer composable** (solo se muestra si `isDual == true`):
```kotlin
@Composable
fun VolumeMixer(
    volA: Float, onVolA: (Float) -> Unit,
    volB: Float, onVolB: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tu voz", style = MaterialTheme.typography.labelSmall)
        Slider(value = volA, onValueChange = onVolA, valueRange = 0f..1f)
        Text("Interlocutor", style = MaterialTheme.typography.labelSmall)
        Slider(value = volB, onValueChange = onVolB, valueRange = 0f..1f)
    }
}
```

**Riesgo**: Bajo — `isDual` actualmente siempre es `false` hasta que V4 implemente el dual-track.

---

## Tabla Maestra de Commits

| ID | Nombre del commit | Archivos clave | Riesgo | Rollback |
|---|---|---|---|---|
| **V1-A** | `refactor: move ShellService to services/shell` | `ShellService.kt` (move) + imports | Bajo | `git revert HEAD` |
| **V1-B** | `refactor: extract ShellCommandExecutor` | `ShellCommandExecutor.kt` (nuevo) · `ShellService.kt` | Bajo | `git revert HEAD` |
| **V1-C** | `refactor: extract ShellAudioPipeline` | `ShellAudioPipeline.kt` (nuevo) · `ShellService.kt` | Medio | `git revert HEAD` |
| **V1-D** | `fix: ProGuard keep rules for ShellService+AIDL` | `proguard-rules.pro` | Bajo | `git checkout HEAD~1 -- proguard-rules.pro` |
| **V2-A** | `feat: add AudioLevelMeter` | `AudioLevelMeter.kt` (nuevo) | Ninguno | `git revert HEAD` |
| **V2-B** | `feat: integrate AudioLevelMeter into engine` | `AudioRecordingEngine.kt` | Bajo | `git revert HEAD` |
| **V2-C** | `feat: add RecordingCapabilitiesStore` | `RecordingCapabilitiesStore.kt` (nuevo) | Ninguno | `git revert HEAD` |
| **V3-A** | `feat: define RecordingSourceFallbackChain` | `RecordingSourceFallbackChain.kt` (nuevo) | Ninguno | `git revert HEAD` |
| **V3-B** | `feat: implement source fallback in ForegroundService` | `RecordingForegroundService.kt` | Medio | `git revert HEAD` |
| **V3-C** | `feat: persist successful source` | `RecordingForegroundService.kt` · `AppPreferences.kt` | Bajo | `git revert HEAD` |
| **V4-A** | `research: dual-track feasibility in AGENTS.md` | `AGENTS.md` | Ninguno | `git revert HEAD` |
| **V4-B** *(cond.)* | `feat: extend AIDL for dual-track` | `IShellService.aidl` · `ShellService.kt` | Alto | `git revert HEAD` + reinstalar UserService |
| **V5-A** | `feat: single-track waveform` | `WaveformBuilder.kt` · `PlaybackScreen.kt` | Bajo | `git revert HEAD` |
| **V5-B** | `feat: dual-track VolumeMixer (gated)` | `PlaybackScreen.kt` · `PlaybackViewModel.kt` | Bajo | `git revert HEAD` |

---

## Comandos de Verificación

### Antes de cada commit
```bash
cd /data/data/com.termux/files/home/repos/ShizuCallRecorder-joselo

git status
git diff --check            # detecta whitespace errors

./gradlew assembleDebug     # compilar siempre antes de commitear
```

### Post V1-A — verificar paquete correcto
```bash
# Debe devolver 0 resultados (referencia al paquete viejo eliminada):
grep -rn "import com.kitsumed.shizucallrecorder.services.ShellService" app/src/

# Debe devolver >= 1 resultado (nueva ubicación existe):
grep -rn "services.shell.ShellService" app/src/
```

### Post V1 completo — verificar R8/ProGuard
```bash
./gradlew assembleRelease
./gradlew lintDebug 2>&1 | grep -E "Error|Warning" | head -30

# Si jadx disponible — verificar que ShellService mantiene su nombre:
# jadx app/build/outputs/apk/release/app-release.apk -d /tmp/jadx_out
# grep -r "ShellService" /tmp/jadx_out/
```

### Post V2 — verificar watchdog
```bash
# Forzar fuente que produce silencio en el dispositivo de prueba.
# Iniciar grabación. Esperar 5+ segundos. Verificar log:
adb logcat -s "SCR:AudioRecordingEngine" | grep -i "silence\|watchdog"

# Verificar que la grabación NO se detiene sola:
adb logcat -s "SCR:RecordingForegroundService" | grep -i "recording"
```

### Post V3 — verificar fallback
```bash
adb logcat -s "SCR:RecordingForegroundService" | grep -i "fallback\|silencio\|fuente"

# Verificar que el archivo SAF resultante tiene tamaño > 0:
# Reproducir en RecordingsScreen y verificar que hay sonido.
```

### Verificación AIDL (crítico post V4-B)
```bash
grep -n "= [0-9]" app/src/main/aidl/com/kitsumed/shizucallrecorder/IShellService.aidl
# Deben mantenerse invariables:
# startRecording=1  stopRecording=2  isRecording=3  grantAppOps=4  destroy=16777114
```

---

## Orden de Ejecución

```
V1-A → V1-B → V1-C → [assembleRelease] → V1-D
                    ↓
              V2-A → V2-B → V2-C
                    ↓
              V3-A → V3-B → V3-C
                    ↓
              V4-A → [evaluar en dispositivo real]
                    ↓ solo si UPLINK/DOWNLINK funcionan separados
              V4-B (condicional)
                    ↓
              V5-A → V5-B
```

**Regla**: Cada versión (V1, V2, V3…) debe compilar y funcionar en dispositivo real
antes de iniciar la siguiente. No mezclar commits de diferentes versiones en la misma sesión.
