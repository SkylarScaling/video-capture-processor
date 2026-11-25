package com.wheezy.apps.vidcapproc.controller;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.FxTestSupport.AppData;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.utils.SaveActionType;
import com.wheezy.apps.vidcapproc.utils.VideoCaptureProcessorUtils;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;

/** Saving into a folder per game, through the same call the Keepers and Clips buttons make. */
public class MainWindowGameFoldersTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  private AppData setUp(boolean perGame, String gameTitle) throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    File capture = temp.newFolder("capture");
    Files.writeString(new File(capture, "2024-05-17 20-14-06.mp4").toPath(), "video");
    data.properties().setProperty(VideoCaptureProcessorProperty.CAPTURE_LOCATION_PROPERTY, capture.getPath());
    data.properties().setProperty(VideoCaptureProcessorProperty.KEEPERS_LOCATION_PROPERTY,
        new File(temp.getRoot(), "keepers").getPath());
    data.properties().setProperty(VideoCaptureProcessorProperty.CLIPS_LOCATION_PROPERTY,
        new File(temp.getRoot(), "clips-dest").getPath());
    data.properties().setProperty(VideoCaptureProcessorProperty.GAME_FOLDERS_PROPERTY, Boolean.toString(perGame));
    if (gameTitle != null)
    {
      Game game = new Game();
      game.setGameTitle(gameTitle);
      game.setFilenameLabel("BF3");
      game.setDisplayed(true);
      data.catalog().add(game);
      data.selection().select(game);
    }
    return data;
  }

  private static void waitFor(File file) throws Exception
  {
    for (int i = 0; i < 100 && !file.exists(); i++)
    {
      Thread.sleep(100);
    }
  }

  @Test
  public void keepersGoIntoTheSelectedGamesFolder() throws Exception
  {
    setUp(true, "Battlefield 3");

    onFxThread(() -> VideoCaptureProcessorUtils.saveNewestFile(SaveActionType.SAVE_KEEPER, null));

    File saved = new File(temp.getRoot(), "keepers/Battlefield_3/BF3_2024-05-17 20-14-06.mp4");
    waitFor(saved);
    assertTrue(saved.exists());
  }

  @Test
  public void clipsGoIntoTheSelectedGamesFolderToo() throws Exception
  {
    setUp(true, "Battlefield 3");

    onFxThread(() -> VideoCaptureProcessorUtils.saveNewestFile(SaveActionType.SAVE_CLIP, null));

    File saved = new File(temp.getRoot(), "clips-dest/Battlefield_3/BF3_2024-05-17 20-14-06.mp4");
    waitFor(saved);
    assertTrue(saved.exists());
  }

  @Test
  public void withoutTheSettingTheFileGoesStraightIntoKeepers() throws Exception
  {
    setUp(false, "Battlefield 3");

    onFxThread(() -> VideoCaptureProcessorUtils.saveNewestFile(SaveActionType.SAVE_KEEPER, null));

    File saved = new File(temp.getRoot(), "keepers/BF3_2024-05-17 20-14-06.mp4");
    waitFor(saved);
    assertTrue(saved.exists());
    assertFalse(new File(temp.getRoot(), "keepers/Battlefield_3").exists());
  }

  @Test
  public void withNoGameSelectedTheFileGoesStraightIntoKeepers() throws Exception
  {
    setUp(true, null);

    onFxThread(() -> VideoCaptureProcessorUtils.saveNewestFile(SaveActionType.SAVE_KEEPER, null));

    File saved = new File(temp.getRoot(), "keepers/2024-05-17 20-14-06.mp4");
    waitFor(saved);
    assertTrue(saved.exists());
    assertEquals(1, new File(temp.getRoot(), "keepers").list().length);
  }
}
