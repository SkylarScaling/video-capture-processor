package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
import java.util.logging.Logger;

import com.wheezy.utils.file.FileUtility;
import com.wheezy.utils.file.FileUtilityReturnCode;

import javafx.concurrent.Task;

/**
 * Moves a capture (and its matching ".clips" file, if any) to a destination
 * directory.
 * <p>
 * This task never touches the UI, so it is safe to run on a background thread.
 * A move that completes with a problem is reported through {@link Result};
 * unexpected I/O errors are thrown and surface as a failed task.
 */
public class FileMoveTask extends Task<FileMoveTask.Result>
{
  private static final Logger logger = Logger.getLogger(FileMoveTask.class.getName());
  private static final String CLIPS_FILE_EXTENSION = ClipMarker.CLIPS_FILE_EXTENSION;

  public record Result(FileUtilityReturnCode code, String detail)
  {
    public boolean succeeded()
    {
      return code == FileUtilityReturnCode.SUCCESS;
    }
  }

  private final String filename;
  private final File captureDir;
  private final File destDir;
  private final File clipsDir;

  /**
   * @param filename   capture filename without extension
   * @param captureDir directory the capture files are currently in
   * @param destDir    directory to move the capture files to
   * @param clipsDir   directory holding the .clips mark file, or null if there is none
   */
  public FileMoveTask(String filename, File captureDir, File destDir, File clipsDir)
  {
    this.filename = filename;
    this.captureDir = captureDir;
    this.destDir = destDir;
    this.clipsDir = clipsDir;
  }

  @Override
  protected Result call() throws Exception
  {
    updateMessage("Moving " + filename + "...");

    File[] captureFiles = FileUtility.getListOfFilesInDirectory(captureDir.getAbsolutePath(), filename);
    if (captureFiles == null || captureFiles.length == 0)
    {
      return new Result(FileUtilityReturnCode.FILE_DOES_NOT_EXIST,
          "No files starting with '" + filename + "' in " + captureDir.getAbsolutePath());
    }

    File clipsFile = clipsDir == null ? null : new File(clipsDir, filename + CLIPS_FILE_EXTENSION);
    boolean moveClips = clipsFile != null && clipsFile.exists();
    int total = captureFiles.length + (moveClips ? 1 : 0);
    int done = 0;

    for (File srcFile : captureFiles)
    {
      FileUtilityReturnCode code = FileUtility.moveFiles(srcFile, destDir,
          filename + FileUtility.getFileExtension(srcFile, true));
      if (code != FileUtilityReturnCode.SUCCESS)
      {
        logger.warning("Unable to move " + srcFile + " to " + destDir + ": " + code.getMessage());
        return new Result(code, srcFile.getName());
      }
      updateProgress(++done, total);
    }

    if (moveClips)
    {
      FileUtilityReturnCode code = FileUtility.moveFiles(clipsFile, destDir, filename + CLIPS_FILE_EXTENSION);
      if (code != FileUtilityReturnCode.SUCCESS)
      {
        logger.warning("Unable to move " + clipsFile + " to " + destDir + ": " + code.getMessage());
        return new Result(code, clipsFile.getName());
      }
      updateProgress(++done, total);
    }

    updateMessage("Moved " + filename);
    return new Result(FileUtilityReturnCode.SUCCESS, filename);
  }
}
