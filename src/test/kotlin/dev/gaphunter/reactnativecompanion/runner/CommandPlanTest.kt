package dev.gaphunter.reactnativecompanion.runner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandPlanTest {

    private val pixel = DeviceTarget(Platform.ANDROID, "emulator-5554", "emulator-5554")
    private val iphone = DeviceTarget(Platform.IOS, "87654321-4321-4321-4321-210987654321", "iPhone 15 Pro")

    private fun rn(action: Action, device: DeviceTarget? = null) =
        CommandPlan.npxArguments(ProjectKind.REACT_NATIVE_CLI, action, device)

    private fun expo(action: Action, device: DeviceTarget? = null) =
        CommandPlan.npxArguments(ProjectKind.EXPO, action, device)

    @Test
    fun reactNativeCliCommandsAreUnchangedWithoutADevice() {
        assertEquals(listOf("react-native", "run-android"), rn(Action.RUN_ANDROID))
        assertEquals(listOf("react-native", "run-ios"), rn(Action.RUN_IOS))
        assertEquals(listOf("react-native", "start"), rn(Action.START))
        assertEquals(listOf("react-native", "build-android", "--mode=release"), rn(Action.RELEASE_ANDROID))
        assertEquals(listOf("react-native", "build-ios", "--mode=Release"), rn(Action.RELEASE_IOS))
    }

    @Test
    fun theSelectedDeviceReachesTheReactNativeCli() {
        assertEquals(listOf("react-native", "run-android", "--deviceId", "emulator-5554"), rn(Action.RUN_ANDROID, pixel))
        assertEquals(listOf("react-native", "run-ios", "--udid", iphone.id), rn(Action.RUN_IOS, iphone))
    }

    @Test
    fun expoProjectsUseTheExpoCli() {
        assertEquals(listOf("expo", "run:android"), expo(Action.RUN_ANDROID))
        assertEquals(listOf("expo", "run:ios", "--device", iphone.id), expo(Action.RUN_IOS, iphone))
        assertEquals(listOf("expo", "start"), expo(Action.START))
        assertEquals(listOf("expo", "run:android", "--variant", "release", "--device", "emulator-5554"),
            expo(Action.RELEASE_ANDROID, pixel))
        assertEquals(listOf("expo", "run:ios", "--configuration", "Release"), expo(Action.RELEASE_IOS))
    }

    @Test
    fun aDeviceFromTheOtherPlatformIsNeverPassed() {
        assertEquals(listOf("react-native", "run-android"), rn(Action.RUN_ANDROID, iphone))
        assertEquals(listOf("expo", "run:ios"), expo(Action.RUN_IOS, pixel))
        assertEquals(listOf("expo", "start"), expo(Action.START, pixel))
    }

    @Test
    fun aSimulatorNameWithSpacesStaysOneArgument() {
        // The id goes as a single list element -- never split by a shell.
        val args = rn(Action.RUN_IOS, iphone)
        assertEquals(4, args.size)
        assertEquals("iPhone 15 Pro", iphone.name)
    }

    @Test
    fun detectsExpoFromItsDependency() {
        assertEquals(ProjectKind.EXPO, ProjectKind.detect("""{"dependencies": {"expo": "~51.0.0", "react-native": "0.74.1"}}"""))
        assertEquals(ProjectKind.EXPO, ProjectKind.detect("{\n  \"devDependencies\": {\n    \"expo\" : \"^52.0.0\"\n  }\n}"))
        assertEquals(ProjectKind.REACT_NATIVE_CLI, ProjectKind.detect("""{"dependencies": {"react-native": "0.74.1"}}"""))
        assertEquals(ProjectKind.REACT_NATIVE_CLI, ProjectKind.detect(null))
    }

    @Test
    fun expoConfigThatIsNotADependencyDoesNotCount() {
        // app.json-style `"expo": { ... }` and packages that only start with "expo".
        assertEquals(ProjectKind.REACT_NATIVE_CLI, ProjectKind.detect("""{"expo": {"name": "demo"}}"""))
        assertEquals(ProjectKind.REACT_NATIVE_CLI, ProjectKind.detect("""{"dependencies": {"expo-camera": "~15.0.0"}}"""))
    }

    @Test
    fun envFileOnlyAppliesToReactNativeConfig() {
        assertTrue(CommandPlan.usesEnvFile(ProjectKind.REACT_NATIVE_CLI))
        assertFalse(CommandPlan.usesEnvFile(ProjectKind.EXPO))
    }

    @Test
    fun devicesAreLabelledByPlatform() {
        assertEquals("Android: emulator-5554", pixel.toString())
        assertEquals("iOS: iPhone 15 Pro", iphone.toString())
    }
}
