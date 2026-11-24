package com.wheezy.apps.vidcapproc.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.data.Game;

import javafx.application.Platform;
import javafx.scene.image.Image;

public class GameIconsTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void startToolkit()
  {
    // Image loading needs the JavaFX toolkit
    try
    {
      Platform.startup(() -> {});
    }
    catch (IllegalStateException alreadyStarted)
    {
      // fine: another test started it
    }
  }

  private File pngOfSize(String name, int width, int height) throws IOException
  {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Color.RED);
    g.fillRect(0, 0, width, height);
    g.dispose();
    File file = temp.newFile(name);
    ImageIO.write(image, "png", file);
    return file;
  }

  private static Game gameWithIcon(String title, String iconPath)
  {
    Game game = new Game();
    game.setGameTitle(title);
    game.setIconImageFilepath(iconPath);
    return game;
  }

  @Test
  public void aMissingOrBlankIconPathShowsTheDefaultIcon()
  {
    Image defaultIcon = GameIcons.defaultIcon();
    for (String path : new String[] { null, "", "   ", "D:\\no\\such\\drive\\Game.png" })
    {
      Image shown = GameIcons.load(path);
      assertFalse(path, shown.isError());
      assertEquals(path, defaultIcon.getWidth(), shown.getWidth(), 0.01);
    }
    assertFalse(GameIcons.load((Game) null).isError());
  }

  @Test
  public void aFileThatIsNotAnImageShowsTheDefaultIcon() throws Exception
  {
    File notAnImage = temp.newFile("notes.png");
    Files.writeString(notAnImage.toPath(), "this is not a picture");

    assertFalse(GameIcons.load(notAnImage.getAbsolutePath()).isError());
  }

  @Test
  public void loadsAnIconScaledDownToFitKeepingTheAspectRatio() throws Exception
  {
    File wide = pngOfSize("wide.png", 200, 100);

    Image loaded = GameIcons.load(wide.getAbsolutePath());

    assertEquals(64.0, loaded.getWidth(), 0.5);
    assertEquals(32.0, loaded.getHeight(), 0.5);
  }

  @Test
  public void savingAnIconScalesItAndNamesItAfterTheGame() throws Exception
  {
    File source = pngOfSize("big.png", 300, 300);
    File imagesDir = new File(temp.getRoot(), "images");

    String recorded = GameIcons.saveIcon(source, "Battlefield 3", List.of(), imagesDir);

    File saved = new File(imagesDir, "Battlefield_3.png");
    assertTrue(saved.exists());
    assertEquals("outside the working directory, so the full path is recorded", saved.getAbsolutePath(), recorded);
    BufferedImage written = ImageIO.read(saved);
    assertEquals(64, written.getWidth());
    assertEquals(64, written.getHeight());
    assertTrue("the original is untouched", source.exists());
  }

  @Test
  public void anIconInsideTheWorkingDirectoryIsRecordedWithARelativePath() throws Exception
  {
    File source = pngOfSize("a.png", 100, 100);
    File imagesDir = new File("target/test-icons");

    String recorded = GameIcons.saveIcon(source, "Relative Game", List.of(), imagesDir);

    assertEquals("target/test-icons/Relative_Game.png", recorded);
    assertTrue(GameIcons.resolve(recorded).isFile());
  }

  @Test
  public void anotherGamesIconFileIsNotOverwritten() throws Exception
  {
    File source = pngOfSize("src.png", 80, 80);
    File imagesDir = new File(temp.getRoot(), "images");

    // "A B" and "A_B" would both be named A_B.png
    String first = GameIcons.saveIcon(source, "A B", List.of(), imagesDir);
    Game owner = gameWithIcon("A B", first);
    String second = GameIcons.saveIcon(source, "A_B", List.of(owner), imagesDir);

    assertEquals(new File(imagesDir, "A_B.png").getAbsolutePath(), first);
    assertEquals(new File(imagesDir, "A_B_2.png").getAbsolutePath(), second);
  }

  @Test
  public void aGameMayReplaceItsOwnIconFile() throws Exception
  {
    File source = pngOfSize("src.png", 80, 80);
    File imagesDir = new File(temp.getRoot(), "images");
    String first = GameIcons.saveIcon(source, "Mine", List.of(), imagesDir);

    // Editing "Mine": it is not among the "others", so its own file can be reused
    String again = GameIcons.saveIcon(source, "Mine", List.of(), imagesDir);

    assertEquals(first, again);
  }

  @Test
  public void aTitleWithNothingUsableStillGetsAFilename() throws Exception
  {
    File source = pngOfSize("src.png", 50, 50);
    File imagesDir = new File(temp.getRoot(), "images");

    String recorded = GameIcons.saveIcon(source, "***", List.of(), imagesDir);

    assertTrue(recorded, new File(recorded).getName().endsWith(".png"));
    assertNotEquals(".png", new File(recorded).getName());
  }

  @Test
  public void savingSomethingThatIsNotAnImageFailsCleanly() throws Exception
  {
    File notAnImage = temp.newFile("junk.png");
    Files.writeString(notAnImage.toPath(), "junk");
    File imagesDir = new File(temp.getRoot(), "images");

    assertThrows(IOException.class, () -> GameIcons.saveIcon(notAnImage, "Junk", List.of(), imagesDir));
    assertFalse("nothing was written", new File(imagesDir, "Junk.png").exists());
  }
}
