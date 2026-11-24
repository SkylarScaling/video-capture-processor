package com.wheezy.components;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXMLStage extends Stage
{
  private FXMLLoader fxmlLoader;
  private String title;
  private String iconPath;
  private boolean modal;

  public FXMLStage(FXMLLoader fxmlLoader, String title, String iconPath, boolean modal)
  {
    this.fxmlLoader = fxmlLoader;
    this.title = title;
    this.iconPath = iconPath;
    this.modal = modal;
  }

  public void initStage() throws IOException
  {
    Parent parent;
    parent = fxmlLoader.load();
    Scene scene = new Scene(parent);
    this.setTitle(title);
    this.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);
    this.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(iconPath)));
    this.setScene(scene);

    if (fxmlLoader.getController() instanceof FXMLController)
    {
      ((FXMLController) fxmlLoader.getController()).setStage(this);
    }
  }

  public FXMLController getController()
  {
    return fxmlLoader.getController();
  }
}
