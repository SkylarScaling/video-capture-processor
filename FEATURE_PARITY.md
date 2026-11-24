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
  - Port notes: done. Applied at startup and again when the Settings dialog closes, and on by default when never set, as in Swing. The Ctrl+A shortcut is not ported (see B8). Settings, the other dialogs and error alerts are now owned by the main window, so they cannot open hidden behind it.
- [x] **B4. Remember the window position.** Saved on close (`FrameLocationX/Y`), restored on launch.
  - Port notes: done (`WindowSettings`). A saved position that is no longer on any screen, for example after unplugging a monitor, is ignored so the window cannot open out of sight. Values are JavaFX coordinates, so positions saved by the Swing app are not carried over.
- [x] **B5. Remember the last folder browsed** in file choosers (`FileChooserLoc`).
  - Port notes: done. The chooser opens at the folder already in the field, otherwise where the last choice was made. After each choice the parent folder is saved, because the capture, keepers and clips folders are usually siblings.
- [x] **B6. Close confirmation while a move is running**, with "Close Anyway" / "Cancel".
  - Port notes: done, with a changed meaning. Closing the window does not stop a move or cleanup (its thread is not a daemon, so it finishes in the background), but the result dialogs would never appear, so closing while one runs asks "Close Anyway" / "Cancel" and says the work will finish in the background.
- [ ] **B7. Log file** (`logs/CapProc.log`).
  - Swing: `java.util.logging` `FileHandler`.
  - Port notes: the port logs to the console only.
- [ ] **B8. Menu bar** with File (Always On Top, Set Folders...) and Games (Show Game Selector, Manage Games...) menus, with Ctrl shortcuts (A, S, G).
  - Port notes: **Decide:** the port uses buttons instead (gear for Settings, the game button for the selector). Decide whether to add the menu bar or keep the compact layout, and if the shortcuts are still wanted.

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
- [ ] **C5. Right-click menu** on a game button with "Edit...".

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

- [ ] **E1.** Check that a capture file is not still open (held by the recorder) before moving it.
- [ ] **E2.** Choose the capture filename format from a menu (today only `yyyy-MM-dd HH-mm-ss` works; Swing's code also mentions `yyyy_M_d_H_m_s`, Live Gamer Portable `yyyyMMddHHmmss`, and OBS).
- [ ] **E3.** Put keepers and clips into per-game folders.
- [ ] **E4.** Faster file movement (native copy).
- [ ] **E5.** Write timestamp tags into the MP4 metadata, like GoPro.
- [x] **E6.** Show the selected game's icon when the game selector is collapsed. Done: the game button on the main window always shows the selected game's icon.
- [ ] **E7.** "Game mode" support for game labels.
- [ ] **E8.** A clip manager, with other video formats and a built-in video player for review.
- [ ] **E9.** Built-in image search to help find game icons.
- [ ] **E10.** System tray icon.

---

## Changes in the port that Swing does not have

Not parity items, but worth knowing about when comparing the two apps.

- Moves run on a background `Task`, with unit tests (`FileMoveTaskTest`).
- Clip-mark logic is separate and unit tested (`ClipMarker`).
- Missing or unset folders show an explanatory dialog instead of failing.
- Clip marks must come from a parseable filename, and errors say what is wrong and write nothing.
- The Settings dialog is opened from a gear button, and Cleanup and Settings are icon buttons with tooltips.
- A move creates a missing destination folder, refuses to overwrite an existing file (nothing is moved), and puts files back if it fails part way; if one cannot be put back, the error names where it is. Swing did none of these.
