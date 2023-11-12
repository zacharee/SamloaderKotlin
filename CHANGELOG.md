# 1.15.0
- macOS builds should now be fully signed and notarized!
- Add option to allow lowercase characters in text fields.
- Persist model/region/firmware/manual values across app restarts.
- Work on text field input performance.
- Update dependencies.

# 1.14.3
- Fix a crash on launch on desktop.
- Migrate to Korlibs 4.
- Fix harmless SLF4J error.
- Other crash fixes.

# 1.14.2
- Fix downloads (#109) (thanks @ananjaser1211).
- Add Bugsnag error reporting.
- Update Compose to 1.5.10.
- Update CSC options.
- Add Bugsnag.
- Fix some date formats (#112) (thanks @Tostis).
- Add native ARM64 macOS build.

# 1.14.1
- Add some more CSCs.
- Fix an issue with version checking preventing downloads.
- Replace a bunch of model states with flows.
- Don't allow the CSC picker dialog to change the current CSC while an operation is running.

# 1.14.0
- Avoid a crash when parsing Patreon supporters fails.
- Add a CSC picker dialog to make it easier to choose the right CSC or pick an alternative CSC.
- Add Material You icon for Android 12+.
- Tweak dialog behavior and UI.
- Add Mastodon social link.
- Move version info to dialog if screen width is below 600dp.

# 1.13.1
- Fix crashes and UI issues on Android.

# 1.13.0
- The History view now uses LazyVerticalStaggeredGrid instead of a manual non-lazy implementation.
- Upgrade to Kotlin 1.8.0 and Compose 1.3.x.
- Add dynamic theming for Android, Windows, and macOS.
- Move to Material Design 3.
- Set minimum window dimensions to 200x300dp.
- Work on pager performance on Android.
- Decrease spacing between icon buttons in footer.
- Show a loading indicator when fetching version info in the Download view.
- Fix the last card in the History view being cut off.
- Update dependencies.

# 1.12.0
- Allow continuing download if Bifrost detects a version mismatch.
- Improve version mismatch checking.
- Improve dialog appearance.
- Work on native macOS version.
- Update dependencies.

# 1.0.11
- Another fix for Windows resources.

# 1.0.10
- Temporarily remove translations until there's a better framework.
- Update progress bar layout and add some more animations.
- Implement functional browser version (still no plans to release it).
- Improve file handle cleanup.
- Add history fallback using Samsung's version.xml page.
- Update dependencies.
- Add temporary workaround for loading resources on Windows.

# 1.0.9
- Update dependencies.
- Update to Kotlin 1.7.0.
- Add Thai translation.
- Fix issues with operations getting stuck/hanging.
- Add some more animations.
- Add some missing strings to resource files.
- Create initial (non-functional) Compose versions for web and macOS native.
- Make image resources properly cross-platform.
- Fix build for Android and JVM.

# 1.0.8
- Update dependencies.
- Resize content for on-screen keyboard on Android.
- Make JS version build again.
- Implement a localization framework.
- Add Russian translation.
- Only show Settings gear if settings are available.
- Use OpenGL renderer on Windows.

# 1.0.7
- Update dependencies.
- Extract strings to variables.
- Fix an error when verifying served firmware.
- Fix broken Windows icon.

# 1.0.6
- Update dependencies.
- Clean up code.
- Work around an issue with blank file pickers by using JFileChooser by default.
- Add a new setting to switch back to using FileDialog.
- Update build to JDK 18.

# 1.0.5
- Make version comparison more reliable for manual mode.

# 1.0.4
- Code cleanup.
- Implement horizontal pager on Android for swiping between views.
- Update Compose and Kotlin.
- Re-enable manual firmware input:
  - There's now a warning when enabled.
  - Bifrost will verify that the requested firmware matches the served firmware.
- Implement some better errors.
- Fix a crash caused by 403 return statuses.
- Fix a crash when the Samsung docs URL is null.

# 1.0.3
- Implement some menu bar items for macOS.
- Add a Patreon Supporters dialog.
- Fix an issue where making HTTP requests would indefinitely hang.
- Clean up code.

# 1.0.2
- Fix a crash on Linux and Windows caused by the About dialog.

# 1.0.1
- Fix macOS package name.
- Update file names.

# 1.0.0
- Rename to Bifrost.
- Update icon and colors.
- Fix up about dialog for macOS.

# 0.5.3
- Update the Windowing API.
- Update Kotlin to 1.5.31.
- Update Compose to 1.0.0.
- Fix some crashes on Android 12.
- Remove reliance on manual DPScale.
- Fix dark mode for macOS.
- Add an about dialog for macOS.
- Fix History View.
- Code cleanup.
- Create an experimental browser version (likely never will be live).

# 0.5.2
- Update dependencies.
- Fix some crashes.
- Properly handle changelogs in other languages.

# 0.5.1
- Update dependencies.
- Move from SamMobile to OdinRom for history tab.
- Add release date to changelogs for history items.
- Update appearance of some UI elements.

# 0.5.0
- Add changelog for current firmware and for items in history tab.
- Use staggered grid for history tab (may cause performance issues).

# 0.4.1
- Remove dependence on Bintray.
- Add back history tab without download buttons.
- Fix a grid issue in history tab.
- Code cleanup.
- Fix crash on Android <8.0.

# 0.4.0
- Update dependencies.
- Remove manual firmware downloads.
- Remove history tab.
- Clean up code.

# 0.3.2
- Backend changes for better organization.
- Make notification show progress on Android.
- Update dependencies.
- Add macOS build.

# 0.3.1
- Update dependencies.
- Add ability to copy info from a history item to the download and decrypt pages. 
- Show OS version text when checking for latest update.
- Limit max content width.

# 0.3.0
- Add a new page for viewing the firmware history of a device and region combo.
- Make some UI tweaks for better desktop display.
- Decrease footer size.
- Decrease some padding values.
- Update dependencies.

# 0.2.1
- Fix the "Working..." notification not dismissing when closing the app.

# 0.2.0
- Update README with build instructions for Android.
- Add MIT license.
- Make main content scrollable.
- Fix screen rotation breaking download.
- Reorder some buttons.
- Make layout slightly more responsive, with auto-flowing input fields and hybrid image/text buttons.
- Add a hint to the firmware field for its format.
- Run download/decrypt in a Service on Android.
- Other misc UI tweaks.

# 0.1.0
Initial Release
