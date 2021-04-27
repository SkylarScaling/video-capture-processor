package com.wheezy.apps.vidcapproc;

import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;

import javafx.application.Application;
import javafx.stage.Stage;

public class VideoCaptureProcessor extends Application
{

  @Override
  public void start(Stage primaryStage)
  {
    try
    {
      primaryStage = new VideoCaptureProcessorStage(VideoCaptureProcessorResources.VIDEO_CAPTURE_PROCESSOR_FXML,
          "Wheezy's VCP", false);
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