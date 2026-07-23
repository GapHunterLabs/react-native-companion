package dev.gaphunter.reactnativecompanion.ui

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import dev.gaphunter.reactnativecompanion.runner.DeviceOutputParser
import dev.gaphunter.reactnativecompanion.runner.ReactNativeCommandRunner
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel

/**
 * Deliberately plain buttons + a combo box, not a custom-drawn UI --
 * this is a v0.1 free tier meant to fix the incumbent's reliability
 * complaints (verdict_console.md), not to out-design it. Device
 * refresh runs off the EDT (`executeOnPooledThread`) for the same
 * reason ReactNativeCommandRunner uses OSProcessHandler: nothing here
 * should ever block the UI thread.
 */
class ReactNativeConsolePanel(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
    private val deviceModel = DefaultComboBoxModel<String>()
    private val deviceCombo = JComboBox(deviceModel)

    init {
        Disposer.register(this, console)

        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        toolbar.add(JButton("Run Android").apply { addActionListener { runCommand(listOf("run-android")) } })
        toolbar.add(JButton("Run iOS").apply { addActionListener { runCommand(listOf("run-ios")) } })
        toolbar.add(JButton("Start Metro").apply { addActionListener { runCommand(listOf("start")) } })
        toolbar.add(deviceCombo)
        toolbar.add(JButton("Refresh Devices").apply { addActionListener { refreshDevices() } })

        add(toolbar, BorderLayout.NORTH)
        add(console.component, BorderLayout.CENTER)

        refreshDevices()
    }

    private fun runCommand(args: List<String>) {
        val workDirectory = project.basePath ?: return
        ReactNativeCommandRunner.run(workDirectory, args, console)
    }

    private fun refreshDevices() {
        ApplicationManager.getApplication().executeOnPooledThread {
            val androidNames = try {
                val handler = CapturingProcessHandler(ReactNativeCommandRunner.runAdbDevices())
                val output = handler.runProcess(5_000)
                DeviceOutputParser.parseAdbDevices(output.stdout).filter { it.isUsable }.map { "Android: ${it.serial}" }
            } catch (e: Exception) {
                emptyList()
            }
            val iosNames = try {
                val handler = CapturingProcessHandler(ReactNativeCommandRunner.runSimctlListDevices())
                val output = handler.runProcess(5_000)
                DeviceOutputParser.parseSimctlDevices(output.stdout).map { "iOS: ${it.name}" }
            } catch (e: Exception) {
                emptyList()
            }

            ApplicationManager.getApplication().invokeLater {
                deviceModel.removeAllElements()
                (androidNames + iosNames).forEach { deviceModel.addElement(it) }
            }
        }
    }

    override fun dispose() {}
}
