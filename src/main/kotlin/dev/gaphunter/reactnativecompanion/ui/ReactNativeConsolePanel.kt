package dev.gaphunter.reactnativecompanion.ui

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import dev.gaphunter.reactnativecompanion.runner.DeviceOutputParser
import dev.gaphunter.reactnativecompanion.runner.EnvProfileDiscovery
import dev.gaphunter.reactnativecompanion.runner.ReactNativeCommandRunner
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

private const val NO_ENV_PROFILE = "(none)"

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
    private val envModel = DefaultComboBoxModel<String>()
    private val envCombo = JComboBox(envModel)

    init {
        Disposer.register(this, console)

        val runToolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        runToolbar.add(JButton("Run Android").apply { addActionListener { runCommand(listOf("run-android")) } })
        runToolbar.add(JButton("Run iOS").apply { addActionListener { runCommand(listOf("run-ios")) } })
        runToolbar.add(JButton("Start Metro").apply { addActionListener { runCommand(listOf("start")) } })
        runToolbar.add(deviceCombo)
        runToolbar.add(JButton("Refresh Devices").apply { addActionListener { refreshDevices() } })

        val releaseToolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        releaseToolbar.add(JButton("Build Android Release").apply {
            addActionListener { runCommand(listOf("build-android", "--mode=release")) }
        })
        releaseToolbar.add(JButton("Build iOS Release").apply {
            addActionListener { runCommand(listOf("build-ios", "--mode=Release")) }
        })
        releaseToolbar.add(JLabel("Environment:"))
        releaseToolbar.add(envCombo)
        releaseToolbar.add(JButton("Refresh Environments").apply { addActionListener { refreshEnvProfiles() } })

        val toolbars = JPanel(GridLayout(2, 1))
        toolbars.add(runToolbar)
        toolbars.add(releaseToolbar)

        add(toolbars, BorderLayout.NORTH)
        add(console.component, BorderLayout.CENTER)

        refreshDevices()
        refreshEnvProfiles()
    }

    private fun runCommand(args: List<String>) {
        val workDirectory = project.basePath ?: return
        val selectedEnv = envCombo.selectedItem as? String
        val envFile = if (selectedEnv == null || selectedEnv == NO_ENV_PROFILE) null else selectedEnv
        ReactNativeCommandRunner.run(workDirectory, args, console, envFile)
    }

    private fun refreshEnvProfiles() {
        val workDirectory = project.basePath ?: return
        val previouslySelected = envCombo.selectedItem as? String
        envModel.removeAllElements()
        envModel.addElement(NO_ENV_PROFILE)
        EnvProfileDiscovery.discover(workDirectory).forEach { envModel.addElement(it) }
        val restored = previouslySelected?.takeIf { envModel.getIndexOf(it) >= 0 } ?: NO_ENV_PROFILE
        envCombo.selectedItem = restored
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
