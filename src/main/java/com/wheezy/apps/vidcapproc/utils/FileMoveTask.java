package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.wheezy.utils.file.FileUtility;
import com.wheezy.utils.file.FileUtilityReturnCode;

import javafx.concurrent.Task;

/**
 * Moves a capture (and its matching ".clips" file, if any) to a destination
 * directory, optionally under a new name.
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

  /** Receives progress as files are moved. */
  @FunctionalInterface
  interface ProgressSink
  {
    void update(long done, long total);
  }

  private final String filename;
  private final String newFilename;
  private final File captureDir;
  private final File destDir;
  private final File clipsDir;

  /** Moves the capture without renaming it. See the other constructor. */
  public FileMoveTask(String filename, File captureDir, File destDir, File clipsDir)
  {
    this(filename, filename, captureDir, destDir, clipsDir);
  }

  /**
   * @param filename    capture filename without extension
   * @param newFilename name (without extension) the moved files get
   * @param captureDir  directory the capture files are currently in
   * @param destDir     directory to move the capture files to
   * @param clipsDir    directory holding the .clips mark file, or null if there is none
   */
  public FileMoveTask(String filename, String newFilename, File captureDir, File destDir, File clipsDir)
  {
    this.filename = filename;
    this.newFilename = newFilename;
    this.captureDir = captureDir;
    this.destDir = destDir;
    this.clipsDir = clipsDir;
  }

  @Override
  protected Result call() throws Exception
  {
    updateMessage("Moving " + filename + "...");

    Result result = moveCapture(filename, newFilename, captureDir, destDir, clipsDir, this::updateProgress);
    if (result.succeeded())
    {
      updateMessage("Moved " + newFilename);
    }
    return result;
  }

  /**
   * Moves every file in captureDir whose name starts with {@code filename}, plus
   * the capture's .clips mark file, into destDir as {@code newFilename}.
   *
   * @param progress where to report progress, or null
   */
  static Result moveCapture(String filename, String newFilename, File captureDir, File destDir, File clipsDir,
      ProgressSink progress) throws IOException
  {
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
      // Keep each file's own extension; only the name part changes
      FileUtilityReturnCode code = FileUtility.moveFiles(srcFile, destDir,
          newFilename + FileUtility.getFileExtension(srcFile, true));
      if (code != FileUtilityReturnCode.SUCCESS)
      {
        logger.warning("Unable to move " + srcFile + " to " + destDir + ": " + code.getMessage());
        return new Result(code, srcFile.getName());
      }
      if (progress != null)
      {
        progress.update(++done, total);
      }
    }

    if (moveClips)
    {
      FileUtilityReturnCode code = FileUtility.moveFiles(clipsFile, destDir, newFilename + CLIPS_FILE_EXTENSION);
      if (code != FileUtilityReturnCode.SUCCESS)
      {
        logger.warning("Unable to move " + clipsFile + " to " + destDir + ": " + code.getMessage());
        return new Result(code, clipsFile.getName());
      }
      if (progress != null)
      {
        progress.update(++done, total);
      }
    }

    return new Result(FileUtilityReturnCode.SUCCESS, newFilename);
  }
}
