/* This file is part of JnaFileChooser.
 *
 * JnaFileChooser is free software: you can redistribute it and/or modify it
 * under the terms of the new BSD license.
 *
 * JnaFileChooser is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 */
package jnafilechooser.api

import com.sun.jna.Platform
import java.awt.Window
import java.io.File
import java.util.Arrays
import java.util.Collections
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * JnaFileChooser is a wrapper around the native Windows file chooser
 * and folder browser that falls back to the Swing JFileChooser on platforms
 * other than Windows or if the user chooses a combination of features
 * that are not supported by the native dialogs (for example multiple
 * selection of directories).
 *
 * Example:
 * JnaFileChooser fc = new JnaFileChooser();
 * fc.setFilter("All Files", "*");
 * fc.setFilter("Pictures", "jpg", "jpeg", "gif", "png", "bmp");
 * fc.setMultiSelectionEnabled(true);
 * fc.setMode(JnaFileChooser.Mode.FilesAndDirectories);
 * if (fc.showOpenDialog(parent)) {
 * Files[] selected = fc.getSelectedFiles();
 * // do something with selected
 * }
 *
 * @see JFileChooser, WindowsFileChooser, WindowsFileBrowser
 */
class JnaFileChooser() {
    private enum class Action {
        Open,
        Save
    }

    /**
     * the availabe selection modes of the dialog
     */
    enum class Mode(val jFileChooserValue: Int) {
        Files(JFileChooser.FILES_ONLY),
        Directories(JFileChooser.DIRECTORIES_ONLY),
        FilesAndDirectories(JFileChooser.FILES_AND_DIRECTORIES)

    }

    var selectedFiles: Array<File?>
        protected set
    var currentDirectory: File? = null
        protected set
    protected var filters: ArrayList<Array<String>> = ArrayList()

    /**
     * sets whether to enable multiselection
     */
    var isMultiSelectionEnabled = false

    /**
     * sets the selection mode
     */
    var mode: Mode = Mode.Files
    protected var defaultFile = ""
    protected var dialogTitle = ""
    var openButtonText = ""
    var saveButtonText = ""

    /**
     * creates a new file chooser with multiselection disabled and mode set
     * to allow file selection only.
     */
    init {
        selectedFiles = arrayOf(null)
    }

    /**
     * creates a new file chooser with the specified initial directory
     *
     * @param currentDirectory the initial directory
     */
    constructor(currentDirectory: File?) : this() {
        if (currentDirectory != null) {
            this.currentDirectory =
                if (currentDirectory.isDirectory()) currentDirectory else currentDirectory.getParentFile()
        }
    }

    /**
     * creates a new file chooser with the specified initial directory
     *
     * @param currentDirectoryPath the initial directory
     */
    constructor(currentDirectoryPath: String?) : this(
        if (currentDirectoryPath != null) File(
            currentDirectoryPath
        ) else null
    )

    /**
     * shows a dialog for opening files
     *
     * @param parent the parent window
     *
     * @return true if the user clicked OK
     */
    fun showOpenDialog(parent: Window): Boolean {
        return showDialog(parent, Action.Open)
    }

    /**
     * shows a dialog for saving files
     *
     * @param parent the parent window
     *
     * @return true if the user clicked OK
     */
    fun showSaveDialog(parent: Window): Boolean {
        return showDialog(parent, Action.Save)
    }

    private fun showDialog(parent: Window, action: Action): Boolean {
        // native windows filechooser doesn't support mixed selection mode
        if (Platform.isWindows() && mode != Mode.FilesAndDirectories) {
            // windows filechooser can only multiselect files
            if (isMultiSelectionEnabled && mode == Mode.Files) {
                // TODO Here we would use the native windows dialog
                // to choose multiple files. However I haven't been able
                // to get it to work properly yet because it requires
                // tricky callback magic and somehow this didn't work for me
                // quite as documented (probably because I messed something up).
                // Because I don't need this feature right now I've put it on
                // hold to get on with stuff.
                // Example code: http://support.microsoft.com/kb/131462/en-us
                // GetOpenFileName: http://msdn.microsoft.com/en-us/library/ms646927.aspx
                // OFNHookProc: http://msdn.microsoft.com/en-us/library/ms646931.aspx
                // CDN_SELCHANGE: http://msdn.microsoft.com/en-us/library/ms646865.aspx
                // SendMessage: http://msdn.microsoft.com/en-us/library/ms644950.aspx
            } else if (!isMultiSelectionEnabled) {
                if (mode == Mode.Files) {
                    return showWindowsFileChooser(parent, action)
                } else if (mode == Mode.Directories) {
                    return showWindowsFolderBrowser(parent)
                }
            }
        }

        // fallback to Swing
        return showSwingFileChooser(parent, action)
    }

    private fun showSwingFileChooser(parent: Window, action: Action): Boolean {
        val fc = JFileChooser(currentDirectory)
        fc.setMultiSelectionEnabled(isMultiSelectionEnabled)
        fc.setFileSelectionMode(mode.jFileChooserValue)

        // set select file
        if (defaultFile.isNotEmpty() and (action == Action.Save)) {
            val fsel = File(defaultFile)
            fc.setSelectedFile(fsel)
        }
        if (dialogTitle.isNotEmpty()) {
            fc.setDialogTitle(dialogTitle)
        }
        if ((action == Action.Open) and openButtonText.isNotEmpty()) {
            fc.setApproveButtonText(openButtonText)
        } else if ((action == Action.Save) and saveButtonText.isNotEmpty()) {
            fc.setApproveButtonText(saveButtonText)
        }

        // build filters
        if (filters.size > 0) {
            var useAcceptAllFilter = false
            for (spec in filters) {
                // the "All Files" filter is handled specially by JFileChooser
                if (spec[1] == "*") {
                    useAcceptAllFilter = true
                    continue
                }
                fc.addChoosableFileFilter(
                    FileNameExtensionFilter(
                        spec[0], *Arrays.copyOfRange(spec, 1, spec.size)
                    )
                )
            }
            fc.setAcceptAllFileFilterUsed(useAcceptAllFilter)
        }
        val result: Int = if (action == Action.Open) {
            fc.showOpenDialog(parent)
        } else {
            if (saveButtonText.isEmpty()) {
                fc.showSaveDialog(parent)
            } else {
                fc.showDialog(parent, null)
            }
        }
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFiles =
                if (isMultiSelectionEnabled) fc.selectedFiles else arrayOf(fc.selectedFile)
            currentDirectory = fc.currentDirectory
            return true
        }
        return false
    }

    private fun showWindowsFileChooser(parent: Window, action: Action): Boolean {
        val fc = WindowsFileChooser(currentDirectory)
        fc.filters = filters
        if (defaultFile.isNotEmpty()) fc.defaultFilename = defaultFile
        if (dialogTitle.isNotEmpty()) {
            fc.setTitle(dialogTitle)
        }
        val result = fc.showDialog(parent, action == Action.Open)
        if (result) {
            selectedFiles = arrayOf(fc.selectedFile)
            currentDirectory = fc.currentDirectory
        }
        return result
    }

    private fun showWindowsFolderBrowser(parent: Window): Boolean {
        val fb = WindowsFolderBrowser()
        if (dialogTitle.isNotEmpty()) {
            fb.setTitle(dialogTitle)
        }
        val file = fb.showDialog(parent)
        if (file != null) {
            selectedFiles = arrayOf(file)
            currentDirectory = if (file.getParentFile() != null) file.getParentFile() else file
            return true
        }
        return false
    }

    /**
     * add a filter to the user-selectable list of file filters
     *
     * @param name   name of the filter
     * @param filter you must pass at least 1 argument, the arguments are the file
     * extensions.
     */
    fun addFilter(name: String, vararg filter: String) {
        require(filter.isNotEmpty())
        val parts = ArrayList<String>()
        parts.add(name)
        Collections.addAll(parts, *filter)
        filters.add(parts.toTypedArray())
    }

    fun setCurrentDirectory(currentDirectoryPath: String?) {
        currentDirectory = if (currentDirectoryPath != null) File(currentDirectoryPath) else null
    }

    fun setDefaultFileName(dfile: String) {
        defaultFile = dfile
    }

    /**
     * set a title name
     *
     * @param title Title of dialog
     */
    fun setTitle(title: String) {
        dialogTitle = title
    }

    val selectedFile: File?
        get() = selectedFiles[0]
}