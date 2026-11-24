package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;
import com.wheezy.utils.file.FileUtility;
import com.wheezy.utils.ui.AlertDialog;

import javafx.scene.control.Alert.AlertType;

public class VideoCaptureProcessorUtils
{
  private static final Logger logger = Logger.getLogger(VideoCaptureProcessorUtils.class.getName());

  /**
   * Starts moving the newest capture file (and its clips file) to the location
   * configured for the given action. Must be called on the JavaFX thread: the move
   * runs on a background thread, and the result is reported with dialogs on the
   * JavaFX thread.
   *
   * @return the running task, or null if nothing was started
   */
  public static FileMoveTask saveNewestFile(SaveActionType sat)
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

    FileMoveTask task = new FileMoveTask(FileUtility.getFilename(newFile, false), captureDir, new File(destLocation),
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
