package com.wheezy.apps.vidcapproc.data;

import java.io.IOException;

import com.wheezy.utils.file.properties.PropertiesUtility;

public class VideoCaptureProcessorProperties extends PropertiesUtility
{
  private static final String CONFIG_FILENAME = "CapProcConfig.xml";
  private static VideoCaptureProcessorProperties instance;

  public enum CaptureProcessorProperties
  {
    CAPTURE_LOCATION_PROPERTY("CaptureLoc"),
    KEEPERS_LOCATION_PROPERTY("KeepersLoc"),
    FILE_CHOOSER_LOCATION_PROPERTY("FileChooserLoc"),
    ALWAYS_ON_TOP_PROPERTY("AlwaysOnTop"),
    GAME_SELECTOR_VISIBLE_PROPERTY("GameSelectorShow"),
    SELECTED_GAME_PROPERTY("SelectedGame"),
    FRAME_LOCATION_X_PROPERTY("FrameLocationX"),
    FRAME_LOCATION_Y_PROPERTY("FrameLocationY");

    private String name;

    private CaptureProcessorProperties(String name)
    {
      this.name = name;
    }

    public String getName()
    {
      return name;
    }
  }
  
  public VideoCaptureProcessorProperties() throws IOException
  {
    super(CONFIG_FILENAME);
  }

  public static VideoCaptureProcessorProperties getInstance() throws IOException
  {
    if (instance == null)
    {
      instance = new VideoCaptureProcessorProperties();
    }
    return instance;
  }
}
