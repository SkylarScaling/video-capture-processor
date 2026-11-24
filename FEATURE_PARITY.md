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
- [ ] **A4. Hotkeys F1 / F2 / F3** for Keepers, Clips and Mark Clip, working anywhere in the window.
  - Swing: key bindings on each button, tooltips show the key (for example "Move latest video to keepers folder (F1)").
  - Port notes: **Decide:** keep F1 to F3, or make them configurable? The Swing TODO list also asks for re-mappable keys. Tooltips should show the key either way.
- [ ] **A5. Game-prefixed filenames on save.** With a game selected, files are renamed to `<game filename label>_<label text>_<original name>`.
  - Swing: `FileMoveThread` builds the name from the selected game's filename label and the label box (sanitized with `FileUtility.sanitizeStringForFilename`). With no game selected, the name is unchanged.
  - Port notes: **Decide:** in Swing the one label box is used both for the clip-mark label *and* the filename. The port currently uses it only for marks, and clears it after each mark. Should the filename label be a separate field? Depends on the game selector (section C).
- [x] **A6. Background file moves with error reporting.** Move failures show a dialog and are logged.
  - Swing: `FileMoveThread` and `keepFile()`.
  - Port notes: done (`FileMoveTask`), with tests. Dialogs are shown from the JavaFX thread instead of the worker.
- [ ] **A7. Cleanup button** deletes everything in the capture folder, after confirmation.
  - Swing: `FileDeleteThread`. Asks "Are you sure you want to delete all files from the capture directory?" first. If any `.clips` mark files exist, asks again with *Yes / Cancel / Save Clips*. *Save Clips* first moves the captures that have marks to the Clips folder (with the game prefix), then deletes the rest. Afterwards it deletes the `.clips` files and warns if some files could not be deleted.
  - Port notes: the button currently opens a test panel (`ButtonPanel.fxml`, marked "TODO REMOVE"). **Decide:** keep the *Save Clips* option? Deleting is destructive, so consider moving to the recycle bin instead of permanent delete, and showing a count before confirming.
- [ ] **A8. Status text and progress bar** for long actions ("Moving File...", "Move Complete", "Move Failed", "Cleaning Up Files...").
  - Swing: status bar with a label and an indeterminate progress bar.
  - Port notes: the status line shows the move messages, but there is no progress bar. `FileMoveTask` already reports progress, so a determinate bar is straightforward.
- [ ] **A9. Button press feedback.** Buttons flash for 250 ms when used (`ButtonSwingWorker`), for example when a hotkey triggers them.
  - Port notes: matters most once hotkeys exist (A4), since there is no click to show the press.

## B. Settings and window

- [x] **B1. Settings dialog** with Capture, Keepers and Clips folders and Browse buttons.
  - Swing: `ConfigDialog` ("Set Folders..." in the File menu).
  - Port notes: done. Opened with the gear button instead of a menu.
- [x] **B2. Properties file** (`CapProcConfig.xml`) saves settings, with an error dialog if it cannot be read.
  - Port notes: done, with the same file format. **Decide:** still read from the working directory. Moving it to a per-user location (such as `%APPDATA%`) would remove the "launch from the same folder" requirement.
- [ ] **B3. Always On Top is applied** to the window, defaulting to on.
  - Swing: File menu check item, **Ctrl+A**. Defaults to true when unset.
  - Port notes: the checkbox in Settings saves `AlwaysOnTop`, but **nothing applies it to the window yet**, so the setting currently does nothing.
- [ ] **B4. Remember the window position.** Saved on close (`FrameLocationX/Y`), restored on launch.
  - Port notes: the property keys exist but are unused.
- [ ] **B5. Remember the last folder browsed** in file choosers (`FileChooserLoc`).
  - Port notes: the key exists but the folder chooser does not use it.
- [ ] **B6. Close confirmation while a move is running**, with "Close Anyway" / "Cancel".
  - Port notes: **Decide:** the port's move thread is not a daemon thread, so a move finishes even if the window closes. A warning may still be wanted.
- [ ] **B7. Log file** (`logs/CapProc.log`).
  - Swing: `java.util.logging` `FileHandler`.
  - Port notes: the port logs to the console only.
- [ ] **B8. Menu bar** with File (Always On Top, Set Folders...) and Games (Show Game Selector, Manage Games...) menus, with Ctrl shortcuts (A, S, G).
  - Port notes: **Decide:** the port uses buttons instead (gear for Settings, the game button for the selector). Decide whether to add the menu bar or keep the compact layout, and if the shortcuts are still wanted.

## C. Game selector

- [ ] **C1. Game collection stored in `SavedGames.xml`.** Ordered list of games, each with title (unique), filename label, icon path and a "displayed" flag.
  - Swing: `GameCollection`, `GameSet`, `GameModel`, `GameController`, JAXB. The model keeps a `backup.bak` copy of the file when saving.
  - Port notes: only the `Game` bean exists (already JAXB). Nothing loads or saves a collection. **Decide:** keep the same XML format so existing `SavedGames.xml` files keep working? The file stores absolute icon paths. Replace the Swing `Observable`-based model with something current.
- [ ] **C2. Game selector** shows icon buttons for displayed games, plus a clear-game ("None") button, with the selected game highlighted.
  - Swing: grid of 5 icons per row, tooltip shows the title, selected game gets a red 4 px border, and the name shows in a label.
  - Port notes: `GameSelectorDialog.fxml` and the game button on the main window exist, but selection is not wired up. **Decide:** the Swing selector is inline and expands the main window; the port's FXML is a separate dialog.
- [ ] **C3. Selected game is remembered** between runs (`SelectedGame`).
- [ ] **C4. Show or hide the selector**, remembered between runs (`GameSelectorShow`), with the window resizing to fit.
  - Port notes: only relevant if the selector stays inline (see C2).
- [ ] **C5. Right-click menu** on a game button with "Edit...".

## D. Game manager

- [ ] **D1. Game manager window** listing every game with its icon, title and filename format (`<label>_<filename>`).
  - Swing: `GameManager` and `GamePanel`. `GameManager.fxml` and `GamePanel.fxml` exist as unwired scaffolding.
- [ ] **D2. Add, edit and delete games.**
- [ ] **D3. Reorder games** with up and down.
  - Port notes: the Swing TODO list also wants send-to-top, send-to-bottom, and drag and drop.
- [ ] **D4. Click a game to toggle whether it is displayed** in the selector.
- [ ] **D5. Game editor** with title, filename label, and an icon chosen with a file browser and preview.
  - Swing: `GameEditor`. Title must be unique. The chosen image is resized to 64×64 and saved as a PNG under `images/`. A default icon is used until one is chosen.
  - Port notes: **Decide:** where icons should live (Swing uses a working-directory `images/` folder; the port bundles only `default.jpg`). Known Swing bug to fix while porting: the filename label is not checked for characters that are invalid in filenames.
- [ ] **D6. Image helpers** for resizing and previewing icons.
  - Swing: `ImageUtility` and `ImagePreviewPanel` under `com.wheezy.utils.image`, not yet copied into this repo.

## E. Ideas from the Swing TODO list

These were never built in Swing. They are recorded here so they can be taken up, or dropped, deliberately.

- [ ] **E1.** Check that a capture file is not still open (held by the recorder) before moving it.
- [ ] **E2.** Choose the capture filename format from a menu (today only `yyyy-MM-dd HH-mm-ss` works; Swing's code also mentions `yyyy_M_d_H_m_s`, Live Gamer Portable `yyyyMMddHHmmss`, and OBS).
- [ ] **E3.** Put keepers and clips into per-game folders.
- [ ] **E4.** Faster file movement (native copy).
- [ ] **E5.** Write timestamp tags into the MP4 metadata, like GoPro.
- [ ] **E6.** Show the selected game's icon when the game selector is collapsed.
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
