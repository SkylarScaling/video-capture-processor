package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
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

    String clipsLocation = props.getProperty(VideoCaptureProcessorProperty.CLIPS_LOCATION_PROPERTY);
    FileMoveTask task = new FileMoveTask(FileUtility.getFilename(newFile, false), captureDir, new File(destLocation),
        isBlank(clipsLocation) ? null : new File(clipsLocation));

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
//
//  private void markClip()
//  {
//    Date currentTime = new Date();
//
//    File clipFile = FileUtility.findNewestFileInDirectory(
//        propertiesInstance.getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName()));
//
//    if (clipFile == null)
//    {
//      JOptionPane.showMessageDialog(captureProcessorFrame, "There are no files in the capture directory!",
//          "Capture Directory is Empty", JOptionPane.WARNING_MESSAGE);
//      return;
//    }
//
//    String filename = FileUtility.getFilename(clipFile, false);
//
//    /*
//     * Allow custom format Filename format: 2011_8_23_20_14_6.M2TS
//     * 
//     * Live Gamer Portable Filename format: (yyyyMMddHHmmss) 20110823201406.mp4
//     * 
//     * OBS Studio Filename format: yyyy-MM-dd-HH-mm-ss.mp4
//     */
//    /*
//     * SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy_M_d_H_m_s",
//     * Locale.ENGLISH);
//     */
//    SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.ENGLISH);
//
//    Date fileDate;
//    try
//    {
//      fileDate = filenameDateFormat.parse(filename);
//    }
//    catch (ParseException e)
//    {
//      JOptionPane.showMessageDialog(captureProcessorFrame,
//          "Unable to parse date from capture file name. This clip cannot be marked.\n" + clipFile.getPath(),
//          "Clip Parsing Error", JOptionPane.ERROR_MESSAGE);
//      logger.log(Level.SEVERE, "Clip Date Parsing Error", e);
//      return;
//    }
//
//    long timeDiff = currentTime.getTime() - fileDate.getTime();
//    long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
//    long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff)
//        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeDiff));
//
//    NumberFormat numFormat = new DecimalFormat("00");
//    String clipTime = minutes + ":" + numFormat.format(seconds);
//    String clipLabel = labelField.getText().isEmpty() ? "" : " (" + labelField.getText() + ") ";
//    try
//    {
//      FileUtility.writeStringToFile(clipTime + clipLabel, new File(CLIPS_DIRECTORY + filename + CLIPS_FILE_EXTENSION),
//          true);
//      progressLabel.setText("Clip marked: " + clipTime);
//    }
//    catch (IOException e)
//    {
//      JOptionPane.showMessageDialog(captureProcessorFrame,
//          "Unable to parse date from capture file name. This clip cannot be marked.", "Clip Parsing Error",
//          JOptionPane.ERROR_MESSAGE);
//      logger.log(Level.SEVERE, "Clip File Creation Error", e);
//    }
//
//    new ButtonSwingWorker(FunctionButtons.MARK_BUTTON);
//  }
//
//  private void resetLabelFieldAndGetFocus()
//  {
//    // Reset the text
//    labelField.setText("");
//    labelField.requestFocus();
//  } 
}
