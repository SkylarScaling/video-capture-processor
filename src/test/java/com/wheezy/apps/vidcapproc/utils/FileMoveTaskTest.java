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
    File notAFolder = temp.newFile("not-a-folder");

    FileMoveTask.Result result = new FileMoveTask("game3", captureDir, notAFolder, null).call();

    assertEquals(FileUtilityReturnCode.INVALID_COPY_LOCATION, result.code());
    assertTrue("source left in place on failure", new File(captureDir, "game3.mp4").exists());
  }

  @Test
  public void clipsFileSurvivesWhenClipsDirIsAlsoTheDestination() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File clipsDir = temp.newFolder("clips");
    Files.writeString(new File(captureDir, "game4.mp4").toPath(), "video");
    Files.writeString(new File(clipsDir, "game4.clips").toPath(), "1:23");

    FileMoveTask.Result result = new FileMoveTask("game4", captureDir, clipsDir, clipsDir).call();

    assertTrue(result.detail(), result.succeeded());
    assertTrue(new File(clipsDir, "game4.mp4").exists());
    assertEquals("1:23", Files.readString(new File(clipsDir, "game4.clips").toPath()));
  }

  @Test
  public void markedClipsTravelWithTheCapture() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File keepersDir = temp.newFolder("keepers");
    File clipsDir = new File(temp.getRoot(), "clips"); // created by the first mark
    String name = "2024-05-17 20-14-06";
    Files.writeString(new File(captureDir, name + ".mp4").toPath(), "video");

    ClipMarker.mark(name, clipsDir, "ace", java.time.LocalDateTime.of(2024, 5, 17, 20, 16, 1));
    FileMoveTask.Result result = new FileMoveTask(name, captureDir, keepersDir, clipsDir).call();

    assertTrue(result.detail(), result.succeeded());
    assertEquals("1:55 (ace)" + System.lineSeparator(),
        Files.readString(new File(keepersDir, name + ".clips").toPath()));
    assertFalse("mark file leaves the clips directory", new File(clipsDir, name + ".clips").exists());
  }

  @Test
  public void renamesAllFilesAndTheMarkFileKeepingExtensions() throws Exception
  {
    File captureDir = temp.newFolder("capture");
    File destDir = temp.newFolder("dest");
    File clipsDir = temp.newFolder("clips");
    Files.writeString(new File(captureDir, "game5.mp4").toPath(), "video");
    Files.writeString(new File(captureDir, "game5.srt").toPath(), "subs");
    Files.writeString(new File(clipsDir, "game5.clips").toPath(), "0:30");

    FileMoveTask.Result result = new FileMoveTask("game5", "ace_game5", captureDir, destDir, clipsDir).call();

    assertTrue(result.detail(), result.succeeded());
    assertEquals("ace_game5", result.detail());
    assertTrue(new File(destDir, "ace_game5.mp4").exists());
    assertTrue(new File(destDir, "ace_game5.srt").exists());
    assertTrue(new File(destDir, "ace_game5.clips").exists());
    assertFalse(new File(destDir, "game5.mp4").exists());
    assertFalse(new File(captureDir, "game5.mp4").exists());
  }

  private File capture(String... names) throws Exception
  {
    File captureDir = temp.newFolder("capture");
    for (String name : names)
    {
      Files.writeString(new File(captureDir, name).toPath(), "data " + name);
    }
    return captureDir;
  }

  @Test
  public void createsAMissingDestinationFolder() throws Exception
  {
    File captureDir = capture("game3.mp4", "game3.txt");
    File dest = new File(temp.getRoot(), "keepers/2024/may"); // two levels missing

    FileMoveTask.Result result = new FileMoveTask("game3", captureDir, dest, null).call();

    assertTrue(result.detail(), result.succeeded());
    assertTrue(new File(dest, "game3.mp4").exists());
    assertTrue(new File(dest, "game3.txt").exists());
    assertFalse(new File(captureDir, "game3.mp4").exists());
  }

  @Test
  public void doesNotCreateTheDestinationWhenThereIsNothingToMove() throws Exception
  {
    File captureDir = capture("other.mp4");
    File dest = new File(temp.getRoot(), "keepers");

    FileMoveTask.Result result = new FileMoveTask("game3", captureDir, dest, null).call();

    assertEquals(FileUtilityReturnCode.FILE_DOES_NOT_EXIST, result.code());
    assertFalse("no stray empty folder", dest.exists());
  }

  @Test
  public void reportsADestinationItCannotCreate() throws Exception
  {
    File captureDir = capture("game3.mp4");
    File blocker = temp.newFile("blocker"); // a file where a parent folder would have to be

    FileMoveTask.Result result = new FileMoveTask("game3", captureDir, new File(blocker, "keepers"), null).call();

    assertFalse(result.succeeded());
    assertTrue(new File(captureDir, "game3.mp4").exists());
  }

  @Test
  public void refusesToOverwriteAndMovesNothing() throws Exception
  {
    File captureDir = capture("game3.mp4", "game3.txt");
    File dest = temp.newFolder("keepers");
    Files.writeString(new File(dest, "game3.txt").toPath(), "older file");

    FileMoveTask.Result result = new FileMoveTask("game3", captureDir, dest, null).call();

    assertEquals(FileUtilityReturnCode.FILE_MOVE_ERROR, result.code());
    assertTrue(result.detail(), result.detail().contains("game3.txt already exists"));
    assertTrue("nothing moved", new File(captureDir, "game3.mp4").exists());
    assertTrue(new File(captureDir, "game3.txt").exists());
    assertFalse(new File(dest, "game3.mp4").exists());
    assertEquals("the older file is untouched", "older file", Files.readString(new File(dest, "game3.txt").toPath()));
  }

  @Test
  public void putsEarlierFilesBackWhenALaterMoveReportsAFailure() throws Exception
  {
    File captureDir = capture("game3.mp4", "game3.txt");
    File dest = new File(temp.getRoot(), "keepers"); // created by the move, so it should be removed again
    int[] calls = { 0 };

    FileMoveTask.Result result = FileMoveTask.moveCapture("game3", "game3", captureDir, dest, null, null,
        (src, destDir, name) ->
        {
          if (++calls[0] == 2)
          {
            return FileUtilityReturnCode.FILE_MOVE_ERROR;
          }
          return com.wheezy.utils.file.FileUtility.moveFiles(src, destDir, name);
        });

    assertEquals(FileUtilityReturnCode.FILE_MOVE_ERROR, result.code());
    assertTrue(result.detail(), result.detail().contains("put back"));
    assertEquals(2, calls[0]);
    assertEquals(2, captureDir.list().length);
    assertTrue(new File(captureDir, "game3.mp4").exists());
    assertTrue(new File(captureDir, "game3.txt").exists());
    assertEquals("contents survived", "data game3.mp4", Files.readString(new File(captureDir, "game3.mp4").toPath()));
    assertFalse("the folder the move created is gone again", dest.exists());
  }

  @Test
  public void putsEarlierFilesBackWhenALaterMoveThrows() throws Exception
  {
    File captureDir = capture("game3.mp4", "game3.txt");
    File dest = temp.newFolder("keepers"); // already existed, so it must stay
    int[] calls = { 0 };

    try
    {
      FileMoveTask.moveCapture("game3", "game3", captureDir, dest, null, null, (src, destDir, name) ->
      {
        if (++calls[0] == 2)
        {
          throw new java.io.IOException("disk went away");
        }
        return com.wheezy.utils.file.FileUtility.moveFiles(src, destDir, name);
      });
      org.junit.Assert.fail("expected the I/O error to be thrown");
    }
    catch (java.io.IOException expected)
    {
      assertTrue(expected.getMessage(), expected.getMessage().contains("disk went away"));
    }

    assertTrue(new File(captureDir, "game3.mp4").exists());
    assertTrue(new File(captureDir, "game3.txt").exists());
    assertEquals(0, dest.list().length);
    assertTrue(dest.exists());
  }

  @Test
  public void saysSoWhenAFileCannotBePutBack() throws Exception
  {
    File captureDir = capture("game3.mp4", "game3.txt");
    File dest = temp.newFolder("keepers");
    int[] calls = { 0 };
    File[] files = captureDir.listFiles();
    java.util.Arrays.sort(files);

    FileMoveTask.Result result = FileMoveTask.moveCapture("game3", "game3", captureDir, dest, null, null,
        (src, destDir, name) ->
        {
          if (++calls[0] == 2)
          {
            // Something new appears where the first file came from, so it cannot go back
            Files.writeString(files[0].toPath(), "a new file");
            return FileUtilityReturnCode.FILE_MOVE_ERROR;
          }
          return com.wheezy.utils.file.FileUtility.moveFiles(src, destDir, name);
        });

    assertFalse(result.succeeded());
    assertTrue(result.detail(), result.detail().contains("could not be put back"));
    assertTrue("it is left where it can be found", new File(dest, files[0].getName()).exists());
  }
}
