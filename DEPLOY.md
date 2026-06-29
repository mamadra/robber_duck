# Deploy to JetBrains Marketplace

Plugin: **Rubber Duck Buddy** — id `com.mamadra.rubberduck`.

## 0. One-time prerequisites

- JDK 21 (`export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home`).
- A JetBrains account: https://plugins.jetbrains.com/

## 1. (Recommended) Verify compatibility

Runs the JetBrains Plugin Verifier against compatible IDEs (downloads them on first run — can be
large/slow):

```bash
./gradlew verifyPlugin
```

Fix any **errors** it reports (warnings are usually fine for a toy plugin).

## 2. Build the distribution

```bash
./gradlew clean buildPlugin
# → build/distributions/rubber-duck-0.0.1.zip
```

## 3. First release — upload via the website

A brand-new plugin's **first** version must be uploaded through the web UI (you accept the
agreement and the listing gets created + reviewed):

1. https://plugins.jetbrains.com/plugin/add
2. Upload `build/distributions/rubber-duck-0.0.1.zip`.
3. Fill in: category (e.g. *UI / Editor*), license (**MIT**, see `LICENSE`), tags, screenshots.
   - Good screenshots: the duck idle in the editor corner, the panic face on a broken build, the
     "Tell the Duck" popup mid-nod.
4. Submit. JetBrains **signs** the plugin on their side and reviews it (usually a day or two).

> Signing is handled by JetBrains — we upload an unsigned zip. No certificate needed on our end.

## 4. Later releases — publish from the CLI

Once the listing exists, bump `pluginVersion` in `gradle.properties`, update `<change-notes>` in
`plugin.xml`, then:

```bash
# Marketplace → your profile → My Tokens → generate a token
export PUBLISH_TOKEN=xxxxxxxx
./gradlew publishPlugin
```

Use a pre-release channel by uncommenting `channels = listOf("beta")` in `build.gradle.kts`
(users then opt in via a custom repository URL).

## Notes

- **Compatibility:** `since-build=243` (IntelliJ 2024.3+), open-ended `until-build`. The plugin
  depends on the bundled **Java** plugin (for the compilation listener), so it targets IntelliJ
  IDEA (Community/Ultimate) and other IDEs that ship the Java plugin.
- **Privacy:** the plugin sends nothing anywhere and stores no data; the "tell the duck" text is
  discarded. Handy to state this in the Marketplace description's privacy section.
- **Debug actions** (force-mood / gallery) are commented out in `plugin.xml` and excluded from
  releases. Re-enable that block for local development.
