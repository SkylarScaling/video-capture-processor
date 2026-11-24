package com.wheezy.utils.file.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtility
{
  private final Properties props = new Properties();
  private String propsFilename;
  private File propsFile;

  public final String PROPERTIES_FILE_PATH_KEY = "propertiesFilePath";

  /**
   * Utility for managing properties files.
   */
  public PropertiesUtility(String filename)
  {
    propsFilename = filename;
    propsFile = new File(propsFilename);
  }

  private void checkFile() throws IOException
  {
    if (!propsFile.exists())
    {
      propsFile.createNewFile();
      props.setProperty(PROPERTIES_FILE_PATH_KEY, propsFile.getCanonicalPath());
      storeProperties();
    }
  }

  public String getProperty(String key)
  {
    return props.getProperty(key);
  }

  public void setProperty(String key, String value)
  {
    props.setProperty(key, value);
  }

  public void storeProperties() throws IOException
  {
    FileOutputStream propFOS = null;
    try
    {
      propFOS = new FileOutputStream(propsFile);
      props.storeToXML(propFOS, null);
    }
    finally
    {
      try
      {
        if (propFOS != null)
        {
          propFOS.close();
        }
      }
      catch (IOException e)
      {
        // Don't care
      }
    }
  }

  public void loadProperties() throws IOException
  {
    FileInputStream propFIS = null;
    try
    {
      checkFile();
      propFIS = new FileInputStream(propsFilename);
      props.loadFromXML(propFIS);
      propFIS.close();
    }
    finally
    {
      if (propFIS != null)
      {
        try
        {
          propFIS.close();
        }
        catch (IOException e)
        {
          // Don't care
        }
      }
    }
  }
}
