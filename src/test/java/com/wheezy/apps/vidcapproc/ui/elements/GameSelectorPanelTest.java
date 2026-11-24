package com.wheezy.apps.vidcapproc.ui.elements;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.data.GameCatalog;
import com.wheezy.apps.vidcapproc.data.GameSelection;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GameSelectorPanelTest
{
  private static final String SELECTED_MARKER = "FF0000FF"; // the red border of the selected icon

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private GameCatalog catalog;
  private GameSelection selection;
  private GameSelectorPanel panel;
  private Game alpha;
  private Game beta;
  private Game gamma;

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

  /** Alpha and Gamma are displayed; Beta is hidden. */
  private void open() throws Exception
  {
    catalog = new GameCatalog(new File(temp.getRoot(), "SavedGames.xml"));
    alpha = game("Alpha", "A", true);
    beta = game("Beta", "B", false);
    gamma = game("Gamma", "G", true);
    catalog.add(alpha);
    catalog.add(beta);
    catalog.add(gamma);
    selection = new GameSelection(catalog, title -> {});
    panel = onFxThread(() ->
    {
      GameSelectorPanel created = new GameSelectorPanel();
      created.bind(catalog, selection);
      return created;
    });
  }

  private List<String> iconTitles() throws Exception
  {
    return onFxThread(() -> panel.gameButtons().stream().map(button -> String.valueOf(button.getGameTitle())).toList());
  }

  private boolean highlighted(int index) throws Exception
  {
    return onFxThread(() -> panel.gameButtons().get(index).getStyle().contains(SELECTED_MARKER));
  }

  private Button handle()
  {
    return (Button) panel.getChildren().get(0);
  }

  private VBox content()
  {
    return (VBox) panel.getChildren().get(1);
  }

  private String nameLabel()
  {
    return ((Label) content().getChildren().get(0)).getText();
  }

  @Test
  public void startsCollapsedAsJustAHandle() throws Exception
  {
    open();

    assertFalse(panel.isExpanded());
    assertFalse(content().isVisible());
    assertFalse("a collapsed panel takes up no room", content().isManaged());
    assertEquals("▸", handle().getText());
  }

  @Test
  public void theHandleExpandsAndCollapsesThePanel() throws Exception
  {
    open();
    AtomicInteger changes = new AtomicInteger();
    panel.expandedProperty().addListener((observable, was, is) -> changes.incrementAndGet());

    onFxThread(() -> handle().fire());
    assertTrue(panel.isExpanded());
    assertTrue(content().isVisible());
    assertTrue(content().isManaged());
    assertEquals("◂", handle().getText());

    onFxThread(() -> handle().fire());
    assertFalse(panel.isExpanded());
    assertFalse(content().isManaged());
    assertEquals("▸", handle().getText());
    assertEquals("the window is told each time", 2, changes.get());
  }

  @Test
  public void showsOneIconForNoGameThenOneForEachDisplayedGame() throws Exception
  {
    open();

    // The first is the stand-in for no game, which has no title
    assertEquals(List.of("null", "Alpha", "Gamma"), iconTitles());
  }

  @Test
  public void clickingAnIconSelectsThatGameAndHighlightsIt() throws Exception
  {
    open();
    assertEquals("Game: None", onFxThread(this::nameLabel));
    assertTrue("no game is the starting selection", highlighted(0));

    onFxThread(() -> panel.gameButtons().get(2).fire()); // Gamma

    assertSame(gamma, selection.getSelectedGame());
    assertEquals("Game: Gamma", onFxThread(this::nameLabel));
    assertFalse(highlighted(0));
    assertFalse(highlighted(1));
    assertTrue(highlighted(2));
  }

  @Test
  public void theNoGameIconClearsTheSelection() throws Exception
  {
    open();
    selection.select(alpha);
    assertTrue(highlighted(1));

    onFxThread(() -> panel.gameButtons().get(0).fire());

    assertNull(selection.getSelectedGame());
    assertTrue(highlighted(0));
    assertFalse(highlighted(1));
    assertEquals("Game: None", onFxThread(this::nameLabel));
  }

  @Test
  public void showsAGameSelectedBeforeThePanelExisted() throws Exception
  {
    open();
    selection.select(gamma);

    GameSelectorPanel fresh = onFxThread(() ->
    {
      GameSelectorPanel created = new GameSelectorPanel();
      created.bind(catalog, selection);
      return created;
    });

    assertTrue(onFxThread(() -> fresh.gameButtons().get(2).getStyle().contains(SELECTED_MARKER)));
    assertEquals("Game: Gamma", ((Label) ((VBox) fresh.getChildren().get(1)).getChildren().get(0)).getText());
  }

  @Test
  public void theIconsFollowTheGameManager() throws Exception
  {
    open();

    onFxThread(() ->
    {
      try
      {
        catalog.add(game("Delta", "D", true));
        catalog.setDisplayed(beta, true); // Beta becomes displayed
        catalog.setDisplayed(alpha, false); // and Alpha is hidden
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    });

    assertEquals(List.of("null", "Beta", "Gamma", "Delta"), iconTitles());
  }

  @Test
  public void aSelectedGameThatIsHiddenStaysSelectedWithoutAnIcon() throws Exception
  {
    open();
    selection.select(alpha);

    onFxThread(() ->
    {
      try
      {
        catalog.setDisplayed(alpha, false);
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    });

    assertSame(alpha, selection.getSelectedGame());
    assertEquals(List.of("null", "Gamma"), iconTitles());
    assertEquals("Game: Alpha", onFxThread(this::nameLabel));
    assertFalse("no icon is highlighted", highlighted(0) || highlighted(1));
  }

  @Test
  public void theNameFollowsAGameThatIsRenamed() throws Exception
  {
    open();
    selection.select(alpha);

    onFxThread(() ->
    {
      try
      {
        catalog.replace(alpha, game("Alpha Prime", "AP", true));
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    });

    assertEquals("Game: Alpha Prime", onFxThread(this::nameLabel));
    assertTrue("its icon is highlighted in the same place", highlighted(1));
  }

  @Test
  public void theManageButtonRunsItsAction() throws Exception
  {
    open();
    List<String> calls = new ArrayList<>();
    panel.setOnManageGames(() -> calls.add("manage"));

    Node last = content().getChildren().get(content().getChildren().size() - 1);
    onFxThread(() -> ((Button) last).fire());

    assertEquals("Manage Games...", ((Button) last).getText());
    assertEquals(List.of("manage"), calls);
  }

  @Test
  public void gameIconsHaveAnEditItemInTheirRightClickMenu() throws Exception
  {
    open();

    List<String> menus = onFxThread(() -> panel.gameButtons().stream()
        .map(button -> button.getContextMenu() == null ? "none"
            : button.getContextMenu().getItems().get(0).getText())
        .toList());

    assertEquals("no game has nothing to edit", List.of("none", "Edit...", "Edit..."), menus);
  }

  @Test
  public void editingAnIconHandsThatGameToTheEditHandler() throws Exception
  {
    open();
    List<Game> asked = new ArrayList<>();
    onFxThread(() ->
    {
      panel.setOnEditGame(asked::add);
      return null;
    });

    onFxThread(() ->
    {
      panel.gameButtons().get(2).getContextMenu().getItems().get(0).fire(); // None, Alpha, Gamma
      return null;
    });

    assertEquals(1, asked.size());
    assertSame(gamma, asked.get(0));
    assertNull("editing does not select the game", selection.getSelectedGame());
  }

  @Test
  public void theEditHandlerSurvivesTheIconsBeingRebuilt() throws Exception
  {
    open();
    List<Game> asked = new ArrayList<>();
    onFxThread(() ->
    {
      panel.setOnEditGame(asked::add);
      return null;
    });

    catalog.add(game("Delta", "D", true)); // the icons are rebuilt
    onFxThread(() ->
    {
      panel.gameButtons().get(3).getContextMenu().getItems().get(0).fire();
      return null;
    });

    assertEquals("Delta", asked.get(0).getGameTitle());
  }
}
