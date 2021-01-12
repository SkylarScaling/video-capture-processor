package com.wheezy.apps.vidcapproc.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.wheezy.apps.vidcapproc.data.VCPSettings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VideoCaptureProcessorGUIController implements Initializable
{

  @FXML
  private Button myButton;
  @FXML
  private TextField myTextField;

  private VCPSettings vcpSettings;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    // TODO (don't really need to do anything here).
  }

  @FXML
  void openSettingsDialog(ActionEvent event)
  {
    System.out.println("Button Clicked!");
    
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../ui/VCPSettingsDialog.fxml"));
    Parent parent;
    try
    {
      parent = fxmlLoader.load();
      VCPSettingsDialogController dialogController = fxmlLoader.<VCPSettingsDialogController>getController();
      dialogController.setVCPSettings(vcpSettings);
      Scene scene = new Scene(parent);
      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setScene(scene);
      stage.showAndWait();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @FXML
  void openGameSelectorDialog(ActionEvent event)
  {
    System.out.println("Game Selector Clicked!");
    
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../ui/GameSelectorDialog.fxml"));
    Parent parent;
    try
    {
      parent = fxmlLoader.load();
      Scene scene = new Scene(parent);
      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setScene(scene);
      stage.showAndWait();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @FXML
  void cleanupFiles(ActionEvent event)
  {
    System.out.println("Cleanup Clicked!");
  }
}