===== AI PROJECT MEMORY =====


===== .ai/project.md =====
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

===== .ai/architecture.md =====
# Architecture Notes

## Flujo principal

Usuario
 ↓
Manager UI
 ↓
Shizuku API
 ↓
Server
 ↓
Sistema Android


## Principios

- Separación UI / servicio.
- Evitar regresiones en permisos.
- Mantener compatibilidad entre Phone, TV y Wear.

===== .ai/decisions.md =====
# Architectural Decisions

## Null safety Application Management

Decisión:
usar mapNotNull cuando applicationInfo pueda ser null.

Motivo:
evitar crashes sin cambiar lógica de autorización.


## TV Loading

Decisión:
no mostrar estado vacío mientras packagesResource está cargando.

Motivo:
evitar parpadeo visual.

===== .ai/tasks.md =====
# Current Tasks

- [ ] Comparar upstream/main
- [ ] Revisar mejoras integrables
- [ ] Crear índice semántico del código
- [ ] Automatizar resumen después de commits

===== .ai/git_memory.md =====
# Git Memory

Updated: 2026-07-07 03:32:22 UTC

## Branch

```
main
```

## HEAD

```
1eb8d83
1eb8d83 chore: add AI memory updater
```

## Working tree

```
 M .ai/git_memory.md
 M .ai/index/repo_index.md
 M app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingForegroundService.kt
?? .ai/antigravity_context_snapshot.md
?? scripts/agyctx
```

## Last diff stat

```
 .ai/git_memory.md           |   33 ++
 .ai/index/repo_index.md     | 1005 ++++++++++++++++++++++++++++++++++++++++++-
 scripts/ai_update_memory.sh |   45 ++
 3 files changed, 1079 insertions(+), 4 deletions(-)
```

===== .ai/index/repo_index.md =====
# Repo Index

Generated: 2026-07-07 03:32:22 UTC

## Git

```
main
1eb8d83
```

## Recent commits

```
1eb8d83 chore: add AI memory updater
4ff9526 chore: add local AI repo index
0d86d1d chore: add AI project memory structure
93134cc refactor: extract CallLogHelper from recording service
986962d refactor: extract ShellAudioPipeline from ShellService
a20fed9 Add local agent skills for ShizuCallRecorder workflow
df0979c refactor: extract ShellCommandExecutor from ShellService
e2f92c3 docs: add V2 backend separation audit
1837c8f refactor: move ShellService to services/shell package
8194d46 docs: add Cally integration implementation plan
02f9f2a Enhance recording share intent with call details
dc66185 Merge playback library into main
89540dc Enable Gradle configuration cache
0a99f8c Improve recordings library playback and ownership filtering
330e141 Fix recordings folder lookup
9b37b66 Optimize release APK size
fc7ef98 Add recordings playback library
4d2466a Add Media3 playback dependencies
8650636 Add Gradle wrapper
db53a32 Add Media3 playback dependencies
```

## Important files

./.agent/skills/android-audit/SKILL.md
./.agent/skills/evercall-release-check/SKILL.md
./.agent/skills/playstore-readiness/SKILL.md
./.agent/skills/root-compatibility-check/SKILL.md
./.agent/skills/root-emulation-audit/SKILL.md
./.agent/skills/shell-command-audit/SKILL.md
./.agent/skills/shizuku-audit/SKILL.md
./.agent/skills/stellar-nightzuku-compatibility/SKILL.md
./.agents/skills/code-review-and-quality/SKILL.md
./.agents/skills/debugging-and-error-recovery/SKILL.md
./.agents/skills/git-workflow-and-versioning/SKILL.md
./.agents/skills/incremental-implementation/SKILL.md
./.agents/skills/security-and-hardening/SKILL.md
./.agents/skills/shizucall-project-rules/SKILL.md
./.agents/skills/source-driven-development/SKILL.md
./.ai/antigravity_context_snapshot.md
./.ai/architecture.md
./.ai/decisions.md
./.ai/git_memory.md
./.ai/project.md
./.ai/tasks.md
./AGENTS.md
./CLAUDE.md
./CODEX.md
./CONTRIBUTING.md
./GEMINI.md
./README.md
./SECURITY.md
./app/build.gradle.kts
./app/src/main/AndroidManifest.xml
./app/src/main/java/com/kitsumed/shizucallrecorder/AppNavigationScreen.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/AppUrls.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/MainActivity.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ShizuApplication.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/data/call/CallDirection.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/data/call/EnrichedCallData.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/data/call/RawCallData.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioCodec.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioMuxer.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioSource.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyClient.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyConfig.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ServerExtractor.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/onboarding/OnboardingStatus.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/RecordingDecisionEngine.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/CallDetectionMode.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/CallDetectionOrchestrator.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/incall/InCallService.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateReceiver.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateSessionManager.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateTemporaryCache.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/AudioRecordingEngine.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingForegroundService.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingNotificationHelper.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingServiceState.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/shell/ShellAudioPipeline.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/shell/ShellCommandExecutor.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/services/shell/ShellService.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/system/SystemIntentHelpers.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/system/permissions/AppPermission.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/system/permissions/PermissionChecks.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/system/storage/SafHelper.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/ContactSelectionDialog.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/DropdownComponents.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/FileNameFormatDialog.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/ToggleListItem.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/DisclaimerScreen.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/PermissionsScreen.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/PlaybackScreen.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/RecordingsScreen.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/SettingsScreen.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/SponsorScreen.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/theme/Color.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/theme/Theme.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/theme/Type.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/AppNavigationViewModel.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/ContactPickerViewModel.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PermissionsViewModel.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PlaybackViewModel.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/utils/CallLogHelper.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/utils/PhoneNumberManager.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/utils/RecordingFileNameFormatter.kt
./app/src/main/java/com/kitsumed/shizucallrecorder/utils/SponsorNotificationHelper.kt
./app/src/main/res/drawable/ic_mic.xml
./app/src/main/res/drawable/ic_stop.xml
./app/src/main/res/mipmap-anydpi/ic_launcher.xml
./app/src/main/res/mipmap-anydpi/ic_launcher_round.xml
./app/src/main/res/values-de/strings.xml
./app/src/main/res/values-es/strings.xml
./app/src/main/res/values-fr/strings.xml
./app/src/main/res/values-hu/strings.xml
./app/src/main/res/values-it/strings.xml
./app/src/main/res/values-ja/strings.xml
./app/src/main/res/values-pl/strings.xml
./app/src/main/res/values-pt-rBR/strings.xml
./app/src/main/res/values-ru/strings.xml
./app/src/main/res/values-tr/strings.xml
./app/src/main/res/values-vi/strings.xml
./app/src/main/res/values-zh-rCN/strings.xml
./app/src/main/res/values/colors.xml
./app/src/main/res/values/strings.xml
./app/src/main/res/values/themes.xml
./app/src/main/res/xml/backup_rules.xml
./app/src/main/res/xml/data_extraction_rules.xml
./build.gradle.kts
./docs/SUPPORT.md
./docs/audits/auditoria_v2_separacion_responsabilidades.md
./docs/audits/plan_implementacion_cally.md
./docs/configuration.md
./settings.gradle.kts

## Kotlin symbols

app/src/main/java/com/kitsumed/shizucallrecorder/AppNavigationScreen.kt:67:fun AppNavigationScreen() {
app/src/main/java/com/kitsumed/shizucallrecorder/AppUrls.kt:14:object AppUrls {
app/src/main/java/com/kitsumed/shizucallrecorder/MainActivity.kt:22:class MainActivity : AppCompatActivity() {
app/src/main/java/com/kitsumed/shizucallrecorder/ShizuApplication.kt:18:class ShizuApplication : Application() {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:26:class AppPreferences(context: Context) {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:38:    object DefaultsValue {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:43:        fun LAST_FORCED_REMINDER_SUPPORT_PROJECT_TIME(context: Context): Long = (runCatching { context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime }.getOrDefault(Long.MIN_VALUE)) - 25920000000L // 300 days in milliseconds
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:97:    enum class Key(val id: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:140:    enum class IgnoreContactsMode(val key: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:155:            fun fromKey(key: String?): IgnoreContactsMode {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:166:    enum class ThemeMode(val key: String, val displayNameResId: Int) {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:177:            fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:210:    fun isDisclaimerAccepted() = getBoolean(Key.DISCLAIMER_ACCEPTED, DefaultsValue.DISCLAIMER_ACCEPTED)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:213:    fun setDisclaimerAccepted(accepted: Boolean) = setBoolean(Key.DISCLAIMER_ACCEPTED, accepted)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:216:    fun getLastForcedReminderSupportProjectTimeInApp() = getLong(Key.LAST_FORCED_REMINDER_SUPPORT_PROJECT_TIME_INAPP, DefaultsValue.LAST_FORCED_REMINDER_SUPPORT_PROJECT_TIME(appContext))
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:219:    fun setLastForcedReminderSupportProjectTimeInApp(time: Long) = setLong(Key.LAST_FORCED_REMINDER_SUPPORT_PROJECT_TIME_INAPP, time)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:222:    fun getLastForcedReminderSupportProjectTimeNotification() = getLong(Key.LAST_FORCED_REMINDER_SUPPORT_PROJECT_TIME_NOTIFICATION, DefaultsValue.LAST_FORCED_REMINDER_SUPPORT_PROJECT_TIME(appContext))
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:225:    fun setLastForcedReminderSupportProjectTimeNotification(time: Long) = setLong(Key.LAST_FORCED_REMINDER_SUPPORT_PROJECT_TIME_NOTIFICATION, time)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:230:    fun getRecordingFolderUri(): Uri? = getString(Key.RECORDING_FOLDER_URI, DefaultsValue.RECORDING_FOLDER_URI)?.toUri()
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:233:    fun setRecordingFolderUri(uri: Uri?) = setString(Key.RECORDING_FOLDER_URI, uri?.toString())
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:236:    fun isVibrationEnabled() = getBoolean(Key.VIBRATION_ENABLED, DefaultsValue.VIBRATION_ENABLED)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:239:    fun setVibrationEnabled(enabled: Boolean) = setBoolean(Key.VIBRATION_ENABLED, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:245:    fun getCallDetectionMode(): CallDetectionMode {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:267:    fun setCallDetectionMode(mode: CallDetectionMode) {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:275:    fun isRecordThirdPartyCallsEnabled() = getBoolean(Key.RECORD_THIRD_PARTY_CALLS, DefaultsValue.RECORD_THIRD_PARTY_CALLS)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:277:    fun setRecordThirdPartyCallsEnabled(enabled: Boolean) = setBoolean(Key.RECORD_THIRD_PARTY_CALLS, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:282:    fun isAutoRecordIncomingEnabled() = getBoolean(Key.AUTO_RECORD_INCOMING, DefaultsValue.AUTO_RECORD_INCOMING)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:285:    fun setAutoRecordIncomingEnabled(enabled: Boolean) = setBoolean(Key.AUTO_RECORD_INCOMING, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:288:    fun isAutoRecordOutgoingEnabled() = getBoolean(Key.AUTO_RECORD_OUTGOING, DefaultsValue.AUTO_RECORD_OUTGOING)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:291:    fun setAutoRecordOutgoingEnabled(enabled: Boolean) = setBoolean(Key.AUTO_RECORD_OUTGOING, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:296:    fun isIgnoreAnonymousIncomingEnabled() = getBoolean(Key.IGNORE_ANONYMOUS_INCOMING, DefaultsValue.IGNORE_ANONYMOUS_INCOMING)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:299:    fun setIgnoreAnonymousIncomingEnabled(enabled: Boolean) = setBoolean(Key.IGNORE_ANONYMOUS_INCOMING, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:302:    fun isIgnoreCrossCountryIncomingEnabled() = getBoolean(Key.IGNORE_CROSS_COUNTRY_INCOMING, DefaultsValue.IGNORE_CROSS_COUNTRY_INCOMING)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:305:    fun setIgnoreCrossCountryIncomingEnabled(enabled: Boolean) = setBoolean(Key.IGNORE_CROSS_COUNTRY_INCOMING, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:308:    fun isIgnoreCrossCountryOutgoingEnabled() = getBoolean(Key.IGNORE_CROSS_COUNTRY_OUTGOING, DefaultsValue.IGNORE_CROSS_COUNTRY_OUTGOING)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:311:    fun setIgnoreCrossCountryOutgoingEnabled(enabled: Boolean) = setBoolean(Key.IGNORE_CROSS_COUNTRY_OUTGOING, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:314:    fun getIgnoreContactsModeIncoming() = IgnoreContactsMode.fromKey(getString(Key.IGNORE_CONTACTS_MODE_INCOMING, DefaultsValue.IGNORE_CONTACTS_MODE_INCOMING.key))
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:317:    fun setIgnoreContactsModeIncoming(mode: IgnoreContactsMode) = setString(Key.IGNORE_CONTACTS_MODE_INCOMING, mode.key)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:320:    fun getIgnoreContactsModeOutgoing() = IgnoreContactsMode.fromKey(getString(Key.IGNORE_CONTACTS_MODE_OUTGOING, DefaultsValue.IGNORE_CONTACTS_MODE_OUTGOING.key))
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:323:    fun setIgnoreContactsModeOutgoing(mode: IgnoreContactsMode) = setString(Key.IGNORE_CONTACTS_MODE_OUTGOING, mode.key)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:326:    fun getIgnoredContactsIncoming() = getStringSet(Key.IGNORED_CONTACTS_INCOMING, DefaultsValue.IGNORED_CONTACTS_INCOMING)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:329:    fun setIgnoredContactsIncoming(numbers: Set<String>) = setStringSet(Key.IGNORED_CONTACTS_INCOMING, numbers)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:332:    fun getIgnoredContactsOutgoing() = getStringSet(Key.IGNORED_CONTACTS_OUTGOING, DefaultsValue.IGNORED_CONTACTS_OUTGOING)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:335:    fun setIgnoredContactsOutgoing(numbers: Set<String>) = setStringSet(Key.IGNORED_CONTACTS_OUTGOING, numbers)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:340:    fun isLoggingEnabled() = getBoolean(Key.LOGGING_ENABLED, DefaultsValue.LOGGING_ENABLED)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:343:    fun setLoggingEnabled(enabled: Boolean) = setBoolean(Key.LOGGING_ENABLED, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:346:    fun isDebugEnabled() = getBoolean(Key.DEBUG_ENABLED, DefaultsValue.DEBUG_ENABLED)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:349:    fun setDebugEnabled(enabled: Boolean) = setBoolean(Key.DEBUG_ENABLED, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:352:    fun getDebugCallerNumber() = getString(Key.DEBUG_CALLER_NUMBER, DefaultsValue.DEBUG_CALLER_NUMBER) ?: DefaultsValue.DEBUG_CALLER_NUMBER
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:355:    fun setDebugCallerNumber(number: String) = setString(Key.DEBUG_CALLER_NUMBER, number)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:360:    fun getAudioSource() = getString(Key.AUDIO_SOURCE, DefaultsValue.AUDIO_SOURCE) ?: DefaultsValue.AUDIO_SOURCE
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:363:    fun setAudioSource(source: String) = setString(Key.AUDIO_SOURCE, source)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:366:    fun getAudioCodec() = getString(Key.AUDIO_CODEC, DefaultsValue.AUDIO_CODEC) ?: DefaultsValue.AUDIO_CODEC
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:369:    fun setAudioCodec(codec: String) = setString(Key.AUDIO_CODEC, codec)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:372:    fun getAudioBitRate(): Int {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:382:    fun setAudioBitRate(bitRate: Int) {
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:394:    fun getFileNameTemplate() = getString(Key.FILE_NAME_TEMPLATE, DefaultsValue.FILE_NAME_TEMPLATE) ?: DefaultsValue.FILE_NAME_TEMPLATE
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:397:    fun setFileNameTemplate(template: String) = setString(Key.FILE_NAME_TEMPLATE, template)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:402:    fun getThemeMode() = ThemeMode.fromKey(getString(Key.THEME_MODE, DefaultsValue.THEME_MODE.key))
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:405:    fun setThemeMode(mode: ThemeMode) = setString(Key.THEME_MODE, mode.key)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:408:    fun isDynamicColorEnabled() = getBoolean(Key.DYNAMIC_COLOR, DefaultsValue.DYNAMIC_COLOR)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:411:    fun setDynamicColorEnabled(enabled: Boolean) = setBoolean(Key.DYNAMIC_COLOR, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:414:    fun isShowToastsEnabled() = getBoolean(Key.SHOW_TOASTS, DefaultsValue.SHOW_TOASTS)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:417:    fun setShowToastsEnabled(enabled: Boolean) = setBoolean(Key.SHOW_TOASTS, enabled)
