package com.wheezy.components;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

public class FXMLController implements Initializable
{
  private Stage stage;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    // (Don't really need to do anything here).
  }

  public Stage getStage()
  {
    return stage;
  }

  public void setStage(Stage stage)
  {
    this.stage = stage;
  }  

  @FXML
  public void close(ActionEvent event)
  {
    getStage().close();
  }
}
