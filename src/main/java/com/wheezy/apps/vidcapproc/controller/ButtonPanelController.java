package com.wheezy.apps.vidcapproc.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.wheezy.apps.vidcapproc.utils.VideoCaptureProcessorUtils;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;
import com.wheezy.components.FXMLController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ButtonPanelController extends FXMLController
{
  private static Logger logger = Logger.getLogger(VideoCaptureProcessorController.class.getName());
  
  private VideoCaptureProcessorProperties propertiesInstance;
  
  @FXML  
  private JFXButton keepersButton;
  
  @FXML  
  private JFXButton clipsButton;
  
  @FXML  
  private JFXButton cleanupButton;
  
  @FXML  
  private JFXButton markClipButton;
  
  @FXML  
  private JFXButton noGameButton;
  
  @FXML  
  private JFXButton game1Button;
  
  @FXML  
  private JFXButton game2Button;
  
  @FXML  
  private TextField clipTextField;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    super.initialize(location, resources);

    propertiesInstance = VideoCaptureProcessorProperties.getInstance();
  }

  @FXML
  void saveKeeper(ActionEvent event)
  {
    
  }

  @FXML
  void saveClip(ActionEvent event)
  {
    
  }
  
  @FXML
  void markClip(ActionEvent event)
  {
    
  }

  @FXML
  void cleanupFiles(ActionEvent event)
  {
    
  }
}
