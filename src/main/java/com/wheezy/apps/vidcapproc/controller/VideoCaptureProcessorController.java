package com.wheezy.apps.vidcapproc.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;
import com.wheezy.apps.vidcapproc.ui.elements.GameSelectorButton;
import com.wheezy.apps.vidcapproc.utils.FileMoveTask;
import com.wheezy.apps.vidcapproc.utils.SaveActionType;
import com.wheezy.apps.vidcapproc.utils.VideoCaptureProcessorUtils;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.components.FXMLController;
import com.wheezy.utils.ui.AlertDialog;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VideoCaptureProcessorController extends FXMLController
{
  private static Logger logger = Logger.getLogger(VideoCaptureProcessorController.class.getName());

  private VideoCaptureProcessorProperties propertiesInstance;
  
  @FXML
  private GameSelectorButton gameButton;

  @FXML
  private Button keepersButton;

  @FXML
  private Button clipsButton;

  @FXML
  private TextField clipLabelField;

  @FXML
  private Label statusLabel;

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
  void saveKeeper(ActionEvent event)
  {
    startSave(SaveActionType.SAVE_KEEPER);
  }

  @FXML
  void saveClip(ActionEvent event)
  {
    startSave(SaveActionType.SAVE_CLIP);
  }

  @FXML
  void markClip(ActionEvent event)
  {
    String clipTime = VideoCaptureProcessorUtils.markClip(clipLabelField.getText());
    if (clipTime != null)
    {
      statusLabel.setText("Clip marked: " + clipTime);
      clipLabelField.clear();
    }
  }

  private void startSave(SaveActionType action)
  {
    FileMoveTask task = VideoCaptureProcessorUtils.saveNewestFile(action);
    if (task == null)
    {
      return;
    }

    // Block repeat clicks until the move finishes
    keepersButton.setDisable(true);
    clipsButton.setDisable(true);
    task.messageProperty().addListener((observable, oldMessage, newMessage) -> statusLabel.setText(newMessage));
    task.stateProperty().addListener((observable, oldState, newState) ->
    {
      if (newState == Worker.State.SUCCEEDED || newState == Worker.State.FAILED || newState == Worker.State.CANCELLED)
      {
        boolean moved = newState == Worker.State.SUCCEEDED && task.getValue().succeeded();
        statusLabel.setText(moved ? "Move complete" : "Move failed");
        keepersButton.setDisable(false);
        clipsButton.setDisable(false);
      }
    });
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