# 1.0.4
- Code cleanup.
- Implement horizontal pager on Android for swiping between views.
- Update Compose and Kotlin.
- Re-enable manual firmware input:
  - There's now a warning when enabled.
  - Bifrost will verify that the requested firmware matches the served firmware.
- Implement some better errors.
- Fix a crash caused by 403 return statuses.

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
