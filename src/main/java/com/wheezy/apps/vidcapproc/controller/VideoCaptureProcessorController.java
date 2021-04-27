package com.wheezy.apps.vidcapproc.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.wheezy.apps.vidcapproc.data.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;
import com.wheezy.components.FXMLController;
import com.wheezy.utils.ui.AlertDialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;

public class VideoCaptureProcessorController extends FXMLController
{
  private static Logger logger = Logger.getLogger(VideoCaptureProcessorController.class.getName());
  
  private VideoCaptureProcessorProperties propertiesInstance;

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
          "An error was encountered while reading from the properties file.", 
          ioe.getMessage());
      
      logger.log(Level.SEVERE, "Property Read Error", ioe);
    }
  }

  @FXML
  private JFXButton settingsButton;

  @FXML
  private TextField myTextField;

  @FXML
  void openSettingsDialog(ActionEvent event)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../ui/VideoCaptureProcessorSettingsDialog.fxml"));
    
    VideoCaptureProcessorStage stage = new VideoCaptureProcessorStage(fxmlLoader, "Settings", true);
    VideoCaptureProcessorSettingsController settingsDialogController = 
        fxmlLoader.<VideoCaptureProcessorSettingsController>getController();
    settingsDialogController.setStage(stage);
    stage.showAndWait();
  }

  @FXML
  void openGameSelectorDialog(ActionEvent event)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../ui/GameSelectorDialog.fxml"));
    
    VideoCaptureProcessorStage stage = new VideoCaptureProcessorStage(fxmlLoader, "Game Selector", true);
    GameSelectorController gameSelectorController = fxmlLoader.<GameSelectorController>getController();
    gameSelectorController.setStage(stage);
    stage.showAndWait();
  }

  @FXML
  void cleanupFiles(ActionEvent event)
  {
    System.out.println("Cleanup Clicked!");
  }
}