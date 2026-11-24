package com.wheezy.apps.vidcapproc.utils.file.properties;

import com.wheezy.utils.file.properties.PropertiesUtility;

public class VideoCaptureProcessorProperties extends PropertiesUtility
{
  private static final String CONFIG_FILENAME = "CapProcConfig.xml";
  private static VideoCaptureProcessorProperties instance;  

  public String getProperty(VideoCaptureProcessorProperty property)
  {
    return this.getProperty(property.getName());
  }
  
  public void setProperty(VideoCaptureProcessorProperty property, String value)
  {
    this.setProperty(property.getName(), value);
  }
  
  /** Whether the main window stays on top. Defaults to true until the user changes it. */
  public boolean isAlwaysOnTop()
  {
    String value = getProperty(VideoCaptureProcessorProperty.ALWAYS_ON_TOP_PROPERTY);
    return value == null || Boolean.parseBoolean(value);
  }

  public VideoCaptureProcessorProperties()
  {
    super(CONFIG_FILENAME);
  }

  /** Uses a different file than the app's own, which tests use to avoid touching the real settings. */
  public VideoCaptureProcessorProperties(String filename)
  {
    super(filename);
  }

  public static synchronized VideoCaptureProcessorProperties getInstance()
  {
    if (instance == null)
    {
      instance = new VideoCaptureProcessorProperties();
    }
    return instance;
  }

  /** Replaces the app's settings. Tests use this so that they never read or change the real settings file. */
  public static synchronized void setInstance(VideoCaptureProcessorProperties properties)
  {
    instance = properties;
  }
}
