# Rubber Duck — JetBrains IDE plugin

A rubber duck lives in the **empty whitespace of your editor** and reacts with its "face" to
the state of your project. It breathes while idle, panics on a failed build, calms down on a
clean one, and quietly **nods** when you tell it what's wrong. Zero usefulness, maximum delight —
that's the point.

The duck never reads, analyses, or answers your problem. Saying it out loud is what helps. This
is rubber-duck debugging, made literal.

## Stack

- Kotlin, JDK 21
- IntelliJ Platform Gradle Plugin **2.x** (`org.jetbrains.intellij.platform`)
- Target IDE: IntelliJ IDEA Community — version pinned in `gradle.properties` (`platformVersion`)
- Swing UI; new UI supported

## Architecture (three independent modules around one service)

`DuckMoodService` (project-level light service) holds the current `Mood`
(`IDLE | HAPPY | PANIC | NOD`). Module 2 writes it; Modules 1 & 3 read it and repaint. Mood
changes are published on the project message bus, always on the EDT.

- **Module 1 — where it lives** (`overlay/`): `DuckOverlay` paints the duck on the IDE glass
  pane via a `Painter` bound to `editor.contentComponent`, so geometry is in editor coordinates
  and clipping is free. It anchors in the empty strip under the last line, recomputes on
  scroll/resize/typing (debounced via `Alarm`) and **glides** to the new spot. `DuckClickArea`
  is a transparent, sprite-sized component that catches clicks only inside the duck silhouette.
- **Module 2 — what sets the mood** (`mood/`): `CompilationMoodSource` (errors → PANIC, clean →
  HAPPY → IDLE) and `WolfMoodSource` (polls `WolfTheProblemSolver` for live problems → PANIC/IDLE).
- **Module 3 — the ritual** (`ritual/`): `TellTheDuck` shows a light popup with a big duck and a
  text box; the duck NODs as you type and lets out a silent "Quack." The text is discarded.

The duck art is drawn procedurally (`render/DuckRenderer`) — HiDPI-perfect placeholders with no
asset loading. Swap that one class for an image-backed renderer when real sprites arrive.

## Build & run

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home

./gradlew runIde        # launch a sandbox IDE with the plugin
./gradlew buildPlugin   # produce build/distributions/rubber-duck-*.zip
```

### Try it

1. Open a short `.kt`/`.java` file — the duck sits in the empty space under the code, breathing.
2. Break the syntax and run a build (Build → Build Project) — the duck panics.
3. Click the duck (or press **Ctrl+Alt+D** → *Tell the Duck*) and type — the duck nods, then quacks.
4. There's a temporary debug action **Rubber Duck: Cycle Mood (debug)** to flip moods by hand.

Bump the target IDE by editing `platformVersion` in `gradle.properties`.
