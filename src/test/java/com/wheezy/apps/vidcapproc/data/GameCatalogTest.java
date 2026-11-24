package com.wheezy.apps.vidcapproc.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GameCatalogTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private File file;
  private GameCatalog catalog;

  @Before
  public void setUp() throws Exception
  {
    file = new File(temp.getRoot(), "SavedGames.xml");
    catalog = new GameCatalog(file);
  }

  private static Game game(String title, String label)
  {
    Game game = new Game();
    game.setGameTitle(title);
    game.setFilenameLabel(label);
    game.setDisplayed(true);
    return game;
  }

  private List<String> titles()
  {
    return catalog.getGames().stream().map(Game::getGameTitle).toList();
  }

  private GameCatalog reload() throws IOException
  {
    GameCatalog fresh = new GameCatalog(file);
    fresh.load();
    return fresh;
  }

  @Test
  public void loadsAFileInTheSwingAppsFormat() throws Exception
  {
    try (InputStream sample = getClass().getResourceAsStream("/swing-saved-games.xml"))
    {
      Files.copy(sample, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    catalog.load();

    // The case-insensitive repeat of "Black Ops 2" and the game with no title are skipped
    assertEquals(List.of("Battlefield 3", "Black Ops 2", "Caf\u00e9 & Bar"), titles());
    Game bf3 = catalog.getGames().get(0);
    assertEquals("BF3", bf3.getFilenameLabel());
    assertEquals("D:\\Somewhere\\images\\BF3.png", bf3.getIconImageFilepath());
    assertTrue(bf3.isDisplayed());
    assertFalse("hidden flag is read", catalog.getGames().get(1).isDisplayed());
    assertEquals("images/CAFE.png", catalog.getGames().get(2).getIconImageFilepath());
  }

  @Test
  public void aMissingFileMeansNoGames() throws Exception
  {
    catalog.load();
    assertTrue(catalog.getGames().isEmpty());
  }

  @Test
  public void changesAreSavedAndComeBackInTheSameOrder() throws Exception
  {
    catalog.add(game("Alpha", "A"));
    catalog.add(game("Beta", "B"));
    Game gamma = game("Gamma", "G");
    catalog.add(gamma);
    catalog.moveUp(gamma);
    catalog.setDisplayed(catalog.getGames().get(0), false);

    GameCatalog reloaded = reload();

    assertEquals(List.of("Alpha", "Gamma", "Beta"), reloaded.getGames().stream().map(Game::getGameTitle).toList());
    assertFalse(reloaded.getGames().get(0).isDisplayed());
    assertTrue(reloaded.getGames().get(1).isDisplayed());
  }

  @Test
  public void writesTheSameXmlStructureAsTheSwingApp() throws Exception
  {
    catalog.add(game("Alpha", "A"));

    String xml = Files.readString(file.toPath(), StandardCharsets.UTF_8);

    assertTrue(xml, xml.contains("<gameCollection>"));
    assertTrue(xml, xml.contains("<gameSet>"));
    assertTrue(xml, xml.contains("<gameTitle>Alpha</gameTitle>"));
    assertTrue(xml, xml.contains("<filenameLabel>A</filenameLabel>"));
    assertTrue(xml, xml.contains("<displayed>true</displayed>"));
  }

  @Test
  public void titlesAreUniqueIgnoringCase() throws Exception
  {
    catalog.add(game("Alpha", "A"));

    assertThrows(IllegalArgumentException.class, () -> catalog.add(game("ALPHA", "X")));
    assertThrows(IllegalArgumentException.class, () -> catalog.add(game("  ", "X")));
    assertEquals(List.of("Alpha"), titles());
    assertTrue(catalog.find("aLpHa").isPresent());
    assertFalse(catalog.find("Beta").isPresent());
  }

  @Test
  public void replacingKeepsPositionAndAllowsKeepingOrChangingTheTitle() throws Exception
  {
    Game alpha = game("Alpha", "A");
    Game beta = game("Beta", "B");
    catalog.add(alpha);
    catalog.add(beta);

    Game alphaRenamed = game("Alpha Prime", "AP");
    catalog.replace(alpha, alphaRenamed);
    Game betaEdited = game("beta", "B2"); // only the case of its own title changed
    catalog.replace(beta, betaEdited);

    assertEquals(List.of("Alpha Prime", "beta"), titles());
    assertSame(alphaRenamed, catalog.getGames().get(0));
    assertThrows("cannot take another game's title", IllegalArgumentException.class,
        () -> catalog.replace(alphaRenamed, game("BETA", "X")));
    assertEquals("Alpha Prime", catalog.getGames().get(0).getGameTitle());
  }

  @Test
  public void removesAGame() throws Exception
  {
    Game alpha = game("Alpha", "A");
    catalog.add(alpha);
    catalog.add(game("Beta", "B"));

    catalog.remove(alpha);

    assertEquals(List.of("Beta"), titles());
    assertEquals(List.of("Beta"), reload().getGames().stream().map(Game::getGameTitle).toList());
  }

  @Test
  public void movingPastTheEndsDoesNothing() throws Exception
  {
    Game alpha = game("Alpha", "A");
    Game beta = game("Beta", "B");
    catalog.add(alpha);
    catalog.add(beta);

    catalog.moveUp(alpha);
    catalog.moveDown(beta);
    assertEquals(List.of("Alpha", "Beta"), titles());

    catalog.moveDown(alpha);
    assertEquals(List.of("Beta", "Alpha"), titles());
  }

  @Test
  public void listenersHearAboutEachKindOfChange() throws Exception
  {
    AtomicInteger changes = new AtomicInteger();
    catalog.getGames().addListener((javafx.collections.ListChangeListener<Game>) change -> changes.incrementAndGet());

    Game alpha = game("Alpha", "A");
    catalog.add(alpha); // 1
    catalog.add(game("Beta", "B")); // 2
    catalog.setDisplayed(alpha, false); // 3: the same instance changed
    catalog.moveDown(alpha); // 4
    catalog.remove(alpha); // 5

    assertEquals(5, changes.get());
  }

  @Test
  public void theListCannotBeChangedBehindTheCatalogsBack() throws Exception
  {
    catalog.add(game("Alpha", "A"));
    assertThrows(UnsupportedOperationException.class, () -> catalog.getGames().add(game("Sneaky", "S")));
  }

  @Test
  public void aFailedSaveUndoesTheChange() throws Exception
  {
    // The "directory" is really a file, so nothing can be written under it
    File notADirectory = temp.newFile("blocker");
    GameCatalog broken = new GameCatalog(new File(notADirectory, "SavedGames.xml"));

    assertThrows(IOException.class, () -> broken.add(game("Alpha", "A")));
    assertTrue("the add was undone", broken.getGames().isEmpty());
  }

  @Test
  public void aFailedSaveUndoesToggleMoveAndRemove() throws Exception
  {
    Game alpha = game("Alpha", "A");
    Game beta = game("Beta", "B");
    catalog.add(alpha);
    catalog.add(beta);

    // Make the next save fail: turn the target file's location into something unwritable
    assertTrue(file.delete());
    assertTrue(file.mkdir());
    Files.writeString(new File(file, "inside").toPath(), "x");
    assertThrows(IOException.class, () -> catalog.setDisplayed(alpha, false));
    assertTrue("toggle undone", alpha.isDisplayed());
    assertThrows(IOException.class, () -> catalog.moveDown(alpha));
    assertEquals("move undone", List.of("Alpha", "Beta"), titles());
    assertThrows(IOException.class, () -> catalog.remove(alpha));
    assertEquals("remove undone", List.of("Alpha", "Beta"), titles());
  }

  @Test
  public void keepsThePreviousVersionAsABackup() throws Exception
  {
    catalog.add(game("Alpha", "A"));
    catalog.add(game("Beta", "B"));

    File backup = new File(file.getPath() + ".bak");
    String previous = Files.readString(backup.toPath());
    assertTrue("backup holds the file as it was before the last save", previous.contains("Alpha"));
    assertFalse(previous.contains("Beta"));
  }

  @Test
  public void anUnreadableFileIsKeptAsACopyAndReported() throws Exception
  {
    Files.writeString(file.toPath(), "this is not xml");

    IOException failure = assertThrows(IOException.class, () -> catalog.load());

    assertTrue(failure.getMessage(), failure.getMessage().contains("could not be read"));
    assertEquals("this is not xml", Files.readString(new File(file.getPath() + ".unreadable").toPath()));
  }
}
