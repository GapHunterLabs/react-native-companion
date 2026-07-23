package dev.gaphunter.reactnativecompanion.runner

/**
 * Verdict's own complaint list (out/verdict_console.md): the paid
 * incumbent's iOS simulator/Android emulator integration is "full of
 * bugs", buttons that "mostly don't work". Parsing the real output of
 * `adb devices` and `xcrun simctl list devices` ourselves, rather than
 * whatever the incumbent does, is the actual fix -- kept as pure text
 * parsing (no process execution here) so it's testable without a real
 * adb/Xcode install.
 */
data class AndroidDevice(val serial: String, val state: String) {
    val isUsable: Boolean get() = state == "device"
}

data class IosSimulator(val udid: String, val name: String, val state: String) {
    val isBooted: Boolean get() = state == "Booted"
}

object DeviceOutputParser {

    /**
     * `adb devices` output looks like:
     * ```
     * List of devices attached
     * emulator-5554	device
     * R58M12ABCDE	unauthorized
     * ```
     */
    fun parseAdbDevices(output: String): List<AndroidDevice> {
        return output.lineSequence()
            .drop(1) // "List of devices attached" header
            .mapNotNull { line ->
                val parts = line.trim().split(Regex("\\s+"))
                if (parts.size < 2 || parts[0].isEmpty()) null else AndroidDevice(parts[0], parts[1])
            }
            .toList()
    }

    /**
     * `xcrun simctl list devices` output looks like:
     * ```
     * == Devices ==
     * -- iOS 17.0 --
     *     iPhone 15 (12345678-1234-1234-1234-123456789012) (Shutdown)
     *     iPhone 15 Pro (87654321-4321-4321-4321-210987654321) (Booted)
     * -- tvOS 17.0 --
     *     Apple TV (ABCDEF12-3456-7890-ABCD-EF1234567890) (Shutdown)
     * ```
     * Section headers (`== ... ==`, `-- ... --`) don't match the device
     * line shape and are skipped naturally rather than special-cased.
     */
    private val SIMCTL_DEVICE_LINE = Regex("""^\s*(.+?)\s+\(([0-9A-Fa-f-]{36})\)\s+\((\w+)\)\s*$""")

    fun parseSimctlDevices(output: String): List<IosSimulator> {
        return output.lineSequence()
            .mapNotNull { line ->
                SIMCTL_DEVICE_LINE.matchEntire(line)?.let {
                    IosSimulator(udid = it.groupValues[2], name = it.groupValues[1], state = it.groupValues[3])
                }
            }
            .toList()
    }
}
