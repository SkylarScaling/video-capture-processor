package com.wheezy.apps.vidcapproc.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.wheezy.apps.vidcapproc.data.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.data.VideoCaptureProcessorProperties.CaptureProcessorProperties;
import com.wheezy.components.controller.FXMLController;
import com.wheezy.utils.file.FileUtility;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

public class VideoCaptureProcessorSettingsDialogController extends FXMLController
{
  private static Logger logger = Logger.getLogger(VideoCaptureProcessorSettingsDialogController.class.getName());

  @FXML
  private TextField captureTextField;
  @FXML
  private TextField keepersTextField;
  @FXML
  private JFXButton captureDirButton;
  @FXML
  private JFXButton keepersDirButton;

  private VideoCaptureProcessorProperties propertiesInstance;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    try
    {
      propertiesInstance = VideoCaptureProcessorProperties.getInstance();
    }
    catch (IOException ioe)
    {
      //TODO Abstract alert utility
      //https://code.makery.ch/blog/javafx-dialogs-official/
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error Reading Properties");
      alert.setHeaderText("An error was encountered while reading from the properties file.");
      alert.setContentText(ioe.getMessage());
      logger.log(Level.SEVERE, "Property Read Error", ioe);
    }
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

    try
    {
      propertiesInstance.storeProperties();
    }
    catch (IOException ioe)
    {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error Saving Properties");
      alert.setHeaderText("An error prevented settings from being saved.");
      alert.setContentText(ioe.getMessage());
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
