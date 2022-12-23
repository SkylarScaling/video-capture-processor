package com.wheezy.apps.vidcapproc.utils.file.properties;

public enum VideoCaptureProcessorProperty
{
  CAPTURE_LOCATION_PROPERTY("CaptureLoc"),
  KEEPERS_LOCATION_PROPERTY("KeepersLoc"),
  CLIPS_LOCATION_PROPERTY("ClipsLoc"),
  FILE_CHOOSER_LOCATION_PROPERTY("FileChooserLoc"),
  ALWAYS_ON_TOP_PROPERTY("AlwaysOnTop"),
  GAME_SELECTOR_VISIBLE_PROPERTY("GameSelectorShow"),
  SELECTED_GAME_PROPERTY("SelectedGame"),
  FRAME_LOCATION_X_PROPERTY("FrameLocationX"),
  FRAME_LOCATION_Y_PROPERTY("FrameLocationY");

  private String name;

  private VideoCaptureProcessorProperty(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
