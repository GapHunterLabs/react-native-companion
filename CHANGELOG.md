<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# React Native Companion Changelog

## [Unreleased]

### Changed

- `PRIVACY.md` describes the values the plugin keeps in the IDE's local
  settings.

## [0.3.1]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.3.0]

### Added

- Expo support. The toolchain is detected from `package.json`: in an Expo
  project the same buttons run `expo run:android` / `expo run:ios` /
  `expo start`, and the release buttons run the local release builds
  `expo run:android --variant release` / `expo run:ios --configuration
  Release` (no EAS account involved). React Native CLI projects behave as
  before. The detected toolchain is shown in the tool window.

### Fixed

- The device picker didn't do anything: the device selected in the
  dropdown was never passed to the run command, so the app always started
  on the default device. It now goes to `run-android --deviceId <serial>` /
  `run-ios --udid <udid>` (or `--device <id>` for Expo), and only to the
  command for its own platform.
- The plugin description still said release bundling and environment
  profiles were "coming in a future release"; both shipped in 0.2.0. The
  description now lists what the plugin does.

### Changed

- "Start Metro" is now "Start Dev Server" (it starts `expo start` in an
  Expo project), and the separate "Refresh Devices" / "Refresh
  Environments" buttons are a single "Refresh" that also re-detects the
  toolchain.

## [0.2.3]

### Added

- Review/star CTA: after 5 commands launched from the tool window (Run
  Android/iOS, Start Metro, Build releases), a one-time notification
  asks whether to rate the plugin on Marketplace, with a permanent
  "Don't ask again" option.

## [0.2.2]

### Fixed

- Tool window no longer shows the generic platform icon in the sidebar —
  the real Gap Hunter Labs mark is now declared via `icon=` on
  `<toolWindow>`.

## [0.2.1]

### Fixed

- Tool window content (run/release toolbars, console) was rendering
  flush against the tool window's own border, with no margin — fixed
  with an 8px empty border on the root panel.

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

[Unreleased]: https://github.com/GapHunterLabs/react-native-companion/compare/0.3.1...HEAD
[0.3.1]: https://github.com/GapHunterLabs/react-native-companion/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/GapHunterLabs/react-native-companion/compare/0.2.3...0.3.0
[0.2.3]: https://github.com/GapHunterLabs/react-native-companion/compare/0.2.2...0.2.3
[0.2.2]: https://github.com/GapHunterLabs/react-native-companion/compare/0.2.1...0.2.2
[0.2.1]: https://github.com/GapHunterLabs/react-native-companion/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/GapHunterLabs/react-native-companion/compare/0.1.2...0.2.0
[0.1.2]: https://github.com/GapHunterLabs/react-native-companion/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/GapHunterLabs/react-native-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/react-native-companion/commits/0.1.0
