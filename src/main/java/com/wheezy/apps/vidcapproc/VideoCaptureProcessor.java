package com.wheezy.apps.vidcapproc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class VideoCaptureProcessor extends Application
{

  @Override
  public void start(Stage primaryStage)
  {
    try
    {
      // Read file fxml and draw interface.
      Parent root = FXMLLoader.load(getClass().getResource("ui/VideoCaptureProcessor.fxml"));

      primaryStage.setTitle("Wheezy's VCP");
      primaryStage.getIcons().add(new Image(
          getClass().getClassLoader().getResourceAsStream("com/wheezy/apps/vidcapproc/icons/VCPAppIcon.png")));
      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    launch(args);
  }
}