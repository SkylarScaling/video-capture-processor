# video-capture-processor

A small JavaFX desktop app for sorting game-capture recordings. Point it at the folder your capture software writes to, and it moves the newest recording (plus its matching `.clips` marker file) into the folder you choose.

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

Run the tests with `mvn test`. To run one test: `mvn test -Dtest=FileMoveTaskTest`.

## Configuration

Open **Settings** in the app to choose:

- **Capture Folder**: where your capture software saves recordings
- **Keepers Folder**: where keepers are meant to go (not used yet)
- **Clips Folder**: where **Save...** moves captures, and where it looks for the matching `.clips` marker file
- **Always On Top**

Settings are saved to `CapProcConfig.xml` in the directory you launch the app from (it is gitignored). Start the app from the same directory each time, or it will create a fresh config.

## Status

Working:

- Settings dialog (capture, keepers and clips folders, always on top)
- **Save...** finds the newest file in the capture folder and moves it to the clips folder, along with other files that share its name and the matching `.clips` file, on a background thread. If the capture or clips folder isn't set, it tells you to configure them in Settings. The button is disabled while the move runs, and problems are reported in a dialog and logged to the console.

Known issues and gaps:

- Not yet ported from the Swing app: keepers/clips buttons, marking clips, cleanup, hotkeys, and the game selector and manager (adding, editing, reordering and persisting games).
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
