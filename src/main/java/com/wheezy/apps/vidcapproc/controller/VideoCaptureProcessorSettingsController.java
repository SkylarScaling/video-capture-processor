package com.wheezy.apps.vidcapproc.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;
import com.wheezy.components.FXMLController;
import com.wheezy.utils.file.FileUtility;
import com.wheezy.utils.ui.AlertDialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class VideoCaptureProcessorSettingsController extends FXMLController
{
  private static Logger logger = Logger.getLogger(VideoCaptureProcessorSettingsController.class.getName());

  private VideoCaptureProcessorProperties propertiesInstance;

  @FXML
  private TextField captureTextField;
  @FXML
  private TextField keepersTextField;
  @FXML
  private TextField clipsTextField;
  @FXML
  private Button captureDirButton;
  @FXML
  private Button keepersDirButton;
  @FXML
  private Button clipsDirButton;
  @FXML
  private CheckBox alwaysOnTopCheckBox;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    propertiesInstance = VideoCaptureProcessorProperties.getInstance();

    captureTextField.setText(propertiesInstance.getProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY));
    keepersTextField.setText(propertiesInstance.getProperty(VideoCaptureProcessorProperty.KEEPERS_LOCATION_PROPERTY));
    clipsTextField.setText(propertiesInstance.getProperty(VideoCaptureProcessorProperty.CLIPS_LOCATION_PROPERTY));
    alwaysOnTopCheckBox.setSelected(propertiesInstance.isAlwaysOnTop());
  }

  @FXML
  void browseForCaptureDirectory(ActionEvent event)
  {
    browseForDirectory("Select Capture Directory", captureTextField);
  }

  @FXML
  void browseForKeepersDirectory(ActionEvent event)
  {
    browseForDirectory("Select Keepers Directory", keepersTextField);
  }

  @FXML
  void browseForClipsDirectory(ActionEvent event)
  {
    browseForDirectory("Select Clips Directory", clipsTextField);
  }

  /**
   * Lets the user pick a directory into a text field. The chooser opens at the
   * field's current directory, or else wherever the last choice was made, and the
   * choice is remembered for next time.
   */
  private void browseForDirectory(String title, TextField field)
  {
    File initialDirectory = resolveInitialDirectory(field.getText(),
        propertiesInstance.getProperty(VideoCaptureProcessorProperty.FILE_CHOOSER_LOCATION_PROPERTY));

    File selectedDirectory = FileUtility.browseForDirectory(getStage(), title, initialDirectory);
    if (selectedDirectory == null)
    {
      return;
    }

    field.setText(selectedDirectory.getAbsolutePath());

    // Remember the parent: the folders are usually siblings, so the next chooser opens beside this one
    File parent = selectedDirectory.getParentFile();
    propertiesInstance.setProperty(VideoCaptureProcessorProperty.FILE_CHOOSER_LOCATION_PROPERTY,
        (parent != null ? parent : selectedDirectory).getAbsolutePath());
    try
    {
      propertiesInstance.storeProperties();
    }
    catch (IOException ioe)
    {
      // Not worth interrupting the user over
      logger.log(Level.WARNING, "Property Store Error", ioe);
    }
  }

  /**
   * Picks where a directory chooser should open: the directory currently in the
   * text field if it exists, otherwise the last directory chosen, otherwise null
   * (the system default).
   */
  static File resolveInitialDirectory(String currentFieldText, String lastChooserPath)
  {
    for (String candidate : new String[] { currentFieldText, lastChooserPath })
    {
      if (candidate != null && !candidate.isBlank())
      {
        File directory = new File(candidate.strip());
        if (directory.isDirectory())
        {
          return directory;
        }
      }
    }
    return null;
  }

  @FXML
  void saveSettings(ActionEvent event)
  {
    propertiesInstance.setProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY,
        captureTextField.getText());

    propertiesInstance.setProperty(VideoCaptureProcessorProperty.KEEPERS_LOCATION_PROPERTY,
        keepersTextField.getText());

    propertiesInstance.setProperty(VideoCaptureProcessorProperty.CLIPS_LOCATION_PROPERTY,
        clipsTextField.getText());

    propertiesInstance.setProperty(VideoCaptureProcessorProperty.ALWAYS_ON_TOP_PROPERTY,
        Boolean.toString(alwaysOnTopCheckBox.isSelected()));

    try
    {
      propertiesInstance.storeProperties();
    }
    catch (IOException ioe)
    {
      AlertDialog.displayErrorDialog("Error Saving Properties",
          "An error prevented settings from being saved.",
          ioe.getMessage());

      logger.log(Level.SEVERE, "Property Save Error", ioe);
    }

    close(event);
  }
}
