# Feature parity with the Swing app

Tracks what the original Swing app ([`VideoCaptureProcessor`](https://github.com/SkylarScaling/VideoCaptureProcessor), checked out as the sibling repo `../VideoCaptureProcessor`) does, and how much of it this JavaFX port has.

**How to use this file**

- `- [x]` = done in this port. `- [ ]` = not done yet.
- Every item has an ID (A1, B3, ...) so it can be referred to in commits and chats.
- **Swing:** how the original behaves and where to look (class names are under `src/main/java/com/wheezy/apps/vidcapproc/` in that repo).
- **Port notes:** what the port does, or an open question. This is the place to record a decision to *change* a feature instead of copying it. Mark an item **Decide:** while the question is open, and replace that with the decision when it is settled.
- Tick the box in the same commit that finishes the work, and update CLAUDE.md or the README if the behavior changed.

---

## A. Capture actions

- [x] **A1. Keepers button** moves the newest capture, and its clip marks, to the Keepers folder.
  - Swing: `saveNewestFile(SAVE_KEEPER)`, key **F1**.
  - Port notes: done. Runs on a background thread; both save buttons are disabled while a move runs.
- [x] **A2. Clips button** moves the newest capture, and its clip marks, to the Clips folder.
  - Swing: `saveNewestFile(SAVE_CLIP)`, key **F2**.
  - Port notes: done.
- [x] **A3. Mark Clip** records the time elapsed since the current recording started, with an optional label, in `clips/<capture name>.clips`.
  - Swing: `markClip()`, key **F3**.
  - Port notes: done, using `java.time` (`ClipMarker`). Needs capture files named `yyyy-MM-dd HH-mm-ss`, same as Swing. The label box is cleared after a mark.
- [x] **A4. Hotkeys F1 / F2 / F3** for Keepers, Clips and Mark Clip, working anywhere in the window.
  - Swing: key bindings on each button, tooltips show the key (for example "Move latest video to keepers folder (F1)").
  - Port notes: done, with the same tooltips. The keys are fixed, by decision; the Swing TODO list's "allow hotkeys to be re-mapped" is not planned. A hotkey presses its button, so it does exactly what a click does, and it does nothing while that button is disabled, such as during a file move. They work while the label box has focus.
- [x] **A5. Filename label on save.** Saved files are renamed to `<label text>_<original name>` (Swing: `<game filename label>_<label text>_<original name>`).
  - Swing: `FileMoveThread` builds the name from the selected game's filename label and the label box (sanitized with `FileUtility.sanitizeStringForFilename`). With no game selected, the name is unchanged.
  - Port notes: **Decided: a separate field.** Swing used one label box for both the clip-mark label and the filename text; the port has a "Filename label" field under the Keepers button, separate from the Mark Clip label. It applies to both Keepers and Clips. Text is added before the original name (`ace_2024-05-17 20-14-06.mp4`), unsafe characters become underscores, and the field is cleared after a successful move. Unlike Swing, it also works with no game selected. The game-label part of the name is item A10.
- [x] **A6. Background file moves with error reporting.** Move failures show a dialog and are logged.
  - Swing: `FileMoveThread` and `keepFile()`.
  - Port notes: done (`FileMoveTask`), with tests. Dialogs are shown from the JavaFX thread instead of the worker.
- [x] **A7. Cleanup button** deletes everything in the capture folder, after confirmation.
  - Swing: `FileDeleteThread`. Asks "Are you sure you want to delete all files from the capture directory?" first. If any `.clips` mark files exist, asks again with *Yes / Cancel / Save Clips*. *Save Clips* first moves the captures that have marks to the Clips folder (with the game prefix), then deletes the rest. Afterwards it deletes the `.clips` files and warns if some files could not be deleted.
  - Port notes: done (`FileCleanupTask`, with tests). Behaves like Swing, with these decisions:
    - **Permanent delete, not the recycle bin.** The point of a cleanup is to free disk space after a session, and large videos often skip the recycle bin anyway.
    - **Confirmation shows a count and the folder** ("Delete 12 files from the capture directory?"), and the Delete button is not the default.
    - **Save Clips First** is kept. All questions are asked before anything is deleted. If saving a clip fails, nothing is deleted. A mark whose capture is already gone is skipped.
    - **Safety checks (new):** it refuses to clean a drive root, a missing folder or an unset folder, and says so when the folder is already empty. Subdirectories are never touched, and only `.clips` files are removed from the marks folder.
    - While it runs, Keepers, Clips, Mark Clip and Cleanup are disabled. Moves disable Cleanup so the two cannot overlap.
    - The old test panel (`ButtonPanel.fxml`) was removed.
- [x] **A8. Status text and progress bar** for long actions ("Moving File...", "Move Complete", "Move Failed", "Cleaning Up Files...").
  - Swing: status bar with a label and an indeterminate progress bar.
  - Port notes: done. A thin progress bar along the bottom of the window shows while a move or cleanup runs. It is determinate for a move (one step per file, counting the `.clips` mark file) and indeterminate until the first file finishes and during a cleanup, because a single large file gives no finer progress. The status line shows "Moving ...", "Move complete" / "Move failed", and "Cleaning up files...", "Cleanup complete" / "Cleanup failed".

- [x] **A9. Button press feedback.** Buttons flash for 250 ms when used (`ButtonSwingWorker`), for example when a hotkey triggers them.
  - Port notes: done (`ButtonFlash`). The button darkens briefly on any use, click or hotkey, and a second press mid-flash restores the original look correctly.
- [x] **A10. Game label in saved filenames.** With a game selected, the name becomes `<game filename label>_<label text>_<original name>`.
  - Port notes: done. Keepers and Clips put the selected game's label first, then the Filename label box text, then the original name (`BF3_ace_2024-05-17 20-14-06.mp4`). With no game selected, or a game with no label, that part is left out. Cleanup's *Save Clips First* uses the game label alone, as in Swing.

## B. Settings and window

- [x] **B1. Settings dialog** with Capture, Keepers and Clips folders and Browse buttons.
  - Swing: `ConfigDialog` ("Set Folders..." in the File menu).
  - Port notes: done. Opened with the gear button instead of a menu.
- [x] **B2. Properties file** (`CapProcConfig.xml`) saves settings, with an error dialog if it cannot be read.
  - Port notes: done, with the same file format. **Decide:** still read from the working directory. Moving it to a per-user location (such as `%APPDATA%`) would remove the "launch from the same folder" requirement.
- [x] **B3. Always On Top is applied** to the window, defaulting to on.
  - Swing: File menu check item, **Ctrl+A**. Defaults to true when unset.
  - Port notes: done. Applied at startup and again when the Settings dialog closes, and on by default when never set, as in Swing. Ctrl+A is the shortcut of the File menu's Always On Top item (see B8). Settings, the other dialogs and error alerts are now owned by the main window, so they cannot open hidden behind it.
- [x] **B4. Remember the window position.** Saved on close (`FrameLocationX/Y`), restored on launch.
  - Port notes: done (`WindowSettings`). A saved position that is no longer on any screen, for example after unplugging a monitor, is ignored so the window cannot open out of sight. Values are JavaFX coordinates, so positions saved by the Swing app are not carried over.
- [x] **B5. Remember the last folder browsed** in file choosers (`FileChooserLoc`).
  - Port notes: done. The chooser opens at the folder already in the field, otherwise where the last choice was made. After each choice the parent folder is saved, because the capture, keepers and clips folders are usually siblings.
- [x] **B6. Close confirmation while a move is running**, with "Close Anyway" / "Cancel".
  - Port notes: done, with a changed meaning. Closing the window does not stop a move or cleanup (its thread is not a daemon, so it finishes in the background), but the result dialogs would never appear, so closing while one runs asks "Close Anyway" / "Cancel" and says the work will finish in the background.
- [x] **B7. Log file** (`logs/CapProc.log`).
  - Swing: `java.util.logging` `FileHandler`.
  - Port notes: done (`AppLogging`). Everything the app logs goes to `logs/CapProc.log` in the working directory as well as the console, with stack traces, and uncaught exceptions on any thread are logged too. The folder is created if needed. Changes from Swing: one handler on the root logger instead of one per class, a readable text format instead of XML, and a log over 1 MB is set aside as `CapProc.log.1` at startup (Swing appended forever). If the log cannot be created the app still starts.
- [x] **B8. Menu bar** with File (Always On Top, Set Folders...) and Games (Show Game Selector, Manage Games...) menus, with Ctrl shortcuts (A, S, G).
  - Port notes: done, decided to add it (the gear and game buttons stay as well). Same items, Alt mnemonics and shortcuts as Swing: **Ctrl+A** Always On Top, **Ctrl+S** Show Game Selector, **Ctrl+G** Manage Games. Always On Top applies at once and is saved; it and Show Game Selector are check items that stay in step with the Settings dialog and the selector panel. While a text box has focus, Ctrl+A selects its text instead, as in Swing. The window is one menu bar taller.

## C. Game selector

- [x] **C1. Game collection stored in `SavedGames.xml`.** Ordered list of games, each with title (unique), filename label, icon path and a "displayed" flag.
  - Swing: `GameCollection`, `GameSet`, `GameModel`, `GameController`, JAXB. The model keeps a `backup.bak` copy of the file when saving.
  - Port notes: done (`GameCatalog`, `GameCollection`), with decisions:
    - **Same XML format** as Swing, so an existing `SavedGames.xml` loads unchanged. Checked against a real 61-game file. Copy it next to `CapProcConfig.xml` (the working directory) to bring your games over.
    - The model is an observable list that screens listen to, replacing Swing's `Observable`.
    - Every change is saved immediately and **undone in memory if the save fails**. Saves write a temporary file and swap it in, and the previous version is kept as `SavedGames.xml.bak`.
    - An **unreadable file is never silently replaced**: a copy is kept as `SavedGames.xml.unreadable` and the user is told. Games with no title, and repeats of a title (ignoring case), are skipped on load.
    - `Game.hashCode` now agrees with `equals` (both ignore case); before, they disagreed.
- [x] **C2. Game selector** shows icon buttons for displayed games, plus a clear-game ("None") button, with the selected game highlighted.
  - Swing: grid of 5 icons per row, tooltip shows the title, selected game gets a red 4 px border, and the name shows in a label.
  - Port notes: done (`GameSelectorPanel`, `GameSelection`), **decided: a collapsible panel on the right side of the main window**. A narrow handle (with an arrow) is always visible; clicking it, or the game button, opens the panel and the window widens; clicking again collapses it and the window narrows back (a window near the right screen edge is moved left so it is not cut off). The grid is four icons per row (Swing: five, with a window that grew taller), scrolls when there are more games, and shows only displayed games. The first icon, with a "?", means no game. The selected icon gets the red border and its title shows above the grid. The panel does not close when you pick a game; click the handle to close it. **Manage Games...** is at the bottom of the panel. The old separate Game Selector dialog was removed.
- [x] **C3. Selected game is remembered** between runs (`SelectedGame`).
  - Port notes: done. It follows the game if you rename it in the manager, and clears if you delete it. Hiding the selected game keeps it selected (its name still shows, without an icon).
- [x] **C4. Show or hide the selector**, remembered between runs (`GameSelectorShow`), with the window resizing to fit.
  - Port notes: done. It starts collapsed the first time. Unlike Swing, the window grows sideways, not taller.
- [x] **C5. Right-click menu** on a game button with "Edit...".
- Port notes: done. Right-clicking a game icon in the selector offers "Edit...", which opens the same editor as the game manager (`GameEditorController.open`). The icon for no game has no menu. Editing does not select the game.

## D. Game manager

- [x] **D1. Game manager window** listing every game with its icon, title and filename format (`<label>_<filename>`).
  - Swing: `GameManager` and `GamePanel`.
  - Port notes: done (`GameManagerController`, `GamePanel`). Opened from the game selector dialog's "Manage Games..." button for now. Rows show whether a game is displayed, a hidden game is dimmed, and the header shows a count ("61 games, 14 displayed"). The scroll position is kept when a row changes. A game whose icon file is missing shows the default icon.
- [x] **D2. Add, edit and delete games.**
  - Port notes: done. **Delete now asks for confirmation** (Swing deleted at once); the icon file is not deleted. If a change cannot be saved, it is undone and the user is told.
- [x] **D3. Reorder games** with up and down.
  - Port notes: done, with the arrows disabled at the ends. Send-to-top, send-to-bottom and drag and drop (from the Swing TODO list) are not done.
- [x] **D4. Click a game to toggle whether it is displayed** in the selector.
  - Port notes: done. Clicking the Edit, Delete or arrow buttons does not toggle.
- [x] **D5. Game editor** with title, filename label, and an icon chosen with a file browser and preview.
  - Swing: `GameEditor`. Title must be unique. The chosen image is resized to 64×64 and saved as a PNG under `images/`. A default icon is used until one is chosen.
  - Port notes: done (`GameEditorController`), with decisions:
    - **Icons are stored in an `images/` folder in the working directory** (named after the game, never overwriting another game's icon) and recorded with a **relative path** such as `images/Battlefield_3.png`, so the games file survives moving to another drive. Old absolute paths keep working.
    - **The filename label is now validated** (letters, numbers, dashes, underscores, periods; or blank), which fixes the known Swing gap, and the editor shows how files will be named. Titles must be unique ignoring case.
    - Choosing an image remembers the folder for next time (B5). Supported images are what JavaFX reads: PNG, JPG, GIF and BMP (TIFF, which Swing accepted, is not).
    - If the icon cannot be copied into `images/`, the game keeps pointing at the original file, as in Swing.
- [x] **D6. Image helpers** for resizing and previewing icons.
  - Swing: `ImageUtility` and `ImagePreviewPanel` under `com.wheezy.utils.image`.
  - Port notes: done differently. Instead of copying the Swing AWT helpers, `GameIcons` uses JavaFX's own image scaling (fit within 64×64, aspect kept) and writes the PNG with `ImageIO`.

## E. Ideas from the Swing TODO list

These were never built in Swing. They are recorded here so they can be taken up, or dropped, deliberately.

- [x] **E1.** Check that a capture file is not still open (held by the recorder) before moving it.
  - Port notes: done (`FileInUse`, used by `FileMoveTask.moveCapture`). Before anything is moved, each capture file is renamed to a temporary name and straight back, which is exactly what a move needs and fails on Windows while the recorder has the file open. If any is open nothing is moved, no folder is created, and the dialog says the file is still open and asks whether it is still recording. Cleanup's **Save Clips First** gets the same check, so a clip still being recorded stops the cleanup before anything is deleted. Other operating systems allow renaming open files, so there the check finds nothing.
- [x] **E2.** Choose the capture filename format from a menu (today only `yyyy-MM-dd HH-mm-ss` works; Swing's code also mentions `yyyy_M_d_H_m_s`, Live Gamer Portable `yyyyMMddHHmmss`, and OBS).
  - Port notes: done (`CaptureNameFormat`). Settings has a **Capture file names** list: *Detect automatically* (the default, which tries every format below), *OBS* (`yyyy-MM-dd HH-mm-ss`), *Underscores* (`yyyy_M_d_H_m_s`) and *Live Gamer Portable* (`yyyyMMddHHmmss`). Only Mark Clip reads names this way. If a name does not fit, the error says which format was expected. Adding a format is one line in the enum.
- [x] **E3.** Put keepers and clips into per-game folders.
  - Port notes: done (`GameFolders`), as an option that is **off by default**: Settings has *Keep each game's keepers and clips in its own folder*. When on and a game is selected, Keepers and Clips save to `<folder>/<game title>/` (unsafe characters become underscores, as for labels), and Cleanup's Save Clips First does the same. With no game selected they save straight into the folder. The game folder is created by the move and removed again if the move fails.
- [x] **E4.** Faster file movement (native copy).
  - Port notes: **decided: nothing to change.** On one drive a move is a rename, which is instant. Between drives it falls back to `Files.move`, which on Windows hands the copy to the operating system, so there is no slower Java copy loop to replace. I did not time a large cross-drive move. A progress bar for a long cross-drive copy would be a separate feature.
- [ ] **E5.** Write timestamp tags into the MP4 metadata, like GoPro.
  - Port notes: **not done, by decision.** It means rewriting the user's recordings, which this app deliberately never does, and a mistake would corrupt a video. What it would be for (a start time in the file) is already in the file name, and Mark Clip uses that. Worth revisiting only with a clear need and a safe MP4 library.
- [x] **E6.** Show the selected game's icon when the game selector is collapsed. Done: the game button on the main window always shows the selected game's icon.
- [ ] **E7.** "Game mode" support for game labels.
  - Port notes: **not done: undefined.** Swing's list has only this one line and its code has nothing behind it. It needs a definition from you first, for example separate labels per mode (ranked, casual) chosen from the game's right-click menu.
- [ ] **E8.** A clip manager, with other video formats and a built-in video player for review.
  - Port notes: **not done: a project of its own.** JavaFX's media player handles few formats (not the MKV or MOV files recorders often make), so a review player would need a separate library, and the clip manager needs a design for what it lists and edits. Best started as its own task.
- [ ] **E9.** Built-in image search to help find game icons.
  - Port notes: **not done, by decision.** It needs an image search service, which means an API key or scraping a web page that can change or break, and sending game names to a third party. The game editor's file chooser already takes any PNG, JPG, GIF or BMP.
- [x] **E10.** System tray icon.
  - Port notes: done (`MainWindowTray`). The icon shows in the notification area with the app's icon. Clicking it restores the window if it is minimized and brings it to the front. Its right-click menu has **Keepers**, **Clips**, **Mark Clip** and **Exit** (Exit asks the same "Close Anyway" question as the close button when work is running). Closing the window still ends the app and removes the icon. **Decision:** no minimize-to-tray, since hiding the window would need the app to stay alive with no window. Not tried on screen: only the menu and the restore were tested.

---

## Changes in the port that Swing does not have

Not parity items, but worth knowing about when comparing the two apps.

- Moves run on a background `Task`, with unit tests (`FileMoveTaskTest`).
- Clip-mark logic is separate and unit tested (`ClipMarker`).
- Missing or unset folders show an explanatory dialog instead of failing.
- Clip marks must come from a parseable filename, and errors say what is wrong and write nothing.
- The Settings dialog is opened from a gear button, and Cleanup and Settings are icon buttons with tooltips.
- A move creates a missing destination folder, refuses to overwrite an existing file (nothing is moved), and puts files back if it fails part way; if one cannot be put back, the error names where it is. Swing did none of these.
