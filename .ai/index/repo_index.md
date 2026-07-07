# Repo Index

Generated: 2026-07-07 03:26:27 UTC

## Git

```
main
0d86d1d
```

## Recent commits

```
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
842e450 chore(ui): Make UI smoother, better animations
7994d1b feat(onboarding): Improved onboarding experience, more permission control
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
./.ai/architecture.md
./.ai/decisions.md
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
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:422:    fun isShizukuAutoManageEnabled() = getBoolean(Key.SHIZUKU_AUTO_MANAGE, DefaultsValue.SHIZUKU_AUTO_MANAGE)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:425:    fun setShizukuAutoManageEnabled(enabled: Boolean) = setBoolean(Key.SHIZUKU_AUTO_MANAGE, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:428:    fun isShizukuStartOnRecordEnabled() = getBoolean(Key.SHIZUKU_START_ON_RECORD, DefaultsValue.SHIZUKU_START_ON_RECORD)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:431:    fun setShizukuStartOnRecordEnabled(enabled: Boolean) = setBoolean(Key.SHIZUKU_START_ON_RECORD, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:434:    fun isShizukuKeepAliveEnabled() = getBoolean(Key.SHIZUKU_KEEP_ALIVE, DefaultsValue.SHIZUKU_KEEP_ALIVE)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:437:    fun setShizukuKeepAliveEnabled(enabled: Boolean) = setBoolean(Key.SHIZUKU_KEEP_ALIVE, enabled)
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:440:    fun getShizukuAuthKey() = getString(Key.SHIZUKU_AUTH_KEY, DefaultsValue.SHIZUKU_AUTH_KEY) ?: DefaultsValue.SHIZUKU_AUTH_KEY
app/src/main/java/com/kitsumed/shizucallrecorder/data/AppPreferences.kt:443:    fun setShizukuAuthKey(key: String) = setString(Key.SHIZUKU_AUTH_KEY, key)
app/src/main/java/com/kitsumed/shizucallrecorder/data/call/CallDirection.kt:20:enum class CallDirection(val token: String, val labelResId: Int) {
app/src/main/java/com/kitsumed/shizucallrecorder/data/call/CallDirection.kt:34:        fun fromCallStateOrNull(callState: Int): CallDirection? {
app/src/main/java/com/kitsumed/shizucallrecorder/data/call/CallDirection.kt:51:        fun fromToken(token: String?): CallDirection? =
app/src/main/java/com/kitsumed/shizucallrecorder/data/call/EnrichedCallData.kt:31:data class EnrichedCallData(
app/src/main/java/com/kitsumed/shizucallrecorder/data/call/EnrichedCallData.kt:43:    fun getBestNumber() = formattedE164Number ?: normalisedPhoneNumber
app/src/main/java/com/kitsumed/shizucallrecorder/data/call/RawCallData.kt:23:data class RawCallData(
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioCodec.kt:31:enum class ScrcpyAudioCodec(
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioCodec.kt:82:        fun fromKey(key: String): ScrcpyAudioCodec =
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioCodec.kt:92:        fun fromFourCC(fourCC: Int): ScrcpyAudioCodec =
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioMuxer.kt:41:class ScrcpyAudioMuxer(
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioMuxer.kt:110:    fun initialize(codec: ScrcpyAudioCodec) {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioMuxer.kt:128:    fun writePacket(packet: ScrcpyClient.AudioPacket, codec: ScrcpyAudioCodec) {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioSource.kt:38:enum class ScrcpyAudioSource(
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioSource.kt:207:        fun fromKey(key: String): ScrcpyAudioSource =
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyClient.kt:55:class ScrcpyClient(
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyClient.kt:98:    interface AudioPacketListener {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyClient.kt:104:        fun onMetadataReceived(codec: ScrcpyAudioCodec)
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyClient.kt:111:        fun onAudioPacket(packet: AudioPacket)
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyClient.kt:118:        fun onStreamEnd(error: String?)
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyClient.kt:131:    data class AudioPacket(
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyClient.kt:176:    fun start() {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyClient.kt:253:    fun stop() {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyConfig.kt:29:object ScrcpyConfig {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyConfig.kt:67:    fun getServerPath(context: Context): String {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyConfig.kt:117:    fun buildServerArgs(
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyConfig.kt:160:    fun getRandomSocketName(): String {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ServerExtractor.kt:25:object ServerExtractor {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ServerExtractor.kt:40:    fun ensureServerFile(context: Context, serverPath: String): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ServerExtractor.kt:107:    fun verifyServerHash(file: File): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:43:class ShizukuConnectionManager(
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:61:        fun isAvailable(): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:76:        fun hasPermission(context: Context? = null): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:101:        fun checkServerPermission(permissionName: String): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:130:        fun requestPermission() {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:141:        fun getPackageName(context: Context): String? {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:154:        fun startServer(context: Context, authKey: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:184:        fun stopServer(context: Context, authKey: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:359:        fun bindServiceInternal() {
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:411:    fun unbind() {
app/src/main/java/com/kitsumed/shizucallrecorder/onboarding/OnboardingStatus.kt:25:object OnboardingStatus {
app/src/main/java/com/kitsumed/shizucallrecorder/onboarding/OnboardingStatus.kt:40:    data class Status(
app/src/main/java/com/kitsumed/shizucallrecorder/onboarding/OnboardingStatus.kt:54:        fun isComplete(): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/onboarding/OnboardingStatus.kt:74:    fun getStatus(context: Context, preferences: AppPreferences): Status {
app/src/main/java/com/kitsumed/shizucallrecorder/services/RecordingDecisionEngine.kt:36:class RecordingDecisionEngine private constructor(context: Context) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/RecordingDecisionEngine.kt:50:        fun getInstance(context: Context): RecordingDecisionEngine {
app/src/main/java/com/kitsumed/shizucallrecorder/services/RecordingDecisionEngine.kt:97:    fun endRecordingSession() {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/CallDetectionMode.kt:29:enum class CallDetectionMode(
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/CallDetectionMode.kt:81:    fun isSupportedOnCurrentApi(): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/CallDetectionMode.kt:93:        fun fromKey(key: String?): CallDetectionMode {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/CallDetectionMode.kt:101:        fun getDefaultModeForDevice(): CallDetectionMode {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/CallDetectionOrchestrator.kt:21:class CallDetectionOrchestrator(private val context: Context) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/CallDetectionOrchestrator.kt:34:    fun syncComponents() {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/incall/InCallService.kt:47:class InCallService : InCallService() {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateReceiver.kt:37:class PhoneStateReceiver : BroadcastReceiver() {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateSessionManager.kt:34:class PhoneStateSessionManager private constructor(context: Context) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateSessionManager.kt:48:        fun getInstance(context: Context): PhoneStateSessionManager {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateSessionManager.kt:125:        fun clear() {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateSessionManager.kt:178:    fun handlePhoneState(stateString: String, phoneNumber: String?) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateSessionManager.kt:269:    fun handleDebugAction(action: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateTemporaryCache.kt:25:class PhoneStateTemporaryCache(private val context: Context) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateTemporaryCache.kt:42:    fun save(direction: CallDirection?) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateTemporaryCache.kt:53:    fun restore(): CallDirection? {
app/src/main/java/com/kitsumed/shizucallrecorder/services/callDetection/phoneState/PhoneStateTemporaryCache.kt:75:    fun clear() {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/AudioRecordingEngine.kt:44:class AudioRecordingEngine {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/AudioRecordingEngine.kt:118:    fun startPipeline(context: Service, service: IShellService, metadata: EnrichedCallData) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/AudioRecordingEngine.kt:237:    fun release(shellService: IShellService?) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/AudioRecordingEngine.kt:260:    fun cancel(context: Context, shellService: IShellService?) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/AudioRecordingEngine.kt:277:class PipelineInitializationException(
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingForegroundService.kt:49:class RecordingForegroundService : Service() {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingNotificationHelper.kt:32:class RecordingNotificationHelper(private val context: Context) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingNotificationHelper.kt:46:    fun createNotificationChannels() {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingNotificationHelper.kt:77:    fun getNotification(state: RecordingServiceState): Notification {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingNotificationHelper.kt:163:    fun handleStateChangeToasts(oldState: RecordingServiceState, newState: RecordingServiceState) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingNotificationHelper.kt:201:    fun showToast(message: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingNotificationHelper.kt:214:    fun showErrorNotification(message: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingNotificationHelper.kt:230:    fun vibrate(effect: VibrationEffect) {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingServiceState.kt:16:sealed class RecordingServiceState {
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingServiceState.kt:22:    data class Starting(override val metadata: EnrichedCallData) : RecordingServiceState()
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingServiceState.kt:28:    data class Standby(override val metadata: EnrichedCallData? = null) : RecordingServiceState()
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingServiceState.kt:36:    data class Active(
app/src/main/java/com/kitsumed/shizucallrecorder/services/shell/ShellService.kt:53:class ShellService : IShellService.Stub {
app/src/main/java/com/kitsumed/shizucallrecorder/services/shell/ShellCommandExecutor.kt:42:    fun verifyServerJar(serverPath: String): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/services/shell/ShellCommandExecutor.kt:62:    fun launchScrcpyServer(
app/src/main/java/com/kitsumed/shizucallrecorder/services/shell/ShellAudioPipeline.kt:135:    fun start(
app/src/main/java/com/kitsumed/shizucallrecorder/services/shell/ShellAudioPipeline.kt:183:    fun stop() {
app/src/main/java/com/kitsumed/shizucallrecorder/system/SystemIntentHelpers.kt:38:class PersistentFolderPickerContract : ActivityResultContracts.OpenDocumentTree() {
app/src/main/java/com/kitsumed/shizucallrecorder/system/SystemIntentHelpers.kt:56:fun Context.takePersistableFolderPermission(uri: Uri) {
app/src/main/java/com/kitsumed/shizucallrecorder/system/SystemIntentHelpers.kt:67:fun Context.openAppSettings() {
app/src/main/java/com/kitsumed/shizucallrecorder/system/SystemIntentHelpers.kt:79:fun Context.openShizukuManager() {
app/src/main/java/com/kitsumed/shizucallrecorder/system/SystemIntentHelpers.kt:90:fun Context.openGithub() {
app/src/main/java/com/kitsumed/shizucallrecorder/system/SystemIntentHelpers.kt:95:fun Context.openGithubWiki() {
app/src/main/java/com/kitsumed/shizucallrecorder/system/SystemIntentHelpers.kt:100:fun Context.openGithubReportIssue() {
app/src/main/java/com/kitsumed/shizucallrecorder/system/SystemIntentHelpers.kt:104:fun Context.openGithubSponsor() {
app/src/main/java/com/kitsumed/shizucallrecorder/system/permissions/AppPermission.kt:21:sealed class AppPermission(
app/src/main/java/com/kitsumed/shizucallrecorder/system/permissions/AppPermission.kt:37:    class Runtime(
app/src/main/java/com/kitsumed/shizucallrecorder/system/permissions/AppPermission.kt:62:    class AppOp(
app/src/main/java/com/kitsumed/shizucallrecorder/system/permissions/PermissionChecks.kt:24:object PermissionChecks {
app/src/main/java/com/kitsumed/shizucallrecorder/system/permissions/PermissionChecks.kt:32:    fun hasNotificationPermission(context: Context): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/system/permissions/PermissionChecks.kt:53:    fun hasContactsPermission(context: Context): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/system/permissions/PermissionChecks.kt:66:    fun hasBatteryExemption(context: Context): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/system/storage/SafHelper.kt:24:object SafHelper {
app/src/main/java/com/kitsumed/shizucallrecorder/system/storage/SafHelper.kt:34:    data class SafResult(
app/src/main/java/com/kitsumed/shizucallrecorder/system/storage/SafHelper.kt:49:    fun createAudioFile(context: Context, folderUri: Uri, fileName: String, mimeType: String): SafResult? {
app/src/main/java/com/kitsumed/shizucallrecorder/system/storage/SafHelper.kt:69:    fun isFolderValid(context: Context, folderUri: Uri?): Boolean {
app/src/main/java/com/kitsumed/shizucallrecorder/system/storage/SafHelper.kt:87:    fun getFolderDisplayNameOrNull(context: Context, folderUri: Uri?): String? {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/ContactSelectionDialog.kt:52:data class ContactEntry(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/ContactSelectionDialog.kt:70:fun ContactSelectionDialog(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/ContactSelectionDialog.kt:107:fun ContactSelectionContent(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/ContactSelectionDialog.kt:343:fun PreviewContactSelectionDialog() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/DropdownComponents.kt:27:data class OptionItem(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/DropdownComponents.kt:45:fun M3DropdownField(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/DropdownComponents.kt:114:fun PreviewM3DropdownField() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/FileNameFormatDialog.kt:59:fun FileNameFormatDialog(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/common/ToggleListItem.kt:36:fun ToggleListItem(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/DisclaimerScreen.kt:69:fun DisclaimerScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/DisclaimerScreen.kt:207:fun HyperlinkText(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/PermissionsScreen.kt:61:fun PermissionsScreen(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/PermissionsScreen.kt:161:fun PermissionsContent(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/PlaybackScreen.kt:95:fun PlaybackScreen(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/RecordingsScreen.kt:134:fun RecordingsScreen(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/SettingsScreen.kt:91:fun SettingsScreen(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/SettingsScreen.kt:155:fun SettingsContent(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/SettingsScreen.kt:1063:fun WarningCard(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/SponsorScreen.kt:68:fun SponsorScreen(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/screens/SponsorScreen.kt:385:fun BioCard() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/theme/Theme.kt:53:fun ShizucallrecorderTheme(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/AppNavigationViewModel.kt:27:class AppNavigationViewModel(application: Application) : AndroidViewModel(application) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/AppNavigationViewModel.kt:70:    fun refresh() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/ContactPickerViewModel.kt:28:enum class ContactPickerType {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/ContactPickerViewModel.kt:43:data class ContactPickerState(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/ContactPickerViewModel.kt:56:class ContactPickerViewModel(application: Application) : AndroidViewModel(application) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/ContactPickerViewModel.kt:89:    fun openContactPicker(type: ContactPickerType) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/ContactPickerViewModel.kt:108:    fun confirmContactPicker(numbers: Set<String>) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/ContactPickerViewModel.kt:119:    fun dismissContactPicker() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PermissionsViewModel.kt:37:class PermissionsViewModel(application: Application) : AndroidViewModel(application) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PermissionsViewModel.kt:68:    fun onGrantAccess(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PermissionsViewModel.kt:123:    fun onCallDetectionModeChanged(newMode: CallDetectionMode) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PlaybackViewModel.kt:26:class PlaybackViewModel(application: Application) : AndroidViewModel(application) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PlaybackViewModel.kt:62:    fun load(uri: Uri) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PlaybackViewModel.kt:73:    fun togglePlayPause() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PlaybackViewModel.kt:77:    fun seekForward() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PlaybackViewModel.kt:82:    fun seekBack() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PlaybackViewModel.kt:86:    fun seekTo(ms: Long) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PlaybackViewModel.kt:92:    fun updateNote(text: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PlaybackViewModel.kt:99:    fun resetOnLeave() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:35:data class RecordingItem(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:51:enum class RecordingsSortField { TIME, NAME, SIZE }
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:52:enum class RecordingsSortOrder { ASC, DESC }
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:53:enum class RecordingsFilterTab { ALL, FAVOURITES }
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:55:data class RecordingsSortConfig(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:60:data class RecordingsUiState(
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:71:class RecordingsViewModel(application: Application) : AndroidViewModel(application) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:84:    fun loadRecordings() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:151:    fun refresh() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:155:    fun setSearchQuery(query: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:160:    fun clearSearch() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:164:    fun setFilterTab(tab: RecordingsFilterTab) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:169:    fun setSortConfig(config: RecordingsSortConfig) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:174:    fun toggleSelection(uri: Uri) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:182:    fun selectAllVisible() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:188:    fun clearSelection() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:192:    fun toggleFavourite(item: RecordingItem) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:203:    fun deleteRecording(item: RecordingItem) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:215:    fun deleteSelected() {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:232:    fun getNote(uri: Uri): String {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/RecordingsViewModel.kt:236:    fun saveNote(uri: Uri, note: String) {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:37:enum class DebugAction {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:50:interface SettingsActions {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:51:    fun setAutoRecordIncoming(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:52:    fun setAutoRecordOutgoing(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:53:    fun setVibrationEnabled(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:54:    fun setIgnoreAnonymousIncoming(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:55:    fun setIgnoreCrossCountryIncoming(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:56:    fun setIgnoreCrossCountryOutgoing(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:57:    fun setIgnoreContactsModeIncoming(modeEnum: AppPreferences.IgnoreContactsMode)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:58:    fun setIgnoreContactsModeOutgoing(modeEnum: AppPreferences.IgnoreContactsMode)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:59:    fun setAudioSource(source: String)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:60:    fun setAudioCodec(codec: String)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:61:    fun setAudioBitRate(bitRate: Int)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:62:    fun setThemeMode(mode: AppPreferences.ThemeMode)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:63:    fun setDynamicColorEnabled(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:64:    fun setShowToastsEnabled(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:65:    fun setAppLanguage(languageCode: String)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:66:    fun setLoggingEnabled(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:67:    fun setDebugEnabled(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:68:    fun setDebugCallerNumber(number: String)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:69:    fun triggerDebugAction(action: DebugAction)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:70:    fun exportLogs(uri: android.net.Uri)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:71:    fun getAppVersion(): String
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:72:    fun setShizukuAutoManageEnabled(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:73:    fun setShizukuStartOnRecordEnabled(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:74:    fun setShizukuKeepAliveEnabled(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:75:    fun setShizukuAuthKey(key: String)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:76:    fun setFileNameTemplate(template: String)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:77:    fun setCallDetectionMode(mode: CallDetectionMode)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:78:    fun setRecordThirdPartyCalls(enabled: Boolean)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:86:class SettingsViewModel(application: Application) : AndroidViewModel(application), SettingsActions {
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/SettingsViewModel.kt:150:    fun refresh() {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:43:object AppLogger {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:127:    fun init(context: Context) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:189:    fun initAsRemote(callback: ILogCallback, isRedactionEnabled: Boolean = true) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:202:    fun clearLogs() {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:220:    fun exportReport(context: Context, destinationUri: Uri) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:257:    fun v(tag: String, message: String, t: Throwable? = null) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:262:    fun d(tag: String, message: String, t: Throwable? = null) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:267:    fun i(tag: String, message: String, t: Throwable? = null) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:272:    fun w(tag: String, message: String, t: Throwable? = null) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:277:    fun e(tag: String, message: String, t: Throwable? = null) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/AppLogger.kt:282:    fun wtf(tag: String, message: String, t: Throwable? = null) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/PhoneNumberManager.kt:25:class PhoneNumberManager private constructor(context: Context) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/PhoneNumberManager.kt:55:        fun getInstance(context: Context): PhoneNumberManager {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/PhoneNumberManager.kt:68:        fun normalisePhoneNumber(phoneNumber: String): String {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/PhoneNumberManager.kt:89:    fun getDeviceCountryIso(): String {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/RecordingFileNameFormatter.kt:24:object RecordingFileNameFormatter {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/RecordingFileNameFormatter.kt:47:    fun stripOwnershipPrefix(name: String): String =
app/src/main/java/com/kitsumed/shizucallrecorder/utils/RecordingFileNameFormatter.kt:54:    fun isOwnedRecording(name: String): Boolean = name.startsWith(OWNED_FILE_PREFIX)
app/src/main/java/com/kitsumed/shizucallrecorder/utils/RecordingFileNameFormatter.kt:66:    fun isLegacyRecording(name: String): Boolean = LEGACY_NAME_REGEX.containsMatchIn(name)
app/src/main/java/com/kitsumed/shizucallrecorder/utils/RecordingFileNameFormatter.kt:79:    enum class FileNamePlaceholder(val tag: String, @param:StringRes val descriptionResId: Int, val supportedModes: Set<CallDetectionMode>)  {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/RecordingFileNameFormatter.kt:104:    fun formatFileName(
app/src/main/java/com/kitsumed/shizucallrecorder/utils/SponsorNotificationHelper.kt:23:object SponsorNotificationHelper {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/SponsorNotificationHelper.kt:27:    fun showSupportReminderNotification(context: Context) {
app/src/main/java/com/kitsumed/shizucallrecorder/utils/CallLogHelper.kt:16:object CallLogHelper {

## Android strings


## TODO/FIXME

app/src/main/java/com/kitsumed/shizucallrecorder/integrations/scrcpy/ScrcpyAudioMuxer.kt:149:        // TODO: This code was added to fix most phone audio player behaving badly with the large silence, ideally we would like to keep the silence and not corrupt the file by doing so.
app/src/main/java/com/kitsumed/shizucallrecorder/integrations/shizuku/ShizukuConnectionManager.kt:13:* TODO: Mayne remove the permissionListener logic, I don't think there's real scenario where any user would ever need this since the app onboarding require permission granting to proceed. Well it's still a safety check I guess?
app/src/main/java/com/kitsumed/shizucallrecorder/services/recording/RecordingForegroundService.kt:350:        // TODO: Remove this fallback logic once we have a more reliable way to get phone number (using Shizuku and hidden api)
app/src/main/java/com/kitsumed/shizucallrecorder/ui/viewmodels/PermissionsViewModel.kt:107:                    //TODO: Add error message when appOpsGranted is false, so the user knows something went wrong
