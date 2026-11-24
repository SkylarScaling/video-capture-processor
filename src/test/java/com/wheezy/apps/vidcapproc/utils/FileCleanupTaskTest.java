package com.wheezy.apps.vidcapproc.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javafx.application.Platform;

public class FileCleanupTaskTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void startToolkit()
  {
    // Task.updateMessage hops to the FX thread, so it must be running.
    try
    {
      Platform.startup(() -> {});
    }
    catch (IllegalStateException alreadyStarted)
    {
      // fine: another test started it
    }
  }

  private File write(File dir, String name) throws Exception
  {
    File file = new File(dir, name);
    Files.writeString(file.toPath(), "x");
    return file;
  }

  @Test
  public void deletesCaptureFilesAndMarksButNothingElse() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File marksDir = temp.newFolder("clips");
    write(captureDir, "a.mp4");
    write(captureDir, "b.mp4");
    File subDir = new File(captureDir, "keep-me");
    assertTrue(subDir.mkdir());
    File inSubDir = write(subDir, "nested.mp4");
    write(marksDir, "a.clips");
    File notAMark = write(marksDir, "readme.txt");

    FileCleanupTask.Result result = new FileCleanupTask(captureDir, marksDir, null, null).call();

    assertTrue(result.detail(), result.succeeded());
    assertEquals(2, result.deletedFiles());
    assertEquals(0, result.undeletedFiles());
    assertFalse(new File(captureDir, "a.mp4").exists());
    assertFalse(new File(captureDir, "b.mp4").exists());
    assertFalse(new File(marksDir, "a.clips").exists());
    assertTrue("subdirectories are left alone", inSubDir.exists());
    assertTrue("only .clips files are removed from the marks directory", notAMark.exists());
  }

  @Test
  public void savesMarkedCapturesWithTheirMarksBeforeDeleting() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File marksDir = temp.newFolder("clips");
    File saveDir = temp.newFolder("saved");
    write(captureDir, "marked.mp4");
    write(captureDir, "marked.srt");
    write(captureDir, "unmarked.mp4");
    write(marksDir, "marked.clips");
    write(marksDir, "gone.clips"); // a mark whose capture no longer exists

    FileCleanupTask.Result result = new FileCleanupTask(captureDir, marksDir, saveDir, null).call();

    assertTrue(result.detail(), result.succeeded());
    assertEquals(1, result.savedCaptures());
    assertTrue(new File(saveDir, "marked.mp4").exists());
    assertTrue(new File(saveDir, "marked.srt").exists());
    assertTrue(new File(saveDir, "marked.clips").exists());
    assertFalse("unmarked captures are not saved", new File(saveDir, "unmarked.mp4").exists());
    assertEquals("only the saved capture and its mark were kept", 3, saveDir.list().length);
    assertEquals("the unmarked capture was deleted", 0, captureDir.list().length);
    assertEquals("all marks are gone, including the orphan", 0, marksDir.list().length);
    assertEquals(1, result.deletedFiles());
  }

  @Test
  public void savedCapturesCanCarryTheGameLabel() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File marksDir = temp.newFolder("clips");
    File saveDir = temp.newFolder("saved");
    write(captureDir, "round1.mp4");
    write(marksDir, "round1.clips");

    FileCleanupTask.Result result = new FileCleanupTask(captureDir, marksDir, saveDir, "BF3").call();

    assertTrue(result.detail(), result.succeeded());
    assertTrue(new File(saveDir, "BF3_round1.mp4").exists());
    assertTrue(new File(saveDir, "BF3_round1.clips").exists());
  }

  @Test
  public void deletesNothingIfSavingClipsFails() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File marksDir = temp.newFolder("clips");
    File missingSaveDir = new File(temp.getRoot(), "no-such-folder");
    write(captureDir, "marked.mp4");
    write(captureDir, "other.mp4");
    write(marksDir, "marked.clips");

    FileCleanupTask.Result result = new FileCleanupTask(captureDir, marksDir, missingSaveDir, null).call();

    assertFalse(result.succeeded());
    assertEquals(0, result.deletedFiles());
    assertTrue(new File(captureDir, "marked.mp4").exists());
    assertTrue(new File(captureDir, "other.mp4").exists());
    assertTrue("marks are kept so nothing is lost", new File(marksDir, "marked.clips").exists());
  }

  @Test
  public void reportsFilesItCouldNotDelete() throws Exception
  {
    // Windows refuses to delete a file another program has open, like a recorder still writing
    Assume.assumeTrue(System.getProperty("os.name").toLowerCase().contains("win"));

    File captureDir = temp.newFolder("capture");
    File marksDir = temp.newFolder("clips");
    write(captureDir, "done.mp4");
    File busy = write(captureDir, "recording.mp4");

    try (FileOutputStream stillWriting = new FileOutputStream(busy, true))
    {
      FileCleanupTask.Result result = new FileCleanupTask(captureDir, marksDir, null, null).call();

      assertTrue(result.succeeded());
      assertEquals(1, result.deletedFiles());
      assertEquals(1, result.undeletedFiles());
      assertTrue(busy.exists());
    }
  }

  @Test
  public void cleaningAnEmptyDirectoryIsFine() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File marksDir = new File(temp.getRoot(), "clips-that-do-not-exist");

    FileCleanupTask.Result result = new FileCleanupTask(captureDir, marksDir, null, null).call();

    assertTrue(result.succeeded());
    assertEquals(0, result.deletedFiles());
    assertEquals(0, result.undeletedFiles());
  }
}
