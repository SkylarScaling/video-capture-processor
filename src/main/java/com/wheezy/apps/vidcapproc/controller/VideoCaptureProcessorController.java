package com.wheezy.apps.vidcapproc.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;
import com.wheezy.apps.vidcapproc.ui.elements.GameSelectorButton;
import com.wheezy.apps.vidcapproc.utils.SaveActionType;
import com.wheezy.apps.vidcapproc.utils.VideoCaptureProcessorUtils;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;
import com.wheezy.components.FXMLController;
import com.wheezy.utils.ui.AlertDialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class VideoCaptureProcessorController extends FXMLController
{
  private static Logger logger = Logger.getLogger(VideoCaptureProcessorController.class.getName());

  private VideoCaptureProcessorProperties propertiesInstance;
  
  @FXML  
  private GameSelectorButton gameButton;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    super.initialize(location, resources);

    try
    {
      propertiesInstance = VideoCaptureProcessorProperties.getInstance();
      propertiesInstance.loadProperties();
    }
    catch (IOException ioe)
    {
      AlertDialog.displayErrorDialog("Error Reading Properties",
          "An error was encountered while reading from the properties file.", ioe.getMessage());

      logger.log(Level.SEVERE, "Property Read Error", ioe);
    }
  }

  @FXML
  void saveVideo(ActionEvent event)
  {
    VideoCaptureProcessorUtils.saveNewestFile(SaveActionType.SAVE_CLIP, new File(propertiesInstance.getProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY)));
  }

  @FXML
  void openSettingsDialog(ActionEvent event)
  {
    VideoCaptureProcessorStage stage = new VideoCaptureProcessorStage(
        VideoCaptureProcessorResources.SETTINGS_DIALOG_FXML, "Settings", true);
    stage.showAndWait();
  }

  @FXML
  void openGameSelectorDialog(ActionEvent event)
  {
    VideoCaptureProcessorStage stage = new VideoCaptureProcessorStage(
        VideoCaptureProcessorResources.GAME_SELECTOR_DIALOG_FXML, "Game Selector", true);
    stage.showAndWait();
  }

  @FXML
  void cleanupFiles(ActionEvent event)
  {
    System.out.println("Cleanup Clicked!");
    //TODO REMOVE (Testing only)
    VideoCaptureProcessorStage stage = new VideoCaptureProcessorStage(
        VideoCaptureProcessorResources.BUTTON_PANEL_DIALOG_FXML, "Test Panel", true);
    stage.showAndWait();
  }
}