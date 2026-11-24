# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A JavaFX (Java 21, JavaFX 21) desktop app that moves the newest game-capture video (and its matching `.clips` file) into a "keepers" or "clips" folder. It is an unfinished port of the Swing app in the sibling repo `../VideoCaptureProcessor`, which is the **feature reference**: choosing the current game in the game selector (and so the game label in file names) is not ported yet. Keepers, Clips, Mark Clip, Cleanup, hotkeys, the filename label and the game manager/editor are ported.

`FEATURE_PARITY.md` is the checklist of Swing features (with IDs like A1, C2) and the place to record decisions to change a feature before porting it. When you finish or change a feature, tick or update its entry in the same commit.

## Commands

```powershell
mvn clean package                      # compile, run tests, build jars
mvn test                               # tests only
mvn test -Dtest=FileMoveTaskTest#reportsMissingFilesWithoutThrowing   # single test
java -p "target/video-capture-processor-1.0-SNAPSHOT.jar;target/modules" -m com.wheezy.apps.vidcapproc/com.wheezy.apps.vidcapproc.VideoCaptureProcessor
```

- The app is a single **open** Java module (`src/main/java/module-info.java`) and must run on the module path; the old shaded jar and `VideoCaptureProcessorLauncher` are gone, since a classpath launch makes JavaFX print "Unsupported JavaFX configuration". `mvn package` copies the runtime jars (Windows JavaFX jars only; plain `javafx-*.jar` stubs would clash) into `target/modules`. A new `requires` is needed in `module-info.java` for any new JDK or library module. Surefire runs the tests on the module path too.
- No linter or formatter is configured. Tests are JUnit 4.
- `mvn package` regenerates `dependency-reduced-pom.xml` (gitignored); delete it if it shows up.

## Architecture

- **FXML + controller per screen.** FXML files live in `src/main/resources/com/wheezy/apps/vidcapproc/ui/`; their paths are constants in `VideoCaptureProcessorResources`. Open a screen with `new VideoCaptureProcessorStage(<fxml constant>, title, modal)`, which loads the FXML and injects the stage into the controller. Controllers extend `FXMLController` and are wired via `fx:controller` in the FXML. A broken FXML only prints a stack trace in the `VideoCaptureProcessorStage` constructor.
- **Clip marks** (`ClipMarker`, called via `VideoCaptureProcessorUtils.markClip`): the time since the *newest capture file* started, parsed from its filename (`yyyy-MM-dd HH-mm-ss`), is appended as a line to `clips/<capture name>.clips` in the working directory. That `clips/` directory is separate from the user-configured "Clips Folder" setting, which is only a *destination* for the Clips button. `FileMoveTask` moves the `.clips` mark file along with the capture. Keep the time logic in `ClipMarker` (unit-tested, takes `now` as a parameter).
- **The save flow** (`VideoCaptureProcessorController.saveKeeper`/`saveClip` → `VideoCaptureProcessorUtils.saveNewestFile` → `FileMoveTask`): the utils method reads settings and validates them on the FX thread, finds the newest file in the capture dir, and starts a `FileMoveTask` on a non-daemon thread. `FileMoveTask` must stay UI-free: expected problems come back as `Result(code, detail)`, I/O errors are thrown, and the succeeded/failed handlers in the utils class show the dialogs on the FX thread. Never show an `Alert` from `call()`. New file names come from `CaptureNaming.buildName(gameLabel, filenameLabel, originalName)`; the game label is the selected game's (`GameSelection.gameLabel()`), or `null` with no game selected. `FileMoveTask.moveCapture` is the shared, static move logic.
- **Cleanup** (`VideoCaptureProcessorUtils.cleanupCaptureDirectory` → `FileCleanupTask`): every question (confirm, then "Save Clips First / Delete Everything / Cancel" if marks exist) is asked on the FX thread *before* the task starts, so the task never prompts. It deletes permanently on purpose (the goal is freeing disk space), never touches subdirectories, and if saving marked clips fails it deletes nothing. The controller's `trackTask` disables the action buttons while a move or cleanup runs, so the two can't overlap.
- **Settings** are `java.util.Properties` stored as XML in `CapProcConfig.xml`, in the *current working directory* (gitignored). Keys come from the `VideoCaptureProcessorProperty` enum, accessed through the singleton `VideoCaptureProcessorProperties`. Tests must call `VideoCaptureProcessorProperties.setInstance(...)` with a temp file, or load the main window, so they never read or write the real `CapProcConfig.xml`; `FxTestSupport.useTemporaryAppData(dir)` does this for the settings, games and selection together.
- **Games** (`Game`, `GameCollection`, `GameCatalog`): `Game` is a JAXB bean (`jakarta.xml.bind`, glassfish runtime), and `GameCollection` is the root element of `SavedGames.xml`, in the *same format as the Swing app* (`gameCollection` > repeated `gameSet`), so a Swing file loads unchanged. `GameCatalog` (singleton via `getInstance()`) is the single owner of the list: it is a read-only `ObservableList` that screens listen to, every mutator saves immediately and **undoes itself if the save fails**, and titles are unique ignoring case. An unreadable file is copied to `.unreadable` rather than overwritten. Tests must call `GameCatalog.setInstance(tempCatalog)` so they never touch a real `SavedGames.xml`.
- **Game manager** (`GameManagerController` + `GamePanel` rows, `GameEditorController`): rows only report what the user asked for through `GamePanel.Actions`; the controller applies it to the catalog and rebuilds the rows from the catalog's change events. `GameValidation` holds the title and filename-label rules. Game icons (`GameIcons`) are scaled to fit 64×64, stored under `images/` with a path *relative to the working directory*, and any missing or unreadable icon file falls back to the bundled default instead of failing. Nodes inside a `ScrollPane` cannot be found with `lookup()` until the pane is skinned; in tests use `getContent()`.
- **Game selector** (`GameSelection` + `GameSelectorPanel`): `GameSelection` holds the selected game as an observable property, remembers its title in `SelectedGame`, follows the game when it is edited or renamed in the manager, and clears if it is deleted. `GameSelectorPanel` is a collapsible panel on the right of the main window (the main FXML's root is an `HBox`: the controls, then the panel). The panel only reports `expandedProperty()`; `VideoCaptureProcessorController` saves `GameSelectorShow` and calls `stage.sizeToScene()` (then keeps the window on screen) so the window widens and narrows with it. The game button on the main window toggles the panel and always shows the selected game's icon. Create `GameSelection.getInstance()` only after the properties are loaded, since it restores the remembered game from them.
- **`com.wheezy.components` and `com.wheezy.utils`** are copies of classes from the author's `java-components` and `java-utils` repos, inlined on purpose and kept in their original packages so they can be extracted into a shared library later. Don't rename those packages. The app has no dependency on those repos.

## Gotchas

- Source files use CRLF line endings; keep them when editing.
- `.classpath`, `.project`, `.settings` and `.idea` are gitignored (Eclipse/IntelliJ project files are local only).
- `FileUtility.moveFiles` does not create a missing destination directory for a file move (it returns `INVALID_COPY_LOCATION`), and a multi-file move that fails midway is not rolled back.
