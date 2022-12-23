package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
import java.io.IOException;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;
import com.wheezy.utils.file.FileUtility;
import com.wheezy.utils.file.FileUtilityReturnCode;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class VideoCaptureProcessorUtils
{
  private static String CLIPS_FILE_EXTENSION = ".clips";

  public static void saveNewestFile(SaveActionType sat, File sourceDirectory)
  {
    VideoCaptureProcessorProperty locationProperty = VideoCaptureProcessorProperty.CLIPS_LOCATION_PROPERTY;
    //FunctionButtonType fb = FunctionButtonType.CLIPS_BUTTON;

    switch (sat)
    {
      case SAVE_KEEPER:
        locationProperty = VideoCaptureProcessorProperty.KEEPERS_LOCATION_PROPERTY;
        //fb = FunctionButtonType.KEEPERS_BUTTON;
        break;
      case SAVE_CLIP:
        locationProperty = VideoCaptureProcessorProperty.CLIPS_LOCATION_PROPERTY;
        //fb = FunctionButtonType.CLIPS_BUTTON;
        break;
      case CLEANUP:
        break;
      case MARK_CLIP:
        break;
      default:
        break;
    }

    File newFile = FileUtility.findNewestFileInDirectory(sourceDirectory.getAbsolutePath());

    if (newFile != null)
    {
      String filename = FileUtility.getFilename(newFile, false);
      new FileMoveTask(filename, new File(VideoCaptureProcessorProperties.getInstance().getProperty(locationProperty)))
          .run();
    }
    else
    {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("File Save Error");
      alert.setHeaderText("Error During File Save");
      alert.setContentText("There are no files in the directory: " + sourceDirectory.getAbsolutePath());
    }
  }

  protected static FileUtilityReturnCode keepFile(String oldFilename, File destDir, String newFilename)
  {
    File[] fileList = FileUtility.getListOfFilesInDirectory(VideoCaptureProcessorProperties.getInstance()
        .getProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY.getName()), oldFilename);

    try
    {
      for (File srcFile : fileList)
      {
        FileUtilityReturnCode result = FileUtility.moveFiles(srcFile, destDir,
            newFilename + FileUtility.getFileExtension(srcFile, true));

        if (result != FileUtilityReturnCode.SUCCESS)
        {
          Alert alert = new Alert(AlertType.WARNING);
          alert.setTitle("Problem Moving Files");
          alert.setHeaderText("File move was unable to complete for the following reason: ");
          alert.setContentText(result.getMessage() + ".\n" + "Please try again.");
          alert.showAndWait();

          // TODO
//          logger.warning("Error encountered while attempting to move file to " + destDir.getCanonicalPath() + " - "
//              + FileUtility.ERROR_DESCRIPTIONS[result]);
          // stopProgressBar("Move Failed");
          // threadRunning = false;
          return result;
        }
      }

      File clipsFile = new File(VideoCaptureProcessorProperties.getInstance().getProperty(
          VideoCaptureProcessorProperty.CLIPS_LOCATION_PROPERTY) + oldFilename + CLIPS_FILE_EXTENSION);
      if (clipsFile.exists())
      {
        FileUtility.moveFiles(clipsFile, destDir, newFilename + CLIPS_FILE_EXTENSION);
      }
    }
    catch (IOException e)
    {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error Moving Files");
      alert.setHeaderText("An error was encountered while attempting to move files. Check your video directories.");
      alert.setContentText(e.getMessage());
      alert.showAndWait();

      // stopProgressBar("Move Failed");
      // threadRunning = false;
      // logger.log(Level.SEVERE, "Error Moving Files", e);
      return FileUtilityReturnCode.FILE_MOVE_ERROR;
    }
    return FileUtilityReturnCode.SUCCESS;
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
