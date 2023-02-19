# Notice
Manual firmware downloads and downloading from the firmware history feature have been disabled for now. Samsung changed something on the backend and always serves the latest available firmware, no matter which is requested.

If you know a workaround, please follow up on [this issue](https://github.com/zacharee/SamloaderKotlin/issues/10).

# Bifrost - Samsung Firmware Downloader
This is yet another firmware downloader for Samsung devices, but it has some special features.

For one, it's cross-platform. Bifrost runs on Windows, Linux, macOS, and even Android! 

Bifrost is also a graphical program, with a shared UI across all supported platforms.

Most of the functionality in Bifrost is based on [Samloader](https://github.com/nlscc/samloader). The Python code has been converted to Kotlin and tweaked to take advantage of some of Kotlin's features.

Bifrost uses Jetpack Compose, JetBrains Compose for Desktop, and Kotlin Multiplatform to create a shared codebase for all supported platforms.

# Download
Binaries are available for 64-bit versions Windows, Linux, macOS, and Android. JetBrains Compose can't currently build for 32-bit operating systems.

Check out the [Releases](https://github.com/zacharee/SamloaderKotlin/releases) page for the downloads.

# Changelog
Release notes are available in [CHANGELOG.md](CHANGELOG.md).

# FAQ & Troubleshooting

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
   
## Desktop:
Run the `package` Gradle task.

### Command Line:
1. Open the Terminal view in Android Studio (bottom-left).
2. Enter `gradlew createDistributable` on Windows, `./gradlew createDistributable` on Linux, or `./gradlew packageDmg` on macOS.
3. Once it finishes building, check the output log to see where the executable was saved.

### GUI:
1. Go to Android Studio's settings (Ctrl+Alt+S on Windows and Linux, CMD+, on macOS), go to "Experimental", and uncheck "Only include test tasks in the Gradle task list generated during Gradle Sync".
2. Open the Gradle view in Android Studio (top-right).
3. Expand the project, then expand "desktop".
4. Expand "Tasks", then "compose desktop" and double-click "createDistributable" on Windows and Linux, or "packageDmg" on macOS.
5. Once it finishes building, check the output log to see where the executable was saved.

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

## Windows

1. Extract the release ZIP for Windows and go through the folders until you find "Bifrost.exe".
2. Launch the EXE. If it fails, launch as Administrator.

## Linux

1. Extract the release ZIP for Linux and go through the folders until you find "Bifrost".
2. Open a terminal in this location.
3. Enter `chmod +x Bifrost`.
4. Enter `./Bifrost`.

## macOS

1. Extract the release ZIP and open the DMG.
2. Move "Bifrost.app" to the Applications folder.
3. Launch the app.

There may be a security error when launching the app. If there is, follow the steps outlined [here](https://github.com/hashicorp/terraform/issues/23033#issuecomment-542302933).

Alternatively, if the above doesn't work, you can try running the following in a Terminal (requires root permissions):

`sudo xattr -cr /Applications/Bifrost.app`.

Once that command is executed, the app should run.

It's also possible that the DMG itself will refuse to open. If that happens, the same `xattr` command, but run on the DMG, should work:

`sudo xattr -cr ~/Downloads/Bifrost-<VERSION>.dmg`.

## Android

1. Download the release APK to your phone.
2. Install and run it.

# Translating

Bifrost supports basic text localization. You can help translate here: https://crowdin.com/project/bifrost-kotlin.

Note: Pay special attention to formatting arguments. Numbers inside curly brackets (e.g., `{0}`, `{1}`) should be kept as-is as they will be replaced with text during the application's runtime.

Note: Make sure to keep any other formatting characters as-is (e.g., `\n` should stay as `\n` and `%%` should stay as `%%`).

## Translators:

- Russian: [Leo17032009](https://github.com/Leo17032009)

# Screenshots

## Desktop:

![Blank Desktop Downloader](/screenshots/DesktopDownloadViewBlank.png)
![Blank Desktop Decrypter](/screenshots/DesktopDecrypterViewBlank.png)
![Blank Desktop History](/screenshots/DesktopHistoryViewBlank.png)
![Desktop Download Progress](/screenshots/DesktopDownloadViewProgress.png)
![Desktop Decrypter Progress](/screenshots/DesktopDecrypterViewProgress.png)
![Desktop History Populated](/screenshots/DesktopHistoryViewPopulated.png)

## Mobile:
![Blank Android Downloader](/screenshots/AndroidDownloaderBlank.png)
![Blank Android Decrypter](/screenshots/AndroidDecrypterBlank.png)
![Blank Android History](/screenshots/AndroidHistoryBlank.png)
![Android Download Progress](/screenshots/AndroidDownloaderProgress.png)
![Android Decrypter Progress](/screenshots/AndroidDecrypterProgress.png)
![Android History Populated](/screenshots/AndroidHistoryPopulated.png)
