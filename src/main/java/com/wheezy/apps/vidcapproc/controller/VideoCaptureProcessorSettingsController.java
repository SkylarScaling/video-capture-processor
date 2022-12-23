package com.wheezy.apps.vidcapproc.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
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
  private JFXButton captureDirButton;
  @FXML
  private JFXButton keepersDirButton;
  @FXML
  private JFXCheckBox alwaysOnTopCheckBox;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    propertiesInstance = VideoCaptureProcessorProperties.getInstance();
    
    captureTextField.setText(propertiesInstance.getProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY));
    keepersTextField.setText(propertiesInstance.getProperty(VideoCaptureProcessorProperty.KEEPERS_LOCATION_PROPERTY));
    alwaysOnTopCheckBox.setSelected(Boolean.parseBoolean(propertiesInstance.getProperty(VideoCaptureProcessorProperty.ALWAYS_ON_TOP_PROPERTY)));
  }

  @FXML
  void browseForCaptureDirectory(ActionEvent event)
  {
    File selectedDirectory = FileUtility.browseForDirectory(getStage(), "Select Capture Directory");
    if (selectedDirectory != null)
    {
      captureTextField.setText(selectedDirectory.getAbsolutePath());
    }
  }

  @FXML
  void browseForKeepersDirectory(ActionEvent event)
  {
    File selectedDirectory = FileUtility.browseForDirectory(getStage(), "Select Keepers Directory");
    if (selectedDirectory != null)
    {
      keepersTextField.setText(selectedDirectory.getAbsolutePath());
    }
  }

  @FXML
  void saveSettings(ActionEvent event)
  {
    propertiesInstance.setProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY,
        captureTextField.getText());

    propertiesInstance.setProperty(VideoCaptureProcessorProperty.KEEPERS_LOCATION_PROPERTY,
        keepersTextField.getText());
    
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
