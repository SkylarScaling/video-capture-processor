package com.wheezy.apps.vidcapproc.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.wheezy.apps.vidcapproc.data.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.data.VideoCaptureProcessorProperties.CaptureProcessorProperties;
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
    
    captureTextField.setText(propertiesInstance.getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName()));
    keepersTextField.setText(propertiesInstance.getProperty(CaptureProcessorProperties.KEEPERS_LOCATION_PROPERTY.getName()));
    alwaysOnTopCheckBox.setSelected(Boolean.parseBoolean(propertiesInstance.getProperty(CaptureProcessorProperties.ALWAYS_ON_TOP_PROPERTY.getName())));
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
    propertiesInstance.setProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName(),
        captureTextField.getText());

    propertiesInstance.setProperty(CaptureProcessorProperties.KEEPERS_LOCATION_PROPERTY.getName(),
        keepersTextField.getText());
    
    propertiesInstance.setProperty(CaptureProcessorProperties.ALWAYS_ON_TOP_PROPERTY.getName(), 
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

    closeDialog(event);
  }

  @FXML
  void closeDialog(ActionEvent event)
  {
    getStage().close();
  }
}
