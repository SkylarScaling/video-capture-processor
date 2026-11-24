package com.wheezy.apps.vidcapproc.controller;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.data.GameCatalog;
import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class GameEditorControllerTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private GameCatalog catalog;
  private File imagesDir;
  private Parent root;
  private GameEditorController editor;

  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  private void open(Game toEdit, Game... existing) throws Exception
  {
    catalog = new GameCatalog(new File(temp.getRoot(), "SavedGames.xml"));
    for (Game game : existing)
    {
      catalog.add(game);
    }
    imagesDir = new File(temp.getRoot(), "images");
    GameCatalog.setInstance(catalog); // so the controller never reads the real games file

    onFxThread(() ->
    {
      try
      {
        FXMLLoader loader = new FXMLLoader(
            getClass().getClassLoader().getResource(VideoCaptureProcessorResources.GAME_EDITOR_FXML));
        root = loader.load();
        editor = loader.getController();
        editor.useImagesDirectory(imagesDir);
        editor.setGame(toEdit);
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    });
  }

  private static Game game(String title, String label, boolean displayed, String icon)
  {
    Game game = new Game();
    game.setGameTitle(title);
    game.setFilenameLabel(label);
    game.setDisplayed(displayed);
    game.setIconImageFilepath(icon);
    return game;
  }

  private void type(String title, String label) throws Exception
  {
    onFxThread(() ->
    {
      ((TextField) root.lookup("#gameTitleField")).setText(title);
      ((TextField) root.lookup("#filenameLabelField")).setText(label);
    });
  }

  private boolean saveDisabled() throws Exception
  {
    return onFxThread(() -> ((Button) root.lookup("#saveButton")).isDisabled());
  }

  private String error() throws Exception
  {
    return onFxThread(() -> ((Label) root.lookup("#errorLabel")).getText());
  }

  private void pressSave() throws Exception
  {
    onFxThread(() -> editor.save(null));
  }

  private File pngFile(String name) throws IOException
  {
    BufferedImage image = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Color.BLUE);
    g.fillRect(0, 0, 120, 120);
    g.dispose();
    File file = temp.newFile(name);
    ImageIO.write(image, "png", file);
    return file;
  }

  @Test
  public void saveStaysDisabledUntilThereIsAValidTitle() throws Exception
  {
    open(null);
    assertTrue("nothing typed yet", saveDisabled());
    assertEquals("an empty form is not scolded", "", error());

    type("Battlefield 3", "BF3");
    assertFalse(saveDisabled());
    assertEquals("", error());

    type("   ", "BF3");
    assertTrue(saveDisabled());
  }

  @Test
  public void explainsAProblemWithTheTitleOrLabel() throws Exception
  {
    open(null, game("Alpha", "A", true, null));

    type("alpha", "A");
    assertTrue(saveDisabled());
    assertEquals("A game with this title already exists.", error());

    type("Beta", "has spaces");
    assertTrue(saveDisabled());
    assertTrue(error(), error().startsWith("The filename label can only use"));
  }

  @Test
  public void showsHowFilesWillBeNamed() throws Exception
  {
    open(null);
    type("Battlefield 3", "BF3");
    assertEquals("Saved files will be named BF3_<filename>",
        onFxThread(() -> ((Label) root.lookup("#filenameExampleLabel")).getText()));

    type("Battlefield 3", "");
    assertEquals("Saved files will be named <filename>",
        onFxThread(() -> ((Label) root.lookup("#filenameExampleLabel")).getText()));
  }

  @Test
  public void addsANewGameAtTheEndAndSavesIt() throws Exception
  {
    open(null, game("Alpha", "A", true, null));
    type("  Black Ops 2  ", " BO2 ");

    pressSave();

    assertEquals(List.of("Alpha", "Black Ops 2"), catalog.getGames().stream().map(Game::getGameTitle).toList());
    Game added = catalog.getGames().get(1);
    assertEquals("BO2", added.getFilenameLabel());
    assertTrue("new games start displayed", added.isDisplayed());
    assertNull("no icon chosen, so the default is used", added.getIconImageFilepath());

    GameCatalog reloaded = new GameCatalog(new File(temp.getRoot(), "SavedGames.xml"));
    reloaded.load();
    assertEquals(2, reloaded.getGames().size());
  }

  @Test
  public void aChosenImageIsStoredAsTheGamesIcon() throws Exception
  {
    open(null);
    type("Battlefield 3", "BF3");
    File picked = pngFile("picked.png");
    assertTrue(onFxThread(() -> editor.useChosenImage(picked)));

    pressSave();

    Game added = catalog.getGames().get(0);
    assertEquals(new File(imagesDir, "Battlefield_3.png").getAbsolutePath(), added.getIconImageFilepath());
    BufferedImage stored = ImageIO.read(new File(added.getIconImageFilepath()));
    assertEquals(64, stored.getWidth());
  }

  @Test
  public void editingKeepsTheDisplayedFlagPositionAndIcon() throws Exception
  {
    Game alpha = game("Alpha", "A", true, null);
    Game beta = game("Beta", "B", false, "images/Beta.png");
    open(beta, alpha, beta);

    assertEquals("title is filled in", "Beta",
        onFxThread(() -> ((TextField) root.lookup("#gameTitleField")).getText()));
    type("Beta Renamed", "BR");
    pressSave();

    assertEquals(List.of("Alpha", "Beta Renamed"), catalog.getGames().stream().map(Game::getGameTitle).toList());
    Game edited = catalog.getGames().get(1);
    assertEquals("BR", edited.getFilenameLabel());
    assertFalse("hidden stays hidden", edited.isDisplayed());
    assertEquals("the icon was not touched", "images/Beta.png", edited.getIconImageFilepath());
  }

  @Test
  public void aGameMayKeepItsOwnTitleWhenEdited() throws Exception
  {
    Game alpha = game("Alpha", "A", true, null);
    open(alpha, alpha);

    type("ALPHA", "A2"); // same title apart from case: still this game's own title
    assertFalse(saveDisabled());
    pressSave();

    assertEquals(1, catalog.getGames().size());
    assertEquals("ALPHA", catalog.getGames().get(0).getGameTitle());
  }

  @Test
  public void aChangedImageReplacesTheIconWhenEditing() throws Exception
  {
    Game alpha = game("Alpha", "A", true, "images/old.png");
    open(alpha, alpha);
    File replacement = pngFile("new.png");
    assertTrue(onFxThread(() -> editor.useChosenImage(replacement)));

    pressSave();

    assertEquals(new File(imagesDir, "Alpha.png").getAbsolutePath(),
        catalog.getGames().get(0).getIconImageFilepath());
  }
}
