<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# React Native Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Run `react-native run-android` / `run-ios` / `start` from a tool
  window via `OSProcessHandler` (IntelliJ's own async process API) so
  the IDE never freezes while a command runs — the leading paid
  incumbent has recent, repeated reports of exactly that.
- Android/iOS device and simulator picker, parsed directly from real
  `adb devices` / `xcrun simctl list devices` output.

### On hold for 0.2.0

One-click release bundling (`gradlew bundleRelease`/`assembleRelease`),
multi-environment variable profiles.
