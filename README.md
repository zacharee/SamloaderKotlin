# Notices
## Old Firmware
Samsung may not serve the firmware you request. Early in 2023, Samsung made it so it's only possible to download the latest firmware in most cases, no matter which you request.

If you know a workaround, please follow up on [this issue](https://github.com/zacharee/SamloaderKotlin/issues/10).

## IMEI/Serial Number Requirement
Samsung now requires a matching IMEI or serial number be sent with firmware requests. If you're downloading firmware for a device on-hand, enter the IMEI or serial from that device.

Otherwise, you can likely find an IMEI by searching "[MODEL] IMEI" in Google. The CSC doesn't seem to matter, only the model and variant.

The U/U1 variants (US/US Unlocked) are interchangeable. An IMEI for the SM-S918U will work to download firmware for the SM-S918U1, for example.

If you know a model and TAC you want added to the database, please [open an issue](https://github.com/zacharee/SamloaderKotlin/issues/new?assignees=&labels=&projects=&template=imei-database-request.md&title=).

Thank you [VanVuong41429](https://github.com/VanVuong41429) for contributing so many TACs!

# Bifrost - Samsung Firmware Downloader
This is yet another firmware downloader for Samsung devices, but it has some special features.

For one, it's cross-platform. Bifrost runs on Windows, Linux, macOS, and even Android! 

Bifrost is also a graphical program, with a shared UI across all supported platforms.

Most of the functionality in Bifrost is based on [Samloader](https://github.com/nlscc/samloader). The Python code has been converted to Kotlin and tweaked to take advantage of some of Kotlin's features.

Bifrost uses Jetpack Compose, JetBrains Compose for Desktop, and Kotlin Multiplatform to create a shared codebase for all supported platforms.

# Download
Check out the [Releases](https://github.com/zacharee/SamloaderKotlin/releases) page for the downloads.

## Platform Compatibility

|               | x86 | x86_64 | ARMv7 | ARM64 |
|---------------|-----|--------|-------|-------|
| Windows       | ❌   | ✅      | ❌     | ✅     |
| macOS         | ❌   | ✅      | ❌     | ✅     |
| Android       | ✅   | ✅      | ✅     | ✅     |
| Debian-Based  | ❌   | ✅      | ❌     | ✅     |
| Generic Linux | ❌   | ✅      | ❌     | ✅     |

## Note for Linux
Make sure you have at least one of the following font families from each category installed.

### Sans Serif
- Noto Sans
- DejaVu Sans

### Serif
- Noto Serif
- DejaVu Serif
- Times New Roman

### Monospace
- Noto Sans Mono
- DejaVu Sans Mono

### Cursive
- Comic Sans MS

# Changelog
Release notes are available in [CHANGELOG.md](CHANGELOG.md).

# FAQ & Troubleshooting

## Bifrost isn't downloading watch firmware.
Unfortunately, Samsung doesn't serve the full firmware files for watches, so Bifrost can't download them.

## Bifrost is returning error 400/401 when downloading
These errors are on Samsung's end. If you can, try using a different region/CSC.

## Bifrost is returning error 403 when checking for updates
These errors are on Samsung's end. Samsung may no longer be serving firmware for your device or may not have started serving firmware yet. Try a different region/CSC if possible and check to make sure your model number is correct.

## Bifrost opens to a blank screen on Windows
On certain GPUs, Jetpack Compose/Skia has trouble rendering. Try running the program as an administrator.

If you have switchable graphics, try using a different GPU.

## Download speeds are slow
Samsung's servers sometimes throttle downloads to about 3MiB/s. For older devices, you may see even slower speeds. Different regions/CSCs may have faster downloads.

## How do I know which CSC to use?
On your device, do the following:
1. Open the Settings app.
2. Scroll down to "About phone" or "About tablet" and tap it.
3. Tap "Software information".
4. Scroll down to "Service provider software version".
5. You'll see something like "XAA/XAA,XAA/XAU/TMB" or "XAR/XAR/" on the second line.

The first three letters there are your current CSC. The last three letters are the original/firmware CSC of your device.  
Using the above examples, the first has a current CSC of XAA and a firmware CSC of TMB. The second has a current CSC of XAR and a firmware CSC of XAR.

## How do I choose an alternative CSC if mine isn't working?
Use the CSC picker dialog (the button that looks like a list inside the "Region" text field).  
You can search for your country or region in there and see the different CSCs used. If there are specific carriers associated with a CSC, they'll also be shown.

## Why is my antivirus flagging the app?
Certain antivirus programs may flag Bifrost as malware. This is (hopefully obviously) a false positive.

There's a trojan horse malware family named Bifrost, which is part of the greater Bifrose family.  
Antivirus programs flagging Bifrost (this app) seem to be doing it solely based on this app having the same name as the malware.

Bifrost (the malware) only affects Windows systems and has limited functionality after Windows XP. For more information, see [this Wikipedia article](https://en.wikipedia.org/wiki/Bifrost_(Trojan_horse)).

Bifrost (this app) does not contain malware. You can verify this by browsing through the source code or by compiling it yourself using the instructions below.

# Building
Building this project should be fairly easy.

## Prep:
1. Make sure you have the latest [Android Studio Canary](https://developer.android.com/studio/preview) installed.
2. Clone this project into Android Studio and let it import.

## Desktop

### Conveyor
Bifrost makes use of [Conveyor](https://www.hydraulic.dev/) to create binaries for different desktop platforms.

Conveyor can build for Windows and Linux from any host OS, but macOS is required to build for macOS.

1. To build, first download and install Conveyor from the link above.
2. Next, open a terminal to the project's root directory.
3. Run `./gradlew :desktop:build` (`.\gradlew.bat :desktop:build` on Windows).
4. Run the following command based on your target system.  
   4.1. Windows: `conveyor make windows-zip`.  
   4.2. Debian: `conveyor make debian-package`.  
   4.3. Linux: `conveyor make linux-tarball`.  
   4.4. Intel Macs: `conveyor -Kapp.machines=mac.amd64 make unnotarized-mac-zip`.  
   4.5. Apple Silicon Macs: `conveyor -Kapp.machines=mac.arm64 make unnotarized-mac-zip`.
5. Check the `output` folder in the root of the project for the binary.

### Gradle
Alternatively, you can run a debug binary by executing the `:desktop:run` task.

`./gradlew :desktop:run` (`.\gradlew :desktop:run` on Windows).

## Android:

### Command Line:
1. Open the Terminal view in Android Studio (bottom-left).
2. Enter `gradlew :android:build` on Windows or `./gradlew :android:build` on macOS and Linux.
3. Once it finishes building, go to `android/build/outputs/apk/debug` and install `android-debug.apk`.

### GUI:
1. Open the Gradle view in Android Studio (top-right).
2. Expand the project, then expand "android".
3. Expand "Tasks," then "build," and double-click "build".
4. Once it finishes building, go to `android/build/outputs/apk/debug` and install `android-debug.apk`.

# Running

## Android
Download `bifrost_android_<VERSION>.apk` and install it.

## Windows
Download the .zip ending in `windows-amd64`.

Native ARM64 Windows builds aren't currently available.

## macOS
- On Intel Macs, download the .zip ending in `mac-amd64`.
- On Apple Silicon Macs, download the .zip ending in `mac-aarch64`.

## Linux
- On Debian-based systems, download the `.deb` file.
- On other Linux distros, download the `.tar.gz` file.

On x64 Linux, download the `amd64` variant. On ARM64 Linux, choose `aarch64`.

# Translating

Bifrost uses Weblate for translations.

Help translate Bifrost to your language on the [project page](https://hosted.weblate.org/engage/bifrost/)!

<a href="https://hosted.weblate.org/engage/bifrost/">
<img src="https://hosted.weblate.org/widget/bifrost/strings/multi-auto.svg" alt="Translation status" />
</a>

# Screenshots

## Desktop:

<img src="/screenshots/DesktopDownload.png" alt="Desktop Downloader" width="400"></img>
<img src="/screenshots/DesktopDecrypt.png" alt="Desktop Decrypter" width="400"></img>
<img src="/screenshots/DesktopHistory.png" alt="Desktop History" width="400"></img>
<img src="/screenshots/DesktopSettings.png" alt="Desktop Settings" width="400"></img>

## Mobile:
<img src="/screenshots/AndroidDownload.png" alt="Android Downloader" width="400"></img>
<img src="/screenshots/AndroidDecrypt.png" alt="Android Decrypter" width="400"></img>
<img src="/screenshots/AndroidHistory.png" alt="Android History" width="400"></img>
<img src="/screenshots/AndroidSettings.png" alt="Android Settings" width="400"></img>

# Error Reporting
Bifrost uses Bugsnag for error reporting.

<a href="https://www.bugsnag.com"><img src="https://assets-global.website-files.com/607f4f6df411bd01527dc7d5/63bc40cd9d502eda8ea74ce7_Bugsnag%20Full%20Color.svg" width="200"></a>
