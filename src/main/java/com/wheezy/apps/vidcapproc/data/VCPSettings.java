package com.wheezy.apps.vidcapproc.data;

import java.io.File;

public class VCPSettings
{
  private File captureDirectory;
  private File keepersDirectory;
  private File clipsDirectory;

  public File getCaptureDirectory()
  {
    return captureDirectory;
  }

  public void setCaptureDirectory(File captureDirectory)
  {
    this.captureDirectory = captureDirectory;
  }

  public File getKeepersDirectory()
  {
    return keepersDirectory;
  }

  public void setKeepersDirectory(File keepersDirectory)
  {
    this.keepersDirectory = keepersDirectory;
  }

  public File getClipsDirectory()
  {
    return clipsDirectory;
  }

  public void setClipsDirectory(File clipsDirectory)
  {
    this.clipsDirectory = clipsDirectory;
  }
}
