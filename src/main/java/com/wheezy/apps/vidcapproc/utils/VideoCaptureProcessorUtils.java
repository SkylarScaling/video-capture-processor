package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wheezy.apps.vidcapproc.data.GameSelection;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;
import com.wheezy.utils.file.FileUtility;
import com.wheezy.utils.file.filter.list.FileExtensionListFilter;
import com.wheezy.utils.file.filter.list.FileOnlyListFilter;
import com.wheezy.utils.ui.AlertDialog;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

public class VideoCaptureProcessorUtils
{
  private static final Logger logger = Logger.getLogger(VideoCaptureProcessorUtils.class.getName());

  /**
   * Starts moving the newest capture file (and its clips file) to the location
   * configured for the given action. Must be called on the JavaFX thread: the move
   * runs on a background thread, and the result is reported with dialogs on the
   * JavaFX thread.
   *
   * @param filenameLabel text added to the saved files' names (see {@link CaptureNaming}), or null/blank for none
   * @return the running task, or null if nothing was started
   */
  public static FileMoveTask saveNewestFile(SaveActionType sat, String filenameLabel)
  {
    VideoCaptureProcessorProperties props = VideoCaptureProcessorProperties.getInstance();

    VideoCaptureProcessorProperty destProperty;
    switch (sat)
    {
      case SAVE_KEEPER:
        destProperty = VideoCaptureProcessorProperty.KEEPERS_LOCATION_PROPERTY;
        break;
      case SAVE_CLIP:
        destProperty = VideoCaptureProcessorProperty.CLIPS_LOCATION_PROPERTY;
        break;
      default:
        logger.warning("Save action not supported yet: " + sat);
        return null;
    }

    String captureLocation = props.getProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY);
    String destLocation = props.getProperty(destProperty);
    if (isBlank(captureLocation) || isBlank(destLocation))
    {
      AlertDialog.displayErrorDialog("File Save Error", "Locations Not Configured",
          "Set the capture and destination directories in Settings first.");
      return null;
    }

    File captureDir = new File(captureLocation);
    File newFile = captureDir.isDirectory() ? FileUtility.findNewestFileInDirectory(captureLocation) : null;
    if (newFile == null)
    {
      AlertDialog.displayErrorDialog("File Save Error", "Error During File Save",
          "There are no files in the directory: " + captureDir.getAbsolutePath());
      return null;
    }

    String captureName = FileUtility.getFilename(newFile, false);
    // With a game selected, its label comes first: <game label>_<filename label>_<original name>
    String newName = CaptureNaming.buildName(selectedGameLabel(), filenameLabel, captureName);
    FileMoveTask task = new FileMoveTask(captureName, newName, captureDir, new File(destLocation),
        new File(ClipMarker.CLIPS_DIRECTORY));

    task.setOnSucceeded(event ->
    {
      FileMoveTask.Result result = task.getValue();
      if (!result.succeeded())
      {
        AlertDialog.displayAlertDialog(AlertType.WARNING, "Problem Moving Files",
            "File move was unable to complete for the following reason: ",
            result.code().getMessage() + " (" + result.detail() + ").\nPlease try again.");
      }
    });
    task.setOnFailed(event ->
    {
      Throwable error = task.getException();
      logger.log(Level.SEVERE, "Error Moving Files", error);
      AlertDialog.displayErrorDialog("Error Moving Files",
          "An error was encountered while attempting to move files. Check your video directories.",
          String.valueOf(error.getMessage()));
    });

    // Not a daemon thread: closing the window mid-move should not cut a copy short.
    new Thread(task, "file-move").start();
    return task;
  }

  private static final ButtonType DELETE_BUTTON = new ButtonType("Delete", ButtonData.OTHER);
  private static final ButtonType DELETE_EVERYTHING_BUTTON = new ButtonType("Delete Everything", ButtonData.OTHER);
  private static final ButtonType SAVE_CLIPS_BUTTON = new ButtonType("Save Clips First", ButtonData.OTHER);

  /**
   * Deletes every file in the capture directory, after confirming. If clip marks
   * exist, also offers to save the marked captures to the Clips folder first. All
   * questions are asked up front, and nothing is deleted unless the user agrees.
   * Must be called on the JavaFX thread: the cleanup runs on a background thread,
   * and the result is reported with dialogs on the JavaFX thread.
   * <p>
   * The files are deleted permanently, not sent to the recycle bin, because the
   * point of a cleanup is to free disk space after a session.
   *
   * @return the running task, or null if nothing was started
   */
  public static FileCleanupTask cleanupCaptureDirectory()
  {
    VideoCaptureProcessorProperties props = VideoCaptureProcessorProperties.getInstance();

    String captureLocation = props.getProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY);
    if (isBlank(captureLocation))
    {
      AlertDialog.displayErrorDialog("Cleanup Error", "Capture Location Not Configured",
          "Set the capture directory in Settings first.");
      return null;
    }

    File captureDir = new File(captureLocation);
    if (!captureDir.isDirectory())
    {
      AlertDialog.displayErrorDialog("Cleanup Error", "Capture Directory Not Found",
          "There is no directory at: " + captureDir.getAbsolutePath());
      return null;
    }
    if (captureDir.getAbsoluteFile().getParentFile() == null)
    {
      // A mistyped setting must not be able to empty a whole drive
      AlertDialog.displayErrorDialog("Cleanup Error", "Capture Directory Is a Drive Root",
          "Cleanup will not delete the files at the top of a drive: " + captureDir.getAbsolutePath());
      return null;
    }

    File marksDir = new File(ClipMarker.CLIPS_DIRECTORY);
    File[] captureFiles = captureDir.listFiles(new FileOnlyListFilter());
    File[] markFiles = marksDir.listFiles(new FileExtensionListFilter(ClipMarker.CLIPS_FILE_EXTENSION));
    int fileCount = captureFiles == null ? 0 : captureFiles.length;
    int markCount = markFiles == null ? 0 : markFiles.length;

    if (fileCount == 0 && markCount == 0)
    {
      AlertDialog.displayInfoDialog("Nothing to Clean Up", "The capture directory has no files.",
          captureDir.getAbsolutePath());
      return null;
    }

    Optional<ButtonType> confirmed = AlertDialog.displayChoiceDialog(AlertType.WARNING, "Confirm Cleanup",
        "Delete " + fileCount + (fileCount == 1 ? " file" : " files") + " from the capture directory?",
        captureDir.getAbsolutePath() + "\n\nThey are deleted permanently.", DELETE_BUTTON, ButtonType.CANCEL);
    if (confirmed.isEmpty() || confirmed.get() != DELETE_BUTTON)
    {
      return null;
    }

    File saveDir = null;
    if (markCount > 0)
    {
      Optional<ButtonType> choice = AlertDialog.displayChoiceDialog(AlertType.WARNING, "Clips Exist",
          "Clips are marked on " + markCount + (markCount == 1 ? " capture" : " captures") + " that will be deleted.",
          "\"Save Clips First\" moves those captures, with their marks, to the Clips folder before everything else "
              + "is deleted.\n\"Delete Everything\" discards them.",
          SAVE_CLIPS_BUTTON, DELETE_EVERYTHING_BUTTON, ButtonType.CANCEL);
      if (choice.isEmpty() || choice.get() == ButtonType.CANCEL)
      {
        return null;
      }
      if (choice.get() == SAVE_CLIPS_BUTTON)
      {
        String clipsLocation = props.getProperty(VideoCaptureProcessorProperty.CLIPS_LOCATION_PROPERTY);
        if (isBlank(clipsLocation))
        {
          AlertDialog.displayErrorDialog("Cleanup Error", "Clips Folder Not Configured",
              "Set the clips directory in Settings first. Nothing was deleted.");
          return null;
        }
        saveDir = new File(clipsLocation);
      }
    }

    FileCleanupTask task = new FileCleanupTask(captureDir, marksDir, saveDir, selectedGameLabel());

    task.setOnSucceeded(event ->
    {
      FileCleanupTask.Result result = task.getValue();
      if (!result.succeeded())
      {
        AlertDialog.displayAlertDialog(AlertType.WARNING, "Cleanup Stopped",
            "Cleanup stopped before deleting anything, because saving a clip failed.",
            result.detail() + "\n" + result.savedCaptures() + " capture(s) were saved to the Clips folder first.");
      }
      else if (result.undeletedFiles() > 0)
      {
        AlertDialog.displayAlertDialog(AlertType.WARNING, "Files Not Deleted",
            result.undeletedFiles() + " files could not be deleted from the capture directory.",
            "They may still be open in your recording software.");
      }
    });
    task.setOnFailed(event ->
    {
      Throwable error = task.getException();
      logger.log(Level.SEVERE, "Error During Cleanup", error);
      AlertDialog.displayErrorDialog("Cleanup Error", "An error was encountered during the cleanup.",
          String.valueOf(error.getMessage()));
    });

    new Thread(task, "file-cleanup").start();
    return task;
  }

  /** @return the filename label of the selected game, or null if no game is selected or it has no label */
  private static String selectedGameLabel()
  {
    return GameSelection.getInstance().gameLabel().orElse(null);
  }

  private static boolean isBlank(String value)
  {
    return value == null || value.isBlank();
  }

  /**
   * Marks the current moment in the capture that is being recorded (the newest file
   * in the capture directory), showing a dialog and returning null if that is not
   * possible. Must be called on the JavaFX thread.
   *
   * @param label optional text saved with the mark
   * @return the marked time as minutes:seconds, e.g. "1:35", or null if nothing was marked
   */
  public static String markClip(String label)
  {
    String captureLocation = VideoCaptureProcessorProperties.getInstance()
        .getProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY);
    if (isBlank(captureLocation))
    {
      AlertDialog.displayErrorDialog("Clip Mark Error", "Capture Location Not Configured",
          "Set the capture directory in Settings first.");
      return null;
    }

    File captureDir = new File(captureLocation);
    File captureFile = captureDir.isDirectory() ? FileUtility.findNewestFileInDirectory(captureLocation) : null;
    if (captureFile == null)
    {
      AlertDialog.displayAlertDialog(AlertType.WARNING, "Capture Directory is Empty", "Clip Mark Error",
          "There are no files in the capture directory: " + captureDir.getAbsolutePath());
      return null;
    }

    String captureName = FileUtility.getFilename(captureFile, false);
    try
    {
      return ClipMarker.mark(captureName, new File(ClipMarker.CLIPS_DIRECTORY), label, LocalDateTime.now());
    }
    catch (DateTimeParseException | IllegalArgumentException e)
    {
      logger.log(Level.WARNING, "Clip Date Parsing Error", e);
      AlertDialog.displayErrorDialog("Clip Parsing Error", "This clip cannot be marked.",
          "Unable to get a start time from the capture file name (expected yyyy-MM-dd HH-mm-ss):\n"
              + captureFile.getName());
    }
    catch (IOException e)
    {
      logger.log(Level.SEVERE, "Clip File Creation Error", e);
      AlertDialog.displayErrorDialog("Clip Mark Error", "Unable to save the clip mark.", e.getMessage());
    }
    return null;
  }
}
