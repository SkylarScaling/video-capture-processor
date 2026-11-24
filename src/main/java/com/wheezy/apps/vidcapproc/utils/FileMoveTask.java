package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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

  /** Moves one file into a directory under a new name. Replaceable so tests can make a move fail. */
  @FunctionalInterface
  interface Mover
  {
    FileUtilityReturnCode move(File srcFile, File destDir, String destFilename) throws IOException;
  }

  /** A file to move, and the name it gets in the destination. */
  private record Step(File source, String newName)
  {
  }

  /** What was done, so a failure can put everything back. */
  private record Moved(File original, File moved)
  {
  }

  /**
   * Moves every file in captureDir whose name starts with {@code filename}, plus
   * the capture's .clips mark file, into destDir as {@code newFilename}.
   * <p>
   * A missing destDir is created. Nothing is moved if a file with a new name is already
   * in destDir. If a move fails part way, the files already moved are put back, and the
   * destDir this call created is removed again.
   *
   * @param progress where to report progress, or null
   */
  static Result moveCapture(String filename, String newFilename, File captureDir, File destDir, File clipsDir,
      ProgressSink progress) throws IOException
  {
    return moveCapture(filename, newFilename, captureDir, destDir, clipsDir, progress, FileUtility::moveFiles);
  }

  static Result moveCapture(String filename, String newFilename, File captureDir, File destDir, File clipsDir,
      ProgressSink progress, Mover mover) throws IOException
  {
    File[] captureFiles = FileUtility.getListOfFilesInDirectory(captureDir.getAbsolutePath(), filename);
    if (captureFiles == null || captureFiles.length == 0)
    {
      return new Result(FileUtilityReturnCode.FILE_DOES_NOT_EXIST,
          "No files starting with '" + filename + "' in " + captureDir.getAbsolutePath());
    }

    List<Step> steps = new ArrayList<>();
    for (File srcFile : captureFiles)
    {
      // Keep each file's own extension; only the name part changes
      steps.add(new Step(srcFile, newFilename + FileUtility.getFileExtension(srcFile, true)));
    }
    File clipsFile = clipsDir == null ? null : new File(clipsDir, filename + CLIPS_FILE_EXTENSION);
    if (clipsFile != null && clipsFile.exists())
    {
      steps.add(new Step(clipsFile, newFilename + CLIPS_FILE_EXTENSION));
    }

    if (destDir.exists() && !destDir.isDirectory())
    {
      return new Result(FileUtilityReturnCode.INVALID_COPY_LOCATION, destDir.getAbsolutePath() + " is not a folder");
    }

    // Better to refuse up front than to fail half way through
    for (Step step : steps)
    {
      File target = new File(destDir, step.newName());
      if (target.exists() && !isSameFile(target, step.source()))
      {
        logger.warning("Not moving " + filename + ": " + target + " already exists");
        return new Result(FileUtilityReturnCode.FILE_MOVE_ERROR,
            step.newName() + " already exists in " + destDir.getAbsolutePath());
      }
    }

    boolean createdDestDir = false;
    if (!destDir.exists())
    {
      try
      {
        Files.createDirectories(destDir.toPath());
        createdDestDir = true;
      }
      catch (IOException e)
      {
        logger.warning("Unable to create " + destDir + ": " + e);
        return new Result(FileUtilityReturnCode.DIRECTORY_CREATION_ERROR, destDir.getAbsolutePath());
      }
    }

    List<Moved> moved = new ArrayList<>();
    try
    {
      for (Step step : steps)
      {
        FileUtilityReturnCode code = mover.move(step.source(), destDir, step.newName());
        if (code != FileUtilityReturnCode.SUCCESS)
        {
          logger.warning("Unable to move " + step.source() + " to " + destDir + ": " + code.getMessage());
          String note = putBack(moved, createdDestDir ? destDir : null);
          return new Result(code, step.source().getName() + note);
        }
        moved.add(new Moved(step.source(), new File(destDir, step.newName())));
        if (progress != null)
        {
          progress.update(moved.size(), steps.size());
        }
      }
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, "Unable to move " + filename + " to " + destDir, e);
      String note = putBack(moved, createdDestDir ? destDir : null);
      throw note.isEmpty() ? e : new IOException(e.getMessage() + note, e);
    }

    return new Result(FileUtilityReturnCode.SUCCESS, newFilename);
  }

  private static boolean isSameFile(File a, File b) throws IOException
  {
    return a.getCanonicalFile().equals(b.getCanonicalFile());
  }

  /**
   * Moves already-moved files back, newest first, and removes the folder this move created
   * if that leaves it empty.
   *
   * @return text to add to the failure detail: empty if nothing had been moved
   */
  private static String putBack(List<Moved> moved, File createdDestDir)
  {
    if (moved.isEmpty())
    {
      if (createdDestDir != null)
      {
        createdDestDir.delete();
      }
      return "";
    }

    List<String> stuck = new ArrayList<>();
    for (int i = moved.size() - 1; i >= 0; i--)
    {
      Moved done = moved.get(i);
      try
      {
        if (!isSameFile(done.original(), done.moved())) // a file that never really moved needs nothing
        {
          Files.move(done.moved().toPath(), done.original().toPath());
        }
      }
      catch (IOException e)
      {
        logger.log(Level.WARNING, "Unable to put " + done.moved() + " back to " + done.original(), e);
        stuck.add(done.moved().getAbsolutePath());
      }
    }

    if (!stuck.isEmpty())
    {
      return ". Some files that were already moved could not be put back: " + String.join(", ", stuck);
    }
    if (createdDestDir != null)
    {
      createdDestDir.delete(); // only succeeds when empty
    }
    return ". The files already moved were put back.";
  }
}
