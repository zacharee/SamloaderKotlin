package tk.zwander.common.ui

import tk.zwander.common.util.CrossPlatformBugsnag
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Collections
import java.util.Objects
import java.util.function.Consumer
import java.util.regex.Pattern

/**
 * Used for detecting the dark theme on a Linux (GNOME/GTK) system.
 * Tested on Ubuntu.
 *
 * @author Daniel Gyorffy
 */
internal class GenericLinuxThemeDetector {
    private val listeners = Collections.synchronizedSet(HashSet<Consumer<Boolean>?>())
    private val darkThemeNamePattern = Pattern.compile(".*dark.*", Pattern.CASE_INSENSITIVE)
    private var detectorThread: DetectorThread? = null
    val isDark: Boolean
        get() {
            try {
                val runtime = Runtime.getRuntime()
                val process = runtime.exec(GET_CMD)
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    val readLine = reader.readLine()
                    if (readLine != null) {
                        return isDarkTheme(readLine)
                    }
                }
            } catch (e: IOException) {
                println("Couldn't detect Linux OS theme")
                e.printStackTrace()
                CrossPlatformBugsnag.notify(e)
            }
            return false
        }

    private fun isDarkTheme(gtkTheme: String): Boolean {
        return darkThemeNamePattern.matcher(gtkTheme).matches()
    }

    @Synchronized
    fun registerListener(darkThemeListener: Consumer<Boolean>) {
        Objects.requireNonNull(darkThemeListener)
        val listenerAdded = listeners.add(darkThemeListener)
        val singleListener = listenerAdded && listeners.size == 1
        val threadInterrupted = detectorThread != null && detectorThread!!.isInterrupted
        if (singleListener || threadInterrupted) {
            detectorThread = DetectorThread(this)
            detectorThread!!.start()
        }
    }

    @Synchronized
    fun removeListener(darkThemeListener: Consumer<Boolean>?) {
        listeners.remove(darkThemeListener)
        if (listeners.isEmpty()) {
            detectorThread!!.interrupt()
            detectorThread = null
        }
    }

    /**
     * Thread implementation for detecting the actually changed theme
     */
    private class DetectorThread(private val detector: GenericLinuxThemeDetector) :
        Thread() {
        private var lastValue: Boolean

        init {
            lastValue = detector.isDark
            name = "GTK Theme Detector Thread"
            this.isDaemon = true
            priority = NORM_PRIORITY - 1
        }

        @Suppress("NewApi")
        override fun run() {
            try {
                val runtime = Runtime.getRuntime()
                val monitoringProcess = runtime.exec(MONITORING_CMD)
                BufferedReader(InputStreamReader(monitoringProcess.inputStream)).use { reader ->
                    while (!this.isInterrupted) {
                        //Expected input = gtk-theme: '$GtkThemeName'
                        val readLine = reader.readLine() ?: ""
                        val keyValue =
                            readLine.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        val value = keyValue.getOrNull(1)
                        val currentDetection = detector.isDarkTheme(value ?: "")
                        println("Theme changed detection, dark: $currentDetection")
                        if (currentDetection != lastValue) {
                            lastValue = currentDetection
                            for (listener in detector.listeners) {
                                try {
                                    listener!!.accept(currentDetection)
                                } catch (e: RuntimeException) {
                                    println("Caught exception during listener notifying ")
                                    e.printStackTrace()
                                    CrossPlatformBugsnag.notify(e)
                                }
                            }
                        }
                    }
                    println("ThemeDetectorThread has been interrupted!")
                    if (monitoringProcess.isAlive) {
                        monitoringProcess.destroy()
                        println("Monitoring process has been destroyed!")
                    }
                }
            } catch (e: IOException) {
                println("Couldn't start monitoring process ")
                e.printStackTrace()
                CrossPlatformBugsnag.notify(e)
            } catch (e: ArrayIndexOutOfBoundsException) {
                println("Couldn't parse command line output")
                e.printStackTrace()
                CrossPlatformBugsnag.notify(e)
            }
        }
    }

    companion object {
        private val MONITORING_CMD = arrayOf("gsettings", "monitor", "org.gnome.desktop.interface", "icon-theme")
        private val GET_CMD = arrayOf("gsettings", "get", "org.gnome.desktop.interface", "icon-theme")
    }
}
