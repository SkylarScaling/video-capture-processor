# video-capture-processor

A small JavaFX desktop app for sorting game-capture recordings. Point it at the folder your capture software writes to, and it moves the newest recording (plus any clip marks you made while it was recording) into your keepers or clips folder.

> **Status: work in progress.** This is a JavaFX rewrite of the older Swing app, [VideoCaptureProcessor](https://github.com/SkylarScaling/VideoCaptureProcessor), which remains the feature reference. Only part of that app has been ported so far (see [Status](#status)).

## Requirements

- JDK 21 or newer
- Maven 3.8+ (JavaFX 21 is pulled in as a Maven dependency; no separate JavaFX install is needed)

## Build and run

```
mvn clean package
java -jar target/video-capture-processor-1.0-SNAPSHOT-shaded.jar
```

Run the **shaded** jar. It starts through `VideoCaptureProcessorLauncher`, which avoids the JavaFX "missing runtime components" error you get when launching a fat jar via the `Application` class directly.

In Eclipse, run the shared `video-capture-processor.launch` configuration (Run → Run Configurations → Java Application → video-capture-processor). It starts `VideoCaptureProcessorLauncher` with the project folder as the working directory, so it uses the project's `CapProcConfig.xml`.

Run the tests with `mvn test`. To run one test: `mvn test -Dtest=FileMoveTaskTest`.

## Configuration

Open **Settings** in the app to choose:

- **Capture Folder**: where your capture software saves recordings
- **Keepers Folder**: where **Keepers** moves captures
- **Clips Folder**: where **Clips** moves captures
- **Always On Top**

Settings are saved to `CapProcConfig.xml` in the directory you launch the app from (it is gitignored). Start the app from the same directory each time, or it will create a fresh config.

## Status

The full checklist of what has and hasn't been ported from the Swing app is in [FEATURE_PARITY.md](FEATURE_PARITY.md). In short:

Working:

- Settings dialog (capture, keepers and clips folders, always on top)
- **Keepers** and **Clips** find the newest file in the capture folder and move it to the keepers or clips folder, along with other files that share its name and its `.clips` mark file, on a background thread. If a folder isn't set, the app tells you to configure it in Settings. Both buttons are disabled while a move runs, and problems are reported in a dialog and logged to the console.
- **Mark Clip** records how far into the current recording you are, so you can find the moment later. It reads the recording's start time from its filename, so capture files must be named `yyyy-MM-dd HH-mm-ss` (for example `2024-05-17 20-14-06.mp4`, the OBS filename format). Type an optional label first, and it is saved with the mark, as in `12:34 (nice shot)`. Marks are appended to `clips/<capture name>.clips` in the working directory, one per line, and move with the capture when you press Keepers or Clips.

- **Hotkeys:** **F1** presses Keepers, **F2** presses Clips and **F3** presses Mark Clip, anywhere in the main window (including while typing a label). A button flashes briefly whenever it is used. A hotkey does nothing while its button is disabled, for example during a file move.

Known issues and gaps:

- Not yet ported from the Swing app: cleanup, and the game selector and manager (adding, editing, reordering and persisting games).
- A multi-file move that fails partway is not rolled back, and a missing destination folder is not created.

## Project layout

```
src/main/java/com/wheezy/apps/vidcapproc/   application code (controllers, UI, data, utils)
src/main/java/com/wheezy/components/        reusable FXML controller/stage base classes
src/main/java/com/wheezy/utils/             reusable file, properties and dialog helpers
src/main/resources/.../ui/                  FXML screens
```

`com.wheezy.components` and `com.wheezy.utils` are deliberately kept in their original packages so they can be pulled out into a shared library for future apps.

## License

Public domain. See [LICENSE](LICENSE).
