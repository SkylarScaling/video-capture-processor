package com.wheezy.apps.vidcapproc.ui.elements;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.data.Game;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class GamePanelTest
{
  /** Records what the row asked for. */
  private static class RecordingActions implements GamePanel.Actions
  {
    final List<String> calls = new ArrayList<>();

    @Override
    public void toggleDisplayed(Game game)
    {
      calls.add("toggle " + game.getGameTitle());
    }

    @Override
    public void edit(Game game)
    {
      calls.add("edit " + game.getGameTitle());
    }

    @Override
    public void delete(Game game)
    {
      calls.add("delete " + game.getGameTitle());
    }

    @Override
    public void moveUp(Game game)
    {
      calls.add("up " + game.getGameTitle());
    }

    @Override
    public void moveDown(Game game)
    {
      calls.add("down " + game.getGameTitle());
    }
  }

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

  private static Button button(GamePanel panel, String text)
  {
    for (Node node : panel.lookupAll(".button"))
    {
      if (node instanceof Button && text.equals(((Button) node).getText()))
      {
        return (Button) node;
      }
    }
    throw new AssertionError("no button '" + text + "'");
  }

  private static boolean hasLabel(GamePanel panel, String text)
  {
    for (Node node : panel.lookupAll(".label"))
    {
      if (node instanceof Label && text.equals(((Label) node).getText()))
      {
        return true;
      }
    }
    return false;
  }

  private static MouseEvent click(MouseButton which)
  {
    return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, which, 1, false, false, false, false,
        which == MouseButton.PRIMARY, false, which == MouseButton.SECONDARY, true, false, false, null);
  }

  @Test
  public void showsTheTitleFilenameFormatAndDisplayedState() throws Exception
  {
    GamePanel shown = onFxThread(() -> new GamePanel(game("Battlefield 3", "BF3", true), false, false,
        new RecordingActions()));
    GamePanel hidden = onFxThread(() -> new GamePanel(game("Black Ops 2", "", false), false, false,
        new RecordingActions()));

    assertTrue(hasLabel(shown, "Battlefield 3"));
    assertTrue(hasLabel(shown, "BF3_<filename>"));
    assertTrue(hasLabel(shown, "Displayed"));
    assertTrue(hasLabel(hidden, "<filename>")); // no label: files keep their own name
    assertTrue(hasLabel(hidden, "Not displayed"));
  }

  @Test
  public void aHiddenGameIsDimmed() throws Exception
  {
    GamePanel shown = onFxThread(() -> new GamePanel(game("A", "A", true), false, false, new RecordingActions()));
    GamePanel hidden = onFxThread(() -> new GamePanel(game("B", "B", false), false, false, new RecordingActions()));

    assertEquals(1.0, shown.getChildren().get(0).getOpacity(), 0.001);
    assertTrue(hidden.getChildren().get(0).getOpacity() < 1.0);
  }

  @Test
  public void theFirstGameCannotMoveUpAndTheLastCannotMoveDown() throws Exception
  {
    GamePanel first = onFxThread(() -> new GamePanel(game("A", "A", true), true, false, new RecordingActions()));
    GamePanel last = onFxThread(() -> new GamePanel(game("B", "B", true), false, true, new RecordingActions()));
    GamePanel only = onFxThread(() -> new GamePanel(game("C", "C", true), true, true, new RecordingActions()));

    assertTrue(button(first, "▲").isDisabled());
    assertFalse(button(first, "▼").isDisabled());
    assertFalse(button(last, "▲").isDisabled());
    assertTrue(button(last, "▼").isDisabled());
    assertTrue(button(only, "▲").isDisabled());
    assertTrue(button(only, "▼").isDisabled());
  }

  @Test
  public void theButtonsAskForTheirActions() throws Exception
  {
    RecordingActions actions = new RecordingActions();
    GamePanel panel = onFxThread(() -> new GamePanel(game("Alpha", "A", true), false, false, actions));

    onFxThread(() ->
    {
      button(panel, "Edit...").fire();
      button(panel, "Delete").fire();
      button(panel, "▲").fire();
      button(panel, "▼").fire();
    });

    assertEquals(List.of("edit Alpha", "delete Alpha", "up Alpha", "down Alpha"), actions.calls);
  }

  @Test
  public void clickingTheRowTogglesButClickingAButtonDoesNot() throws Exception
  {
    RecordingActions actions = new RecordingActions();
    GamePanel panel = onFxThread(() -> new GamePanel(game("Alpha", "A", true), false, false, actions));

    onFxThread(() ->
    {
      Event.fireEvent(panel, click(MouseButton.PRIMARY));
      // A click on a button bubbles up to the row, but it must not count as a click on the row
      Event.fireEvent(button(panel, "Edit..."), click(MouseButton.PRIMARY));
      Event.fireEvent(panel, click(MouseButton.SECONDARY));
    });

    assertEquals(List.of("toggle Alpha"), actions.calls);
  }

  @Test
  public void aRowKnowsItsGame() throws Exception
  {
    Game game = game("Alpha", "A", true);
    GamePanel panel = onFxThread(() -> new GamePanel(game, false, false, new RecordingActions()));

    assertNotNull(panel.getGame());
    assertEquals(game, panel.getGame());
  }
}
