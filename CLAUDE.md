# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A JavaFX (Java 21, JavaFX 21) desktop app that moves the newest game-capture video (and its matching `.clips` file) into a "keepers" or "clips" folder. It is an unfinished port of the Swing app in the sibling repo `../VideoCaptureProcessor`, which is the **feature reference**: cleanup, hotkeys, the game manager/editor, and the XML-backed game collection are not ported yet (`cleanupFiles` in the main controller is a test stub). Keepers, Clips and Mark Clip are ported.

`FEATURE_PARITY.md` is the checklist of Swing features (with IDs like A1, C2) and the place to record decisions to change a feature before porting it. When you finish or change a feature, tick or update its entry in the same commit.

## Commands

```powershell
mvn clean package                      # compile, run tests, build jars
mvn test                               # tests only
mvn test -Dtest=FileMoveTaskTest#reportsMissingFilesWithoutThrowing   # single test
java -jar target/video-capture-processor-1.0-SNAPSHOT-shaded.jar      # run the app
```

- Always launch the **shaded** jar (main class `VideoCaptureProcessorLauncher`); launching `VideoCaptureProcessor` directly from a fat jar fails with the JavaFX "missing runtime components" error. The "Unsupported JavaFX configuration" warning on startup is expected.
- No linter or formatter is configured. Tests are JUnit 4.
- `mvn package` regenerates `dependency-reduced-pom.xml` (gitignored); delete it if it shows up.

## Architecture

- **FXML + controller per screen.** FXML files live in `src/main/resources/com/wheezy/apps/vidcapproc/ui/`; their paths are constants in `VideoCaptureProcessorResources`. Open a screen with `new VideoCaptureProcessorStage(<fxml constant>, title, modal)`, which loads the FXML and injects the stage into the controller. Controllers extend `FXMLController` and are wired via `fx:controller` in the FXML. A broken FXML only prints a stack trace in the `VideoCaptureProcessorStage` constructor.
- **Clip marks** (`ClipMarker`, called via `VideoCaptureProcessorUtils.markClip`): the time since the *newest capture file* started, parsed from its filename (`yyyy-MM-dd HH-mm-ss`), is appended as a line to `clips/<capture name>.clips` in the working directory. That `clips/` directory is separate from the user-configured "Clips Folder" setting, which is only a *destination* for the Clips button. `FileMoveTask` moves the `.clips` mark file along with the capture. Keep the time logic in `ClipMarker` (unit-tested, takes `now` as a parameter).
- **The save flow** (`VideoCaptureProcessorController.saveKeeper`/`saveClip` → `VideoCaptureProcessorUtils.saveNewestFile` → `FileMoveTask`): the utils method reads settings and validates them on the FX thread, finds the newest file in the capture dir, and starts a `FileMoveTask` on a non-daemon thread. `FileMoveTask` must stay UI-free: expected problems come back as `Result(code, detail)`, I/O errors are thrown, and the succeeded/failed handlers in the utils class show the dialogs on the FX thread. Never show an `Alert` from `call()`.
- **Settings** are `java.util.Properties` stored as XML in `CapProcConfig.xml`, in the *current working directory* (gitignored). Keys come from the `VideoCaptureProcessorProperty` enum, accessed through the singleton `VideoCaptureProcessorProperties`. The underlying `PropertiesUtility` keeps its `Properties` object **static**, so all instances share state.
- **`Game`** is a JAXB bean (`jakarta.xml.bind`, with the glassfish runtime). Game persistence is not wired up yet.
- **`com.wheezy.components` and `com.wheezy.utils`** are copies of classes from the author's `java-components` and `java-utils` repos, inlined on purpose and kept in their original packages so they can be extracted into a shared library later. Don't rename those packages. The app has no dependency on those repos.

## Gotchas

- Source files use CRLF line endings; keep them when editing.
- `.classpath`, `.project`, `.settings` and `.idea` are gitignored (Eclipse/IntelliJ project files are local only).
- `FileUtility.moveFiles` does not create a missing destination directory for a file move (it returns `INVALID_COPY_LOCATION`), and a multi-file move that fails midway is not rolled back.
