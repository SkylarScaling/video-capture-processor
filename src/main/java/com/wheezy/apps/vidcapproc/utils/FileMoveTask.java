package com.wheezy.apps.vidcapproc.utils;

import java.io.File;

import com.wheezy.utils.file.FileUtilityReturnCode;

import javafx.concurrent.Task;

public class FileMoveTask extends Task<FileUtilityReturnCode>
{
  private final String oldFilename;
  private final File destDir;
  private String newFilename;

  public FileMoveTask(String filename, File destDir)
  {
    oldFilename = filename;
    newFilename = filename;
    this.destDir = destDir;
  }

  @Override
  protected FileUtilityReturnCode call() throws Exception
  {
    FileUtilityReturnCode furc;
    //TODO startProgressBar("Moving File...");

    furc = VideoCaptureProcessorUtils.keepFile(oldFilename, destDir, newFilename);  
        
    //TODO stopProgressBar("Move Complete");
        
    return furc;
  }
}
