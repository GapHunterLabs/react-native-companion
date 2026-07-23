package dev.gaphunter.reactnativecompanion.runner

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.util.SystemInfo
import java.io.File

/**
 * The real complaint this plugin exists to fix (verdict_console.md,
 * evidence_9564.md, 2026-04 ★1): "severely impacting IDE performance...
 * PhpStorm frequently becomes unresponsive... reproducible — thread
 * dumps are required." `OSProcessHandler` spawns and reads the process
 * on background threads by construction -- using it (instead of e.g.
 * `Runtime.exec` + a blocking `.waitFor()` on the EDT) is not an
 * optimization here, it IS the fix. Never call anything blocking on
 * this class from the UI thread regardless.
 */
object ReactNativeCommandRunner {

    fun run(workDirectory: String, args: List<String>, console: ConsoleView): OSProcessHandler {
        val commandLine = GeneralCommandLine(npxExecutable())
            .withParameters(listOf("react-native") + args)
            .withWorkDirectory(File(workDirectory))
            .withCharset(Charsets.UTF_8)

        val processHandler = OSProcessHandler(commandLine)
        console.attachToProcess(processHandler)
        processHandler.startNotify()
        return processHandler
    }

    fun runAdbDevices(): GeneralCommandLine =
        GeneralCommandLine("adb", "devices").withCharset(Charsets.UTF_8)

    fun runSimctlListDevices(): GeneralCommandLine =
        GeneralCommandLine("xcrun", "simctl", "list", "devices").withCharset(Charsets.UTF_8)

    private fun npxExecutable(): String = if (SystemInfo.isWindows) "npx.cmd" else "npx"
}
