package dev.gaphunter.reactnativecompanion.runner

/**
 * Which toolchain the project uses. React Native's own docs recommend
 * starting new apps with a framework -- Expo -- and there `react-native
 * run-android` isn't the command: `npx expo run:android` is.
 */
enum class ProjectKind(val displayName: String) {
    REACT_NATIVE_CLI("React Native CLI"),
    EXPO("Expo");

    companion object {
        // `"expo": "~51.0.0"` as a dependency. A plain `"expo" :` key alone
        // isn't enough: app.json-style config (`"expo": { ... }`) isn't a
        // dependency.
        private val EXPO_DEPENDENCY = Regex(""""expo"\s*:\s*"""")

        fun detect(packageJson: String?): ProjectKind =
            if (packageJson != null && EXPO_DEPENDENCY.containsMatchIn(packageJson)) EXPO else REACT_NATIVE_CLI
    }
}

enum class Platform { ANDROID, IOS }

/** A device from `adb devices` / `xcrun simctl`, as shown in the picker. */
data class DeviceTarget(val platform: Platform, val id: String, val name: String) {
    override fun toString(): String = when (platform) {
        Platform.ANDROID -> "Android: $name"
        Platform.IOS -> "iOS: $name"
    }
}

enum class Action(val platform: Platform?) {
    RUN_ANDROID(Platform.ANDROID),
    RUN_IOS(Platform.IOS),
    START(null),
    RELEASE_ANDROID(Platform.ANDROID),
    RELEASE_IOS(Platform.IOS),
}

/**
 * The exact `npx` arguments for an action. Flags come from each CLI's own
 * docs:
 * - React Native CLI: `run-android --deviceId <adb serial>` (documented as
 *   deprecated in favour of `--device <name>`, but still the only flag that
 *   takes the serial `adb devices` prints, and understood by older CLIs
 *   too); `run-ios --udid <udid>`; `build-android --mode=release`,
 *   `build-ios --mode=Release`.
 * - Expo CLI: `run:android|run:ios --device <name or ID>`;
 *   `run:android --variant release`, `run:ios --configuration Release`
 *   (local release builds -- no EAS account involved).
 *
 * A selected device only applies to an action on its own platform: an iOS
 * simulator selected while pressing "Run Android" is ignored, not passed
 * to the Android CLI.
 */
object CommandPlan {
    fun npxArguments(kind: ProjectKind, action: Action, device: DeviceTarget?): List<String> {
        val target = device?.takeIf { it.platform == action.platform }
        return when (kind) {
            ProjectKind.REACT_NATIVE_CLI -> when (action) {
                Action.RUN_ANDROID -> listOf("react-native", "run-android") + deviceFlag("--deviceId", target)
                Action.RUN_IOS -> listOf("react-native", "run-ios") + deviceFlag("--udid", target)
                Action.START -> listOf("react-native", "start")
                Action.RELEASE_ANDROID -> listOf("react-native", "build-android", "--mode=release")
                Action.RELEASE_IOS -> listOf("react-native", "build-ios", "--mode=Release")
            }
            ProjectKind.EXPO -> when (action) {
                Action.RUN_ANDROID -> listOf("expo", "run:android") + deviceFlag("--device", target)
                Action.RUN_IOS -> listOf("expo", "run:ios") + deviceFlag("--device", target)
                Action.START -> listOf("expo", "start")
                Action.RELEASE_ANDROID -> listOf("expo", "run:android", "--variant", "release") + deviceFlag("--device", target)
                Action.RELEASE_IOS -> listOf("expo", "run:ios", "--configuration", "Release") + deviceFlag("--device", target)
            }
        }
    }

    /** ENVFILE is react-native-config's convention; Expo loads `.env*` files itself. */
    fun usesEnvFile(kind: ProjectKind): Boolean = kind == ProjectKind.REACT_NATIVE_CLI

    private fun deviceFlag(flag: String, device: DeviceTarget?): List<String> =
        if (device == null) emptyList() else listOf(flag, device.id)
}
