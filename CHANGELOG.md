<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# React Native Companion Changelog

## [Unreleased]

## [0.2.0]

### Added

- One-click release bundling: "Build Android Release" runs
  `react-native build-android --mode=release`, "Build iOS Release" runs
  `react-native build-ios --mode=Release` — the real react-native CLI
  bundling commands, same `OSProcessHandler` execution path as
  run-android/run-ios/start.
- Multi-environment variable profiles: an "Environment" dropdown
  auto-discovers `.env*` files at the project root (the real
  `react-native-config` convention) and sets `ENVFILE` for every
  command run from the tool window when a profile other than "(none)"
  is selected.

## [0.1.2]

### Changed

- Added a strict local `verifyPlugin` gate (catches
  `@ApiStatus.OverrideOnly`/`Internal`/`Experimental` API usage and
  compatibility problems before Marketplace's own verifier would) — no
  user-visible change, confirmed passing clean against all 6 target IDEs.

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

[Unreleased]: https://github.com/GapHunterLabs/react-native-companion/compare/0.2.0...HEAD
[0.2.0]: https://github.com/GapHunterLabs/react-native-companion/compare/0.1.2...0.2.0
[0.1.2]: https://github.com/GapHunterLabs/react-native-companion/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/GapHunterLabs/react-native-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/react-native-companion/commits/0.1.0
