package dev.gaphunter.reactnativecompanion.ui

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.ui.JBUI
import dev.gaphunter.reactnativecompanion.review.ReviewPrompt
import dev.gaphunter.reactnativecompanion.runner.Action
import dev.gaphunter.reactnativecompanion.runner.CommandPlan
import dev.gaphunter.reactnativecompanion.runner.DeviceOutputParser
import dev.gaphunter.reactnativecompanion.runner.DeviceTarget
import dev.gaphunter.reactnativecompanion.runner.EnvProfileDiscovery
import dev.gaphunter.reactnativecompanion.runner.Platform
import dev.gaphunter.reactnativecompanion.runner.ProjectKind
import dev.gaphunter.reactnativecompanion.runner.ReactNativeCommandRunner
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import java.io.File
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

private const val NO_ENV_PROFILE = "(none)"

/**
 * Deliberately plain buttons + combo boxes, not a custom-drawn UI -- this
 * plugin exists to fix the incumbent's reliability complaints, not to
 * out-design it. Device refresh runs off the EDT (`executeOnPooledThread`)
 * for the same reason ReactNativeCommandRunner uses OSProcessHandler:
 * nothing here should ever block the UI thread.
 *
 * Every button goes through [CommandPlan], so the same buttons drive a
 * React Native CLI project or an Expo project, and the device picked in
 * the combo is the one the app actually runs on.
 */
class ReactNativeConsolePanel(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
    private val deviceModel = DefaultComboBoxModel<DeviceTarget>()
    private val deviceCombo = JComboBox(deviceModel)
    private val envModel = DefaultComboBoxModel<String>()
    private val envCombo = JComboBox(envModel)
    private val kindLabel = JLabel()
    private var kind = ProjectKind.REACT_NATIVE_CLI

    init {
        border = JBUI.Borders.empty(8)
        Disposer.register(this, console)

        val runToolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        runToolbar.add(kindLabel)
        runToolbar.add(JButton("Run Android").apply { addActionListener { run(Action.RUN_ANDROID) } })
        runToolbar.add(JButton("Run iOS").apply { addActionListener { run(Action.RUN_IOS) } })
        runToolbar.add(JButton("Start Dev Server").apply { addActionListener { run(Action.START) } })
        runToolbar.add(JLabel("Device:"))
        runToolbar.add(deviceCombo)
        runToolbar.add(JButton("Refresh").apply {
            addActionListener {
                refreshProjectKind()
                refreshDevices()
                refreshEnvProfiles()
            }
        })

        val releaseToolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        releaseToolbar.add(JButton("Build Android Release").apply { addActionListener { run(Action.RELEASE_ANDROID) } })
        releaseToolbar.add(JButton("Build iOS Release").apply { addActionListener { run(Action.RELEASE_IOS) } })
        releaseToolbar.add(JLabel("Environment:"))
        releaseToolbar.add(envCombo)

        val toolbars = JPanel(GridLayout(2, 1))
        toolbars.add(runToolbar)
        toolbars.add(releaseToolbar)

        add(toolbars, BorderLayout.NORTH)
        add(console.component, BorderLayout.CENTER)

        refreshProjectKind()
        refreshDevices()
        refreshEnvProfiles()
    }

    private fun run(action: Action) {
        val workDirectory = project.basePath ?: return
        val device = deviceCombo.selectedItem as? DeviceTarget
        val selectedEnv = envCombo.selectedItem as? String
        val envFile = selectedEnv?.takeIf { CommandPlan.usesEnvFile(kind) && it != NO_ENV_PROFILE }
        ReactNativeCommandRunner.run(workDirectory, CommandPlan.npxArguments(kind, action, device), console, envFile)
        // A real command was actually launched -- never fires for "no
        // project directory" above, which returns before reaching here.
        ReviewPrompt.recordHit(project)
    }

    private fun refreshProjectKind() {
        val packageJson = project.basePath?.let { File(it, "package.json") }?.takeIf { it.isFile }
        kind = ProjectKind.detect(packageJson?.readText())
        kindLabel.text = "Project: ${kind.displayName}"
        val envApplies = CommandPlan.usesEnvFile(kind)
        envCombo.isEnabled = envApplies
        envCombo.toolTipText = if (envApplies) {
            "Sets ENVFILE for react-native-config"
        } else {
            "Expo loads .env files itself (EXPO_PUBLIC_* variables); no ENVFILE needed"
        }
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
        val previouslySelected = deviceCombo.selectedItem as? DeviceTarget
        ApplicationManager.getApplication().executeOnPooledThread {
            val android = try {
                val output = CapturingProcessHandler(ReactNativeCommandRunner.runAdbDevices()).runProcess(5_000)
                DeviceOutputParser.parseAdbDevices(output.stdout).filter { it.isUsable }
                    .map { DeviceTarget(Platform.ANDROID, it.serial, it.serial) }
            } catch (e: Exception) {
                emptyList()
            }
            val ios = try {
                val output = CapturingProcessHandler(ReactNativeCommandRunner.runSimctlListDevices()).runProcess(5_000)
                DeviceOutputParser.parseSimctlDevices(output.stdout)
                    .map { DeviceTarget(Platform.IOS, it.udid, it.name) }
            } catch (e: Exception) {
                emptyList()
            }

            ApplicationManager.getApplication().invokeLater {
                deviceModel.removeAllElements()
                (android + ios).forEach { deviceModel.addElement(it) }
                if (previouslySelected != null && deviceModel.getIndexOf(previouslySelected) >= 0) {
                    deviceCombo.selectedItem = previouslySelected
                }
            }
        }
    }

    override fun dispose() {}
}
