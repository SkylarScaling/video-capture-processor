package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
import java.util.logging.Logger;

import com.wheezy.utils.file.FileUtility;
import com.wheezy.utils.file.FileUtilityReturnCode;
import com.wheezy.utils.file.filter.list.FileExtensionListFilter;
import com.wheezy.utils.file.filter.list.FileOnlyListFilter;

import javafx.concurrent.Task;

/**
 * Deletes every file in the capture directory and every clip mark file.
 * <p>
 * Optionally saves the captures that have clip marks first: they are moved, with
 * their marks, to a destination directory. If saving any of them fails, nothing is
 * deleted. Subdirectories of the capture directory are left alone.
 * <p>
 * This task never touches the UI, so it is safe to run on a background thread.
 */
public class FileCleanupTask extends Task<FileCleanupTask.Result>
{
  private static final Logger logger = Logger.getLogger(FileCleanupTask.class.getName());

  /**
   * @param succeeded       false if the cleanup stopped before deleting anything
   * @param savedCaptures   captures with marks that were moved to the save directory
   * @param deletedFiles    capture files deleted
   * @param undeletedFiles  capture files that could not be deleted (for example, still open in a recorder)
   * @param detail          what went wrong when {@code succeeded} is false
   */
  public record Result(boolean succeeded, int savedCaptures, int deletedFiles, int undeletedFiles, String detail)
  {
  }

  private final File captureDir;
  private final File marksDir;
  private final File saveDir;
  private final String gameLabel;

  /**
   * @param captureDir directory whose files are deleted
   * @param marksDir   directory holding the .clips mark files, which are also deleted
   * @param saveDir    where to save captures that have marks before deleting, or null to save nothing
   * @param gameLabel  label of the selected game, used in the saved captures' names, or null
   */
  public FileCleanupTask(File captureDir, File marksDir, File saveDir, String gameLabel)
  {
    this.captureDir = captureDir;
    this.marksDir = marksDir;
    this.saveDir = saveDir;
    this.gameLabel = gameLabel;
  }

  @Override
  protected Result call() throws Exception
  {
    updateMessage("Cleaning up files...");

    int saved = 0;
    if (saveDir != null)
    {
      File[] marks = marksDir.listFiles(new FileExtensionListFilter(ClipMarker.CLIPS_FILE_EXTENSION));
      for (File mark : marks == null ? new File[0] : marks)
      {
        String captureName = FileUtility.getFilename(mark, false);
        FileMoveTask.Result result = FileMoveTask.moveCapture(captureName,
            CaptureNaming.buildName(gameLabel, null, captureName), captureDir, saveDir, marksDir, null);

        if (result.code() == FileUtilityReturnCode.FILE_DOES_NOT_EXIST)
        {
          continue; // a mark for a capture that is already gone: nothing to save
        }
        if (!result.succeeded())
        {
          logger.warning("Cleanup stopped while saving clips: " + result.code().getMessage() + " " + result.detail());
          return new Result(false, saved, 0, 0, result.code().getMessage() + " (" + result.detail() + ")");
        }
        saved++;
      }
    }

    File[] captureFiles = captureDir.listFiles(new FileOnlyListFilter());
    int total = captureFiles == null ? 0 : captureFiles.length;
    int undeleted = total == 0 ? 0 : FileUtility.deleteFilesFromDirectory(captureDir);

    FileUtility.deleteFilesWithExtension(marksDir, ClipMarker.CLIPS_FILE_EXTENSION);

    updateMessage("Cleanup complete");
    return new Result(true, saved, total - undeleted, undeleted, "");
  }
}
