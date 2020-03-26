package com.wheezy.apps.vidcapproc;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class VideoCaptureProcessorGUIController implements Initializable
{

  @FXML
  private Button myButton;

  @FXML
  private TextField myTextField;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    // TODO (don't really need to do anything here).
  }

  public void openSettingsDialog(ActionEvent event)
  {
    System.out.println("Button Clicked!");
    
  }
}