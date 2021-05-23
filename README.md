# Notice
Manual firmware downloads and the firmware history feature have been disabled for now. Samsung changed something on the backend and always serves the latest available firmware, no matter which is requested.

If you know a workaround, please follow up on [this issue](https://github.com/zacharee/SamloaderKotlin/issues/10).

# Samsung Firmware Downloader
This is yet another firmware downloader for Samsung devices, but it has some special features.

For one, it's cross-platform. Samsung Firmware Downloader runs on Windows, Linux, macOS, and even Android! 

Samsung Firmware Downloader is also a graphical program, with a shared UI across all supported platforms.

Most of the functionality in Samsung Firmware Downloader is based on [Samloader](https://github.com/nlscc/samloader). The Python code has been converted to Kotlin and tweaked to take advantage of some of Kotlin's features.

Samsung Firmware Downloader uses Jetpack Compose, JetBrains Compose for Desktop, and Kotlin Multiplatform to create a shared codebase for all supported platforms.

# Download
Binaries are available for 64-bit versions Windows, Linux, macOS, and Android. JetBrains Compose can't currently build for 32-bit operating systems.

Check out the [Releases](https://github.com/zacharee/SamloaderKotlin/releases) page for the downloads.

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
1. Open the Gradle view in Android Studio (top-right).
2. Expand the project, then expand "desktop".
3. Expand "Tasks," then "build," and double-click "createDistributable" on Windows and Linux, or "packageDmg" on macOS.
4. Once it finishes building, check the output log to see where the executable was saved.

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

1. Extract the release ZIP for Windows and go through the folders until you find "Samsung Firmware Downloader.exe".
2. Launch the EXE. If it fails, launch as Administrator.

## Linux

1. Extract the release ZIP for Linux and go through the folders until you find the "Samsung Firmware Downloader".
2. Open a terminal in this location.
3. Enter `chmod +x Samsung\ Firmware\ Downloader`.
4. Enter `./Samsung\ Firmware\ Downloader`.

## macOS

1. Extract the release ZIP and open the DMG.
2. Move "Samsung Firmware Downloader.app" to the Applications folder.
3. Launch the app.

There may be a security error when launching the app. If there is, follow the steps outlined [here](https://github.com/hashicorp/terraform/issues/23033#issuecomment-542302933).

Alternatively, if the above doesn't work, you can try running the following in a Terminal (requires root permissions):

`sudo xattr -cr /Applications/Samsung\ Firmware\ Downloader.app`.

Once that command is executed, the app should run.

## Android

1. Download the release APK to your phone.
2. Install and run it.

# Screenshots

## Desktop:

![Blank Desktop Downloader](/screenshots/DesktopDownloadViewBlank.png)
![Blank Desktop Decrypter](/screenshots/DesktopDecryptViewBlank.png)
![Desktop Download Progress](/screenshots/DesktopDownloadViewProgress.png)

## Mobile:
![Blank Android Downloader](/screenshots/AndroidDownloadViewBlank.jpg)
![Blank Android Decrypter](/screenshots/AndroidDecryptViewBlank.jpg)
![Android Download Progress](/screenshots/AndroidDownloadViewProgress.jpg)
