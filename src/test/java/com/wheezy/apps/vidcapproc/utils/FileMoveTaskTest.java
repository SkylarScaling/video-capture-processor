package com.wheezy.apps.vidcapproc.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.utils.file.FileUtilityReturnCode;

import javafx.application.Platform;

public class FileMoveTaskTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void startToolkit()
  {
    // Task.updateMessage/updateProgress hop to the FX thread, so it must be running.
    try
    {
      Platform.startup(() -> {});
    }
    catch (IllegalStateException alreadyStarted)
    {
      // fine: another test started it
    }
  }

  @Test
  public void movesCaptureFilesAndClipsFile() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File destDir = temp.newFolder("dest");
    File clipsDir = temp.newFolder("clips");

    Files.writeString(new File(captureDir, "game1.mp4").toPath(), "video");
    Files.writeString(new File(captureDir, "game1.srt").toPath(), "subs");
    Files.writeString(new File(captureDir, "other.mp4").toPath(), "unrelated");
    Files.writeString(new File(clipsDir, "game1.clips").toPath(), "1:23");

    FileMoveTask.Result result = new FileMoveTask("game1", captureDir, destDir, clipsDir).call();

    assertTrue(result.detail(), result.succeeded());
    assertTrue(new File(destDir, "game1.mp4").exists());
    assertTrue(new File(destDir, "game1.srt").exists());
    assertTrue(new File(destDir, "game1.clips").exists());
    assertFalse(new File(captureDir, "game1.mp4").exists());
    assertFalse(new File(clipsDir, "game1.clips").exists());
    assertTrue("unrelated files stay put", new File(captureDir, "other.mp4").exists());
  }

  @Test
  public void worksWithoutClipsDirectory() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File destDir = temp.newFolder("dest");
    Files.writeString(new File(captureDir, "game2.mp4").toPath(), "video");

    FileMoveTask.Result result = new FileMoveTask("game2", captureDir, destDir, null).call();

    assertTrue(result.detail(), result.succeeded());
    assertTrue(new File(destDir, "game2.mp4").exists());
  }

  @Test
  public void reportsMissingFilesWithoutThrowing() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File destDir = temp.newFolder("dest");

    FileMoveTask.Result result = new FileMoveTask("nothing", captureDir, destDir, null).call();

    assertFalse(result.succeeded());
    assertEquals(FileUtilityReturnCode.FILE_DOES_NOT_EXIST, result.code());
  }

  @Test
  public void reportsInvalidDestination() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    Files.writeString(new File(captureDir, "game3.mp4").toPath(), "video");

    FileMoveTask.Result result = new FileMoveTask("game3", captureDir, new File(temp.getRoot(), "missing"), null).call();

    assertEquals(FileUtilityReturnCode.INVALID_COPY_LOCATION, result.code());
    assertTrue("source left in place on failure", new File(captureDir, "game3.mp4").exists());
  }
}
