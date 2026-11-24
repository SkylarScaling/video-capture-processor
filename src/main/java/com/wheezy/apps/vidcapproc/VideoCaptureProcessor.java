package com.wheezy.apps.vidcapproc;

import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.WindowSettings;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.utils.ui.AlertDialog;

import javafx.application.Application;
import javafx.stage.Stage;

public class VideoCaptureProcessor extends Application
{

  @Override
  public void start(Stage primaryStage)
  {
    try
    {
      // Creating the stage loads the FXML, and the controller loads the properties file
      primaryStage = new VideoCaptureProcessorStage(VideoCaptureProcessorResources.VIDEO_CAPTURE_PROCESSOR_FXML,
          "Wheezy's VCP", false);

      VideoCaptureProcessorProperties props = VideoCaptureProcessorProperties.getInstance();
      WindowSettings.apply(primaryStage, props);

      // Error dialogs belong to the main window so they can't open behind it when it is always on top
      AlertDialog.setDefaultOwner(primaryStage);

      final Stage mainStage = primaryStage;
      mainStage.setOnHiding(event -> WindowSettings.rememberPosition(mainStage, props));

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
