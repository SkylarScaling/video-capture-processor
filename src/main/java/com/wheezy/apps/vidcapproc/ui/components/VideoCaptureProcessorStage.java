package com.wheezy.apps.vidcapproc.ui.components;

import java.io.IOException;

import com.wheezy.components.FXMLStage;

import javafx.fxml.FXMLLoader;

public class VideoCaptureProcessorStage extends FXMLStage
{
  private static final String iconPath = "com/wheezy/apps/vidcapproc/icons/VCPAppIcon.png";

  public VideoCaptureProcessorStage(String resourcePath, String title, boolean modal)
  {
    super(new FXMLLoader(VideoCaptureProcessorStage.class.getClassLoader()
        .getResource(resourcePath)), title, iconPath, modal);

    try
    {
      this.initStage();
    }
    catch (IOException e)
    {
      // If you see this stack trace, you should have tested better.
      e.printStackTrace();
    }
  }
}
