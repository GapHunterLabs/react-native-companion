# React Native Companion

IntelliJ/WebStorm/PhpStorm plugin. Run React Native commands
(`run-android`, `run-ios`, Metro) from a tool window without freezing
the IDE.

## Why it exists

Born from real evidence in JetBrains Marketplace reviews, not
assumptions: the leading paid alternative in this space (React Native
Console, ~430K downloads, $19-24/year) has a recent, severe, reproducible
complaint — "severely impacting IDE performance... frequently becomes
unresponsive... thread dumps are required" — plus older reports of buggy
iOS simulator integration and unreliable buttons. The same vendor's own
free tier is rated higher than their paid one, suggesting the problem is
implementation quality in the paid tier's extra features, not something
inherent to the feature set.

## Why built this way

IntelliJ's platform already ships `OSProcessHandler`, an async process
API that reads a spawned process's I/O on background threads by
construction. Using it (instead of e.g. blocking `Runtime.exec().waitFor()`
calls on the UI thread) isn't an optimization here — it's the direct fix
for the incumbent's #1 complaint. Same for the device/simulator picker:
it parses the real, unmodified output of `adb devices` and
`xcrun simctl list devices` rather than wrapping some other layer that
could itself introduce bugs.

## Usage

Open the **React Native** tool window (bottom of the IDE) → **Run
Android** / **Run iOS** / **Start Metro**, or pick a specific device
from the dropdown after **Refresh Devices**.

## What's next

One-click release bundling and multi-environment variable profiles
aren't built yet.

## Enterprise / Team Licensing

Need enterprise features, custom build/run configurations, or team
licensing? Contact us at **kennyj.diazm@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin     # generates build/distributions/*.zip
./gradlew verifyPlugin    # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
