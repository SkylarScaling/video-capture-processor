package com.wheezy.apps.vidcapproc.controller;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.data.GameCatalog;
import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.elements.GamePanel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class GameManagerControllerTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  private static Game game(String title, String label, boolean displayed)
  {
    Game game = new Game();
    game.setGameTitle(title);
    game.setFilenameLabel(label);
    game.setDisplayed(displayed);
    return game;
  }

  private GameCatalog catalogWith(Game... games) throws Exception
  {
    GameCatalog catalog = new GameCatalog(new File(temp.getRoot(), "SavedGames.xml"));
    for (Game game : games)
    {
      catalog.add(game);
    }
    return catalog;
  }

  /** Loads the manager window's contents, showing the given catalog. */
  private Parent load(GameCatalog catalog) throws Exception
  {
    GameCatalog.setInstance(catalog); // so the controller never reads the real games file
    return onFxThread(() ->
    {
      try
      {
        return new FXMLLoader(getClass().getClassLoader().getResource(VideoCaptureProcessorResources.GAME_MANAGER_FXML))
            .load();
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    });
  }

  /** The rows live inside the scroll pane, which lookup() cannot see into until the pane has a skin. */
  private static VBox listBox(Parent root)
  {
    return (VBox) ((ScrollPane) root.lookup("#gameScrollPane")).getContent();
  }

  private static List<GamePanel> rows(Parent root)
  {
    List<GamePanel> rows = new ArrayList<>();
    for (Node child : listBox(root).getChildren())
    {
      rows.add((GamePanel) child);
    }
    return rows;
  }

  private static List<String> rowTitles(Parent root)
  {
    return rows(root).stream().map(row -> row.getGame().getGameTitle()).toList();
  }

  private static String summary(Parent root)
  {
    return ((Label) root.lookup("#summaryLabel")).getText();
  }

  @Test
  public void listsEveryGameInOrderWithASummary() throws Exception
  {
    Parent root = load(catalogWith(game("Alpha", "A", true), game("Beta", "B", false), game("Gamma", "G", true)));

    assertEquals(List.of("Alpha", "Beta", "Gamma"), onFxThread(() -> rowTitles(root)));
    assertEquals("3 games, 2 displayed", onFxThread(() -> summary(root)));
  }

  @Test
  public void anEmptyListSaysSo() throws Exception
  {
    Parent root = load(catalogWith());

    assertTrue(onFxThread(() -> rows(root)).isEmpty());
    assertEquals("0 games, 0 displayed", onFxThread(() -> summary(root)));
  }

  @Test
  public void theListFollowsChangesToTheCatalog() throws Exception
  {
    GameCatalog catalog = catalogWith(game("Alpha", "A", true));
    Parent root = load(catalog);

    Game beta = game("Beta", "B", true);
    onFxThread(() ->
    {
      try
      {
        catalog.add(beta);
        catalog.moveUp(beta);
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    });

    assertEquals(List.of("Beta", "Alpha"), onFxThread(() -> rowTitles(root)));
    assertEquals("2 games, 2 displayed", onFxThread(() -> summary(root)));
  }

  @Test
  public void theRowButtonsChangeAndSaveTheCatalog() throws Exception
  {
    Game alpha = game("Alpha", "A", true);
    Game beta = game("Beta", "B", true);
    GameCatalog catalog = catalogWith(alpha, beta);
    Parent root = load(catalog);

    // Move Alpha down with its arrow button
    onFxThread(() ->
    {
      for (Node node : rows(root).get(0).lookupAll(".button"))
      {
        if ("▼".equals(((Button) node).getText()))
        {
          ((Button) node).fire();
        }
      }
    });
    assertEquals(List.of("Beta", "Alpha"), onFxThread(() -> rowTitles(root)));

    // Toggle Alpha (now second) by clicking its row
    onFxThread(() -> javafx.event.Event.fireEvent(rows(root).get(1), new javafx.scene.input.MouseEvent(
        javafx.scene.input.MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, javafx.scene.input.MouseButton.PRIMARY, 1, false,
        false, false, false, true, false, false, true, false, false, null)));
    assertFalse(alpha.isDisplayed());
    assertEquals("2 games, 1 displayed", onFxThread(() -> summary(root)));

    // Everything was saved, in order
    GameCatalog reloaded = new GameCatalog(new File(temp.getRoot(), "SavedGames.xml"));
    reloaded.load();
    assertEquals(List.of("Beta", "Alpha"), reloaded.getGames().stream().map(Game::getGameTitle).toList());
    assertFalse(reloaded.getGames().get(1).isDisplayed());
  }
}
