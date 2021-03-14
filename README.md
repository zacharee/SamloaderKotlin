# Samsung Firmware Downloader
This is yet another firmware downloader for Samsung devices, but it has some special features.

For one, it's cross-platform. Samsung Firmware Downloader runs on Windows, Linux, macOS, and even Android! 

Samsung Firmware Downloader is also a graphical program, with a shared UI across all supported platforms.

Most of the functionality in Samsung Firmware Downloader is based on [Samloader](https://github.com/nlscc/samloader). The Python code has been converted to Kotlin and tweaked to take advantage of some of Kotlin's features.

Samsung Firmware Downloader uses Jetpack Compose, JetBrains Compose for Desktop, and Kotlin Multiplatform to create a shared codebase for all supported platforms.

# Download
Currently, binaries are available for Windows, Linux, and Android. JetBrains Compose and Kotlin Multiplatform can only compile for the platform they're currently running on, and I don't have a Mac.

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

On macOS, you'll have to also add an extra step. Use the same process as above to run the "notarizeDmg" task after packaging.

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

# Screenshots

## Desktop:

![Blank Desktop Downloader](/screenshots/DesktopDownloadViewBlank.png)
![Blank Desktop Decrypter](/screenshots/DesktopDecryptViewBlank.png)
![Desktop Download Progress](/screenshots/DesktopDownloadViewProgress.png)

## Mobile:
![Blank Android Downloader](/screenshots/AndroidDownloadViewBlank.jpg)
![Blank Android Decrypter](/screenshots/AndroidDecryptViewBlank.jpg)
![Android Download Progress](/screenshots/AndroidDownloadViewProgress.jpg)