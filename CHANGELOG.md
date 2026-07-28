<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# React Native Companion Changelog

## [Unreleased]

## [0.1.1]

### Added

- Gap Hunter Labs brand icon (`pluginIcon.svg` / `pluginIcon_dark.svg`).

## [0.1.0]

### Added

- Run `react-native run-android` / `run-ios` / `start` from a tool
  window via `OSProcessHandler` (IntelliJ's own async process API) so
  the IDE never freezes while a command runs — the leading paid
  incumbent has recent, repeated reports of exactly that.
- Android/iOS device and simulator picker, parsed directly from real
  `adb devices` / `xcrun simctl list devices` output.

[Unreleased]: https://github.com/GapHunterLabs/react-native-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/react-native-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/react-native-companion/commits/0.1.0
