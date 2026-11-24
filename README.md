# video-capture-processor

A small JavaFX desktop app for sorting game-capture recordings. Point it at the folder your capture software writes to, and it moves the newest recording (plus any clip marks you made while it was recording) into your keepers or clips folder.

> **Status: work in progress.** This is a JavaFX rewrite of the older Swing app, [VideoCaptureProcessor](https://github.com/SkylarScaling/VideoCaptureProcessor), which remains the feature reference. Only part of that app has been ported so far (see [Status](#status)).

## Requirements

- JDK 21 or newer
- Maven 3.8+ (JavaFX 21 is pulled in as a Maven dependency; no separate JavaFX install is needed)

## Build and run

```
mvn clean package
java -p "target/video-capture-processor-1.0-SNAPSHOT.jar;target/modules" -m com.wheezy.apps.vidcapproc/com.wheezy.apps.vidcapproc.VideoCaptureProcessor
```

The app is a Java module (`module-info.java`) and runs on the **module path**, which is how JavaFX is supported. `mvn package` copies the runtime dependencies into `target/modules`, so run the command above from the project folder. The JavaFX jars copied there are the Windows ones; on another OS, change the `win` classifier in `pom.xml`.

In Eclipse, run the shared `video-capture-processor.launch` configuration (Run → Run Configurations → Java Application → video-capture-processor). It starts `VideoCaptureProcessor` with the project folder as the working directory, so it uses the project's `CapProcConfig.xml`.

Run the tests with `mvn test`. To run one test: `mvn test -Dtest=FileMoveTaskTest`.

## Configuration

Open **Settings** in the app to choose:

- **Capture Folder**: where your capture software saves recordings
- **Keepers Folder**: where **Keepers** moves captures
- **Clips Folder**: where **Clips** moves captures
- **Always On Top**

Settings are saved to `CapProcConfig.xml` in the directory you launch the app from (it is gitignored). Start the app from the same directory each time, or it will create a fresh config.

The same directory also holds the rest of the app's data, all gitignored: `SavedGames.xml` (your games), `images/` (game icons), and `clips/` (clip marks). If you used the Swing app, copy its `SavedGames.xml` into this directory and your games appear in the game manager.

## Status

The full checklist of what has and hasn't been ported from the Swing app is in [FEATURE_PARITY.md](FEATURE_PARITY.md). In short:

Working:

- Settings dialog (capture, keepers and clips folders, always on top)
- **Keepers** and **Clips** find the newest file in the capture folder and move it to the keepers or clips folder, along with other files that share its name and its `.clips` mark file, on a background thread. If a folder isn't set, the app tells you to configure it in Settings. Both buttons are disabled while a move runs, and problems are reported in a dialog and logged to the console.
- **Mark Clip** records how far into the current recording you are, so you can find the moment later. It reads the recording's start time from its filename, so capture files must be named `yyyy-MM-dd HH-mm-ss` (for example `2024-05-17 20-14-06.mp4`, the OBS filename format). Type an optional label first, and it is saved with the mark, as in `12:34 (nice shot)`. Marks are appended to `clips/<capture name>.clips` in the working directory, one per line, and move with the capture when you press Keepers or Clips.
- **Filename label** (the box under Keepers): text typed there is added to the front of the saved file names by both Keepers and Clips, so `2024-05-17 20-14-06.mp4` becomes `ace_2024-05-17 20-14-06.mp4`. Characters that aren't safe in filenames become underscores, and the box is cleared after a successful move. It is separate from the Mark Clip label.
- **Cleanup** (the trash button) deletes every file in the capture folder, permanently, after asking you to confirm (it shows the folder and the number of files). If you have marked clips, it then offers **Save Clips First**, which moves the marked captures and their marks to the Clips folder before deleting the rest, or **Delete Everything**. If saving a clip fails, nothing is deleted. Subfolders are never touched, it refuses to clean the top of a drive, and files still open in your recorder are reported as not deleted.
- **Game selector**: a collapsible panel on the right side of the window. The narrow strip at the right edge, or the game button at the top left, opens it, and the window widens to show an icon for each displayed game (the first icon, with a "?", means no game). Click a game to select it, with a red border and its name above the grid, then click the strip again to collapse the panel and shrink the window back. The game button always shows the selected game's icon, and the choice and whether the panel is open are remembered. When a game is selected, its filename label goes at the very front of saved file names, so with the label `BF3` a capture becomes `BF3_ace_2024-05-17 20-14-06.mp4`, and Cleanup's **Save Clips First** names the clips it keeps `BF3_<name>`.
- **Game manager** (the **Manage Games...** button at the bottom of the game selector): lists your games with their icons, filename labels and whether each is displayed. Add, edit, delete (after confirming), reorder with the arrows, and click a game to show or hide it. Games are saved to `SavedGames.xml` as soon as you change them, in the same format as the Swing app. The editor takes a title (unique), an optional filename label (letters, numbers, dashes, underscores and periods) and an icon, which is scaled to fit 64×64 and stored in `images/`. A game whose icon file is missing shows a default icon.
- **Hotkeys:** **F1** presses Keepers, **F2** presses Clips and **F3** presses Mark Clip, anywhere in the main window (including while typing a label). A button flashes briefly whenever it is used. A hotkey does nothing while its button is disabled, for example during a file move.

Known issues and gaps:

- Not yet ported from the Swing app: the menu bar and its Ctrl shortcuts, a close warning while a file move is running, a log file, and a progress bar. See FEATURE_PARITY.md.
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
