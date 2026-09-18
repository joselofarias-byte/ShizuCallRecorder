# ShizuCallRecorder Technical Audit

**Repo:** https://github.com/joselofarias-byte/ShizuCallRecorder  
**Audit branch:** `cursor/shizucallrecorder-audit-2d0c`  
**Base revision:** `e6574cc` (`main`, “Restore caller name in secure notifications (#11)”)  
**Primary target device:** HONOR 200 (ELI-NX9), Android 16 / MagicOS, no root, Shizuku available, ADB/Termux available  
**Auditor environment:** cloud Linux VM with JDK 21, Android SDK 36. No HONOR hardware, no emulator telephony, no Shizuku.  
**Date:** 2026-09-18

Claim labels used throughout:

| Label | Meaning |
|---|---|
| **Confirmed possible** | Demonstrated from this repo, AOSP/Shizuku/scrcpy source, or a successful local experiment |
| **Confirmed impossible under current privileges** | Contradicted by AOSP/API contracts for a normal third-party app UID, or by missing privileges this app does not have |
| **Plausible, requires device testing** | Compatible with source/docs but depends on HONOR/MagicOS HAL, SELinux, or firmware |
| **Unknown** | Not determined from source, docs, or this environment |

This document does **not** treat call recording as a solved problem on HONOR 200. The software path exists; whether both call legs are audible is a device experiment.

---

## 1. Repository inspection

### Identity

- Fork of `kitsumed/ShizuCallRecorder`.
- Application ID: `com.joselofarias.shizucallrecorder` (`app/build.gradle.kts`).
- Java/Kotlin namespace remains `com.kitsumed.shizucallrecorder`.
- `versionName = "1.3.1"`, `versionCode = 17`, `minSdk = 30`, `compileSdk/targetSdk = 36`.
- Git history shows a later “Port seguro de fixes upstream 1.3.3” (`bc24795`) landed on this 1.3.1 version line. **Confirmed:** version metadata and upstream port are out of sync.

### Git / GitHub

- Default branch: `main`.
- Issues are **disabled** on this fork (`gh issue list` returned that the repository has disabled issues).
- PRs observed: branding, upstream 1.3.1/1.3.3 ports, TalkBack, CI draft builds, caller-name notification fix (#11, merged).
- Releases were **not** published by this audit (hard constraint).
- CI: `.github/workflows/pr-android-build.yml` (debug assemble on PRs to `main`), `.github/workflows/build-app.yml` (manual, can create **draft** releases), `.github/workflows/codeql.yml`.

### Tests before this audit

**Confirmed:** the tree had **no** `src/test` or `src/androidTest` sources and no test dependencies. Static analysis in-repo is CodeQL plus whatever `lint` AGP runs.

### Locales

`resourceConfigurations += listOf("en", "es")` strips other translation XMLs from the APK. Those XML files still exist in git. **Confirmed** packaging choice of this fork, not a runtime crash.

---

## 2. Architecture

```
App UID process (com.joselofarias.shizucallrecorder)
├── UI (Compose): onboarding, settings, recordings library, playback
├── CallDetectionOrchestrator  → enables exactly one detection component
│     ├── PhoneStateReceiver + PhoneStateSessionManager
│     └── InCallService (Telecom InCallService, non-UI)
├── RecordingDecisionEngine    → auto-record vs standby vs ignore
├── RecordingForegroundService → FGS specialUse (API 34+) / dataSync (API 30–33)
│     ├── ShizukuConnectionManager.bindUserService(ShellService)
│     └── AudioRecordingEngine
│           ├── SAF create file (app UID)
│           ├── ScrcpyClient parse pipe
│           └── ScrcpyAudioMuxer write OGG/M4A
└── ShizukuProvider

Shizuku server (UID 2000 shell, or 0 if rooted — not the HONOR target)
└── ShellService (UserService, process suffix ElevatedShellService)
      ├── ShellCommandExecutor: appops set / cmd role add-role-holder
      └── ShellAudioPipeline
            ├── LocalServerSocket scrcpy_<scid>
            ├── app_process CLASSPATH=<shared scrcpy-server.jar> com.genymobile.scrcpy.Server
            └── relay socket bytes → ParcelFileDescriptor pipe → app
```

**Confirmed possible:** this split is implemented as described. Capture is **not** `AudioRecord` in the app process. The app never declares `RECORD_AUDIO`.

**Confirmed impossible under current privileges:** a normal app UID using `MediaRecorder.AudioSource.VOICE_CALL` / `VOICE_UPLINK` / `VOICE_DOWNLINK`. Those sources require `CAPTURE_AUDIO_OUTPUT` (`signature|privileged`), which third-party apps cannot hold. Citations: [MediaRecorder.AudioSource docs / AOSP change 851a879](https://android.googlesource.com/platform/frameworks/base/+/851a879%5E%21/), [Sharing audio input](https://developer.android.com/media/platform/sharing-audio-input).

---

## 3. Recording pipeline (call → file)

### 3.1 Call detection

Two mutually exclusive modes (`CallDetectionMode`, toggled via `PackageManager.setComponentEnabledSetting`):

| Mode | Component | When it fires the pipeline | Privileges |
|---|---|---|---|
| `PhoneState` | exported `PHONE_STATE` receiver | RINGING now enters standby; OFFHOOK starts/standbys recording | App UID + `READ_PHONE_STATE` + `READ_CALL_LOG` |
| `InCallService` (default on API 31+) | Telecom `InCallService` | RINGING/DIALING standby; ACTIVE starts/standbys recording | App UID + AppOp `android:manage_ongoing_calls` granted **through Shizuku** |

`InCallService` is declared with:

- `android.telecom.IN_CALL_SERVICE_UI=false`
- `android.telecom.INCLUDE_SELF_MANAGED_CALLS=true`
- permission `BIND_INCALL_SERVICE` / `MANAGE_ONGOING_CALLS`

Escalation chain for `manage_ongoing_calls` (`CallDetectionMode.kt`):

1. `appops set --user <id> <pkg> android:manage_ongoing_calls allow`
2. same at `--uid`
3. `cmd role add-role-holder android.app.role.COMPANION_DEVICE_GLASSES` (API 34+)
4. `cmd role add-role-holder android.app.role.COMPANION_DEVICE_WATCH` (API 31+)

**Plausible, requires device testing:** whether MagicOS allows shell to keep those AppOps/roles. Upstream docs already report Chinese OEMs reverting AppOps ([kitsumed#41](https://github.com/kitsumed/ShizuCallRecorder/issues/41), cited in-code).

Phone number on `PHONE_STATE`: first broadcast redacts the number; second includes it if `READ_CALL_LOG` is granted. Code still reads deprecated `TelephonyManager.EXTRA_INCOMING_NUMBER`. Comments cite AOSP `TelephonyRegistry.java` still sending it on Android 16. **Plausible, requires device testing** on HONOR.

### 3.2 Decision → FGS

`RecordingDecisionEngine.executeDecisionPipeline`:

1. Enrich number (libphonenumber E.164, contacts name, cross-country).
2. Apply auto-record / ignore rules.
3. `startForegroundService` with `ACTION_START_RECORDING` or `ACTION_STANDBY`.

FGS type: `specialUse` on API 34+ with `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` explaining Shizuku call recording; `dataSync` on API 30–33.

Onboarding **requires** battery exemption. Official Android 12+ FGS background-start exemptions include “the user turns off battery optimizations for your app” ([FGS background start restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start), last updated 2026-09-16). **Confirmed possible** *if* that exemption is granted. The current public exemption list does **not** list `PHONE_STATE` itself.

Android 15 `dataSync` 6-hour/24h timeout does not apply to the API 34+ `specialUse` path this app uses on Android 16. **Confirmed** from [Android 15 behavior changes](https://developer.android.com/about/versions/15/behavior-changes-15) plus this app’s API 34 branch in `startForegroundWithType`.

Android 15 overlay exemption narrowing: `SYSTEM_ALERT_WINDOW` only counts if an overlay is actually visible. Overlay is optional here; battery exemption is the intended FGS start path.

### 3.3 Capture

`AudioRecordingEngine.startPipeline`:

1. Validate SAF tree.
2. Extract/verify `scrcpy-server` v4.0 JAR into `getExternalFilesDir()` so UID 2000 can read it (`setReadable(true, false)`).
3. AIDL `IShellService.startRecording(...)` → `ShellAudioPipeline.startCapture`.
4. Shell launches:
   `app_process / com.genymobile.scrcpy.Server 4.0 video=false audio=true ... audio_source=<key> audio_codec=<opus|aac> send_stream_meta=true`
5. App parses scrcpy v4 audio stream (4-byte FourCC + 12-byte frame headers) and muxes to SAF.

Default audio source: `voice-call` (`ScrcpyAudioSource.VOICE_CALL`). That is scrcpy’s mapping onto `MediaRecorder.AudioSource.VOICE_CALL`, which needs `CAPTURE_AUDIO_OUTPUT` **in the capturing process**.

The capturing process is scrcpy-server under **shell UID 2000**, not the app.

AOSP Android 16 `frameworks/base/packages/Shell/AndroidManifest.xml` (tag `android-16.0.0_r4`) **does** declare for `com.android.shell`:

- `android.permission.CAPTURE_AUDIO_OUTPUT`
- `android.permission.RECORD_AUDIO`
- `android.permission.CALL_AUDIO_INTERCEPTION`
- `android.permission.CAPTURE_VOICE_COMMUNICATION_OUTPUT`
- `android.permission.MODIFY_AUDIO_ROUTING`
- `android.permission.MANAGE_AUDIO_POLICY`

**Confirmed possible on AOSP Android 16:** shell UID is *declared* those permissions. Shizuku UserService runs as that UID when started via ADB ([Rikka: UserService runs as shell, ADB already has these permissions](https://github.com/RikkaApps/Shizuku-API/issues/71)).

**Plausible, requires device testing:** HONOR MagicOS may:

- trim shell permissions,
- leave `VOICE_CALL` HAL path silent / one-sided,
- block `app_process` + abstract `LocalServerSocket`,
- treat IMS/VoLTE/Wi-Fi calling as a different mix that `VOICE_CALL` does not include.

The app itself checks `Shizuku.checkRemotePermission(CAPTURE_AUDIO_OUTPUT)` during onboarding and **exits the process** if missing (`PermissionsScreen.kt`). That check is the right gate; a pass still does not prove the HAL returns call audio.

Accessibility is **not** used. **Confirmed.**

### 3.4 Storage

SAF tree chosen at onboarding (`SafHelper.createAudioFile`). Files are **not** written via MediaStore insert. World-readable is only used for the scrcpy JAR in app external files, not for recordings.

Honor integration path documented upstream: write into `/storage/emulated/0/Sounds/CallRecord/` with a specific name pattern. That is a **filename convention for the stock dialer to *display* files**, not proof that Honor’s native recorder can be enabled. See §8.

---

## 4. Privilege model (who runs what)

| Operation | Process / UID | Mechanism |
|---|---|---|
| UI, SAF I/O, muxing, notifications, contacts, call-log reads | App UID | Normal APIs |
| `PHONE_STATE` / `InCallService` callbacks | App UID | Manifest components |
| Start/stop Shizuku | App UID | Broadcast `moe.shizuku.privileged.api.START/STOP` + user auth key |
| Bind `ShellService` | App UID → Shizuku | `Shizuku.bindUserService` |
| `appops set`, `cmd role add-role-holder` | Shell UID 2000 | `ProcessBuilder` in `ShellCommandExecutor` |
| Launch scrcpy-server, open `VOICE_CALL` capture | Shell UID 2000 | `app_process` + scrcpy `AudioRecord`/`AudioSource` |
| Read `/data/data/<other pkg>` | — | **Confirmed impossible** for shell (Shizuku docs) |
| Grant `CAPTURE_AUDIO_OUTPUT` to the **app** UID | — | **Confirmed impossible** (signature\|privileged) |
| Native Honor recorder enablement | — | **Unknown** on this firmware; not implemented |

Root / Magisk / KernelSU / APatch / Stellar / Nightzuku: **not used**. HONOR target is explicitly non-root. If someone started Shizuku as root, `ShellService` would log UID 0; that path is out of scope.

---

## 5. Android 15/16 restrictions (relevant subset)

| Area | Finding | Label |
|---|---|---|
| Call state (`PHONE_STATE`) | Still delivered; number extra still present in AOSP 16 `TelephonyRegistry` per in-code citation. HONOR may still redact. | Plausible / device |
| Telecom `InCallService` non-UI | Supported; needs `MANAGE_ONGOING_CALLS` / AppOp. OEM InCallController may refuse unofficial dialer companions. | Plausible / device |
| `AudioRecord` in app UID of call audio | Blocked without `CAPTURE_AUDIO_OUTPUT`. App does not try this. | Confirmed impossible (app UID) |
| `VOICE_CALL` from **shell** | Permission declared on AOSP shell. HAL/OEM may still return silence. | Confirmed possible (permission); audio content is device-only |
| FGS types | App uses `specialUse` on 16. Avoids Android 15 `dataSync` 6h cap. Play review of `specialUse` is N/A for sideload. | Confirmed possible |
| FGS background start | Relies on battery exemption (onboarding). Overlay exemption is stricter on 15+ and overlay is optional. | Confirmed possible if exemption granted |
| While-in-use mic FGS | App does **not** use `microphone` FGS type, so the Android 14 mic-from-background `SecurityException` does not apply to this service type. Capture is in the shell process. | Confirmed |
| Storage | SAF; no `MANAGE_EXTERNAL_STORAGE`. Honor `Sounds/CallRecord` is reachable if the user picks that tree. | Confirmed possible |
| Accessibility | Unused. | Confirmed |
| Notifications | `POST_NOTIFICATIONS` required; FGS notification is high-importance. Android 14+ dismiss is re-posted. | Confirmed |

Android 16 behavior-changes page ([developer.android.com/about/versions/16/behavior-changes-16](https://developer.android.com/about/versions/16/behavior-changes-16)) did not add a new public third-party call-recording API. JobScheduler quotas for work started from an FGS are irrelevant to this pipeline.

---

## 6. Shizuku capabilities vs this app

**Confirmed possible with Shizuku/shell (AOSP):**

- Run Java (`UserService`) as UID 2000.
- Inherit shell permissions including `CAPTURE_AUDIO_OUTPUT` (AOSP 16 Shell manifest).
- Execute `appops` / `cmd role` (used).
- Query remote permission via `Shizuku.checkRemotePermission` (used).
- Start/stop the Shizuku server via documented broadcasts if the user stored the auth key.

**Confirmed limitations:**

- Not root. Cannot rewrite sepolicy, cannot become `system_server`, cannot read other apps’ private data.
- UserService is not a full Android app process: `Context.getContentResolver()` / some receivers are unreliable (Rikka, same issue #71). This app therefore does **not** try to `AudioRecord` inside `ShellService` with an app `Context`; it execs scrcpy-server instead. That is the correct shape.
- Binder `transactRemote` to hidden APIs is possible in principle but unused here. Using it for Telecom internals would be OEM-fragile. **Not implemented; not claimed.**

**Plausible, requires device testing:** `dumpsys media.audio_flinger` / `dumpsys audio` as shell, listing mix ports named `IN_TELEPHONY_RX` / `VOICE_CALL`. Useful diagnosis, not a second recorder.

**Unknown / not to assume:** whether MagicOS grants shell `CAPTURE_AUDIO_OUTPUT` at runtime (declaration ≠ grant on some OEM permission models). The onboarding remote-permission check is the first experiment.

---

## 7. HONOR / MagicOS hypotheses (do not treat as fact)

Native package **exists on some Honor firmware variants**: `com.hihonor.phone.recorder` (“Call Recorder”, APKMirror listings 15.0.0.004–15.1.0.100). Community reports:

- Present/enabled on **CN** MagicOS dialer.
- Often **absent or inert** on global ROM; users sideload a helper APK and/or `settings put secure enable_record_auto_key 1`.
- MagicOS 9/10 reports of helper APKs failing with “package appears to be invalid”.
- Storage path commonly `/storage/emulated/0/Sounds/CallRecord` (sometimes `Sound/CallRecorder`).

**This audit does not enable, patch, or reuse that package.** Sideloading a privileged OEM APK, disabling signature checks, or pushing it as system is out of scope and may be impossible without root / unlock.

Hypotheses for ELI-NX9 (all **device-only**):

1. `pm path com.hihonor.phone.recorder` is empty on global firmware.
2. Package exists but is disabled / missing `RECORD_AUDIO` / not the default dialer recorder.
3. Stock dialer will **list** third-party files in `Sounds/CallRecord` if the name matches the templates in `docs/troubleshooting.md`. That is display integration, not capture.
4. VoLTE/Wi-Fi calling produces silence on `voice-call` even if GSM/CS fallback works.
5. Aggressive MagicOS battery policy kills FGS despite exemption (dontkillmyapp-class behavior).
6. `manage_ongoing_calls` AppOp is reset after reboot (Chinese-OEM class issue).

---

## 8. Confirmed software bugs (this tree) and fixes on this branch

| ID | Bug | Fix |
|---|---|---|
| B1 | `PermissionsViewModel.onGrantAccess` called `missingPermissions.first()` after globals were satisfied. The Permissions button label becomes “Continue” when `status.isComplete()`, so a tap could throw `NoSuchElementException`. | Return after `isComplete()` / `firstOrNull()`. |
| B2 | `ShizukuConnectionManager.waitForServer()` returns `Boolean`, but `RecordingForegroundService` ignored it and bound anyway. Timeout then surfaced as “Shizuku is not running”. | Require `serverReady` before bind. |
| B3 | `ShellCommandExecutor` called `waitFor()` with **no timeout** and read stdout/stderr **after** wait (pipe-full deadlock risk). Violates project shell rules. | Concurrent drain + 15s `waitFor(timeout)`. |
| B4 | Documented “start Shizuku when the call starts” never ran on RINGING; pipeline only ran on OFFHOOK/ACTIVE. Extra seconds of lost audio on slow Shizuku start. | `enterStandby()` on RINGING (PhoneState) and RINGING/DIALING (InCallService), without locking the ACTIVE start. |
| B5 | Every file was prefixed `shizucall_`, so Honor CallRecord templates in `docs/troubleshooting.md` could not match, and disabling the prefix would also hide those files from the in-app library. | Settings toggle; `composeRecordingFileName`; library lists all audio files when prefix is off. |
| B6 | No automated tests existed. | JUnit tests for number normalisation, filename ownership, call direction, scrcpy enums. |
| B7 | `lintDebug` failed with 5 `NonObservableLocale` errors in Playback/Recordings composables (`Locale.getDefault()`). | Use `LocalLocale.current.platformLocale`. |

Non-bugs recorded so they are not “fixed” speculatively:

- `send_stream_meta` (not `send_codec_meta`) is the scrcpy **4.0** option. Comment text was stale; the argument is correct.
- `Process.myUserHandle().hashCode()` is the numeric user id. Correct.
- Dual-call / call-waiting collapsing into one file is an acknowledged PhoneState limitation, not a regression.
- Version 1.3.1 vs upstream 1.3.3 ports is metadata drift, not a functional defect. Not changed (would imply a release).

---

## 9. Experiments performed in this environment

| Experiment | Result |
|---|---|
| Full source / manifest / Gradle / AIDL read | Completed. Architecture as §2–4. |
| Decode AOSP 16 Shell `AndroidManifest.xml` | `CAPTURE_AUDIO_OUTPUT` and related audio permissions **declared**. |
| Install Android SDK 36 + Gradle assemble | See §10 (filled after the build). |
| `./gradlew :app:testDebugUnitTest` | See §10. |
| `./gradlew :app:lintDebug` | See §10. |
| Real call on HONOR 200 | **Not possible here.** Commands for the human: §11. |
| `adb shell` against MagicOS | **Not possible here.** |
| Enabling `com.hihonor.phone.recorder` | **Not attempted.** Must not be assumed to work. |

---

## 10. Build / test / lint

Environment: OpenJDK 21.0.10, AGP 9.2.1, Gradle 9.5.0, Android SDK 36, `SKIP_SIGNING=true`.

Command:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --stacktrace
```

| Task | Result |
|---|---|
| `:app:assembleDebug` | **SUCCESS.** APK `app/build/outputs/apk/debug/app-debug.apk`, 71 MB, SHA-256 `6fcae6f6c76e8b80b4c7a8f6bcd92cb9905e99b8c6cae6c63c8ca08e2803cffc` |
| `:app:testDebugUnitTest` | **SUCCESS.** 14 tests, 0 failures: `PhoneNumberManagerTest` (4), `RecordingFileNameFormatterTest` (4), `CallDirectionTest` (4), `ScrcpyEnumsTest` (2) |
| `:app:lintDebug` | **SUCCESS after B7.** Final report: **0 errors, 144 warnings** (pre-existing: `InlinedApi` POST_NOTIFICATIONS, `OldTargetApi` noise vs SDK 36, deprecated Compose icons, `UseKtx`, etc.). First lint run on this branch failed with 5 `NonObservableLocale` errors; those were fixed. |
| GitHub Actions on `ba8f082` (PR #12) | **SUCCESS.** `Assemble Debug APK` (4m24s), `Analyze Code (java-kotlin)` / CodeQL (4m38s), CodeQL check (pass). |
| Instrumentation / device tests | **None in repo.** Cannot run on HONOR from this VM. |
| Runtime recording | **Not executed.** No Android device/emulator with telephony. |

Kotlin compile warnings remaining: deprecated `EXTRA_INCOMING_NUMBER`, overlay lock-screen flags, AutoMirrored icons, `LocalLifecycleOwner` import path. Not changed (outside the recording-reliability scope except the incoming-number extra, which is still required and still present in AOSP 16).

---

## 11. Device experiments for the human (HONOR 200 ELI-NX9)

Run these **exactly**. Redact phone numbers if you paste logs back. Do not factory-reset, unlock the bootloader, or install random “Call Recorder” APKs unless you independently choose to.

### 11.1 Baseline (ADB from a PC, or Termux + `adb`)

```bash
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell getprop ro.build.display.id
adb shell getprop ro.vendor.build.fingerprint
adb shell getprop ro.product.brand
adb shell getprop ro.miui.ui.version.name
adb shell getprop ro.build.version.magic
adb shell getprop ro.config.hw_voicerecord
```

**Expected:** model contains `ELI-NX9` or Honor 200; SDK `36`; MagicOS keys may or may not exist (`ro.build.version.magic` can be empty). `hw_voicerecord` **unknown**.

### 11.2 Native Honor recorder package (observe only)

```bash
adb shell pm path com.hihonor.phone.recorder
adb shell pm path com.android.incallui
adb shell pm path com.hihonor.contacts
adb shell cmd package list packages | grep -iE 'record|incall|dialer|telecom|hihonor.phone'
adb shell dumpsys package com.hihonor.phone.recorder | head -n 80
adb shell cmd appops get com.hihonor.phone.recorder
```

**Expected observations:**

- If `pm path` prints `package:/…apk`: package **present**. That does **not** mean it is enabled or allowed to capture.
- If `Failure` / empty: package **absent** on this firmware. **Do not sideload** as part of this experiment unless you explicitly want to test that separate, high-risk path.
- Look for `enabled=true/false`, `RECORD_AUDIO`, `CAPTURE_AUDIO_OUTPUT`.

```bash
ls -la /storage/emulated/0/Sounds/ 2>/dev/null
ls -la /storage/emulated/0/Sounds/CallRecord/ 2>/dev/null
ls -la /storage/emulated/0/Sound/CallRecorder/ 2>/dev/null
```

### 11.3 Shell / Shizuku privileges

With Shizuku **running** (or `adb shell` which is the same UID 2000):

```bash
adb shell id
adb shell dumpsys package com.android.shell | grep -E 'CAPTURE_AUDIO_OUTPUT|RECORD_AUDIO|CALL_AUDIO_INTERCEPTION|CAPTURE_VOICE_COMMUNICATION'
```

From Termux with Shizuku `rish` (if installed):

```bash
rish -c id
rish -c 'cmd appops get com.android.shell CAPTURE_AUDIO_OUTPUT'
```

**Expected on AOSP:** `uid=2000(shell)`; `CAPTURE_AUDIO_OUTPUT` granted. **MagicOS unknown.**

### 11.4 This app’s AppOps / InCallService

Replace the package if you sideload a different ID (fork ID is `com.joselofarias.shizucallrecorder`):

```bash
PKG=com.joselofarias.shizucallrecorder
adb shell dumpsys package $PKG | grep -E 'versionName|enabled|MANAGE_ONGOING_CALLS|READ_PHONE_STATE|READ_CALL_LOG'
adb shell cmd appops get $PKG android:manage_ongoing_calls
adb shell cmd appops get $PKG
adb shell dumpsys activity services $PKG
adb shell dumpsys telecom | grep -A 20 -i incall
```

After granting InCallService mode in the app, re-run `appops get … manage_ongoing_calls`. **Expected if escalation worked:** `allow`. If it flips back after a reboot, MagicOS is reverting AppOps.

### 11.5 Audio path during a live GSM/IMS call

Start a real call, then:

```bash
adb shell dumpsys audio | head -n 120
adb logcat -d | grep -E 'SCR:|scrcpy|AudioFlinger|VOICE_CALL|IN_TELEPHONY'
adb shell dumpsys media.audio_flinger | grep -iE 'voice|telephony|record|in_call' | head
```

**What to look for:**

- `AudioMode: IN_CALL` or `IN_COMMUNICATION`.
- Input device `TELEPHONY` / `IN_TELEPHONY_RX`.
- App logs `scrcpy-server launched successfully` and later `Codec FourCC`.
- File length > 0 in the SAF folder.

If scrcpy starts but the file is silence: HAL is not mixing call RX/TX into `VOICE_CALL`. Then retry Settings → audio source `mic-voice-communication`, then debug sources `output` / `playback` (API 33+ `audio_dup=true`). **None of these is guaranteed.**

### 11.6 Honor filename display test (not capture)

1. In Settings, set folder to `Sounds/CallRecord` (create it if missing).
2. Disable **Prefix recordings with shizucall_**.
3. Template: `{phone_number}_{date:year}{date:month}{date:day}{date:hours}{date:minutes}{date:seconds}`
4. Codec: Opus (`.ogg`) first; if the dialer ignores OGG, retry AAC (`.m4a`).
5. Make a recorded call, then open the stock Phone app’s recording list.

**Success:** stock UI shows the file. **Failure:** capture may still have worked; only the OEM indexer rejected the name/container.

### 11.7 Negative tests

- Airplane mode / Wi-Fi calling only.
- Bluetooth headset / car Android Auto.
- WhatsApp (self-managed) with “record third-party calls” off vs on.
- Shizuku stopped mid-call (expect error notification `recording_error_shizuku_disconnected_unexpectedly`).

---

## 12. Remaining research

1. Live HONOR 200 results from §11 (blocks any “it records both sides” claim).
2. Whether MagicOS `InCallController` binds unofficial `IN_CALL_SERVICE_TYPE_NON_UI` services.
3. Whether `CALL_AUDIO_INTERCEPTION` (declared on AOSP shell) is a usable alternate capture API on MagicOS — **not implemented**, do not assume.
4. IMS vs CS call mix; Honor “VoLTE HD” / Wi-Fi calling.
5. Whether selecting `Sounds/CallRecord` via SAF survives reboot on MagicOS.
6. Upstream kitsumed discussions for Honor 200 specifically (this fork has issues disabled).

---

## 13. What is blocked on physical device / credentials / approval

- Any runtime proof of two-way audio on ELI-NX9.
- Any claim that Honor’s native recorder can be turned on.
- Signed release builds (CI signing secrets exist in GitHub; this agent must not publish releases).
- Merging this PR (hard constraint).

---

## 14. Next commands for the human (short list)

1. Review this PR; **do not merge** until you have run §11 on the phone if you care about HONOR results.
2. Install the PR debug APK (CI artifact `ShizuCallRecorder-pr-debug`) or `assembleDebug` locally.
3. Paste back the outputs of §11.1, §11.2 `pm path`, §11.3 `CAPTURE_AUDIO_OUTPUT`, and one call’s `SCR:` logcat plus file size.
4. Only after that, decide whether to chase HAL silence (audio source matrix) vs InCallService AppOp revert vs OEM recorder display.

---

## Appendix A. Manifest permissions (app UID)

From `app/src/main/AndroidManifest.xml`:

- `moe.shizuku.manager.permission.API_V23`
- `POST_NOTIFICATIONS`
- `READ_PHONE_STATE`
- `READ_CONTACTS`
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
- `VIBRATE`
- `QUERY_ALL_PACKAGES` (file-name `{package_name}`)
- `SYSTEM_ALERT_WINDOW` (optional overlay)
- `READ_CALL_LOG`
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `FOREGROUND_SERVICE_DATA_SYNC`
- `MANAGE_ONGOING_CALLS` (not grantable by the user; AppOps/role via Shizuku)

Not present: `RECORD_AUDIO`, `CAPTURE_AUDIO_OUTPUT`, `WRITE_EXTERNAL_STORAGE`, accessibility, notification listener.

## Appendix B. Key file map

| Path | Role |
|---|---|
| `services/callDetection/phoneState/*` | `PHONE_STATE` session + 500ms number window |
| `services/callDetection/incall/InCallService.kt` | Telecom callbacks |
| `services/RecordingDecisionEngine.kt` | Auto-record policy + FGS intents |
| `services/recording/RecordingForegroundService.kt` | FGS lifecycle, Shizuku bind |
| `services/recording/AudioRecordingEngine.kt` | SAF + scrcpy client |
| `services/shell/ShellService.kt` | Shizuku UserService |
| `services/shell/ShellAudioPipeline.kt` | scrcpy process + pipe |
| `integrations/shizuku/ShizukuConnectionManager.kt` | Bind/start/stop/permission |
| `integrations/scrcpy/*` | Protocol, muxer, JAR extract |
| `system/permissions/AppPermission.kt` | Escalation chain |
