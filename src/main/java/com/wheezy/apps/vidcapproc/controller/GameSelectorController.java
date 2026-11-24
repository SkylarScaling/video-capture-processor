package com.wheezy.apps.vidcapproc.controller;

import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;
import com.wheezy.components.FXMLController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class GameSelectorController extends FXMLController
{
  @FXML
  void manageGames(ActionEvent event)
  {
    VideoCaptureProcessorStage stage = new VideoCaptureProcessorStage(
        VideoCaptureProcessorResources.GAME_MANAGER_FXML, "Manage Games", true);
    stage.initOwner(getStage());
    stage.showAndWait();
  }
}
