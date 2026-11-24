package com.wheezy.apps.vidcapproc.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GameSelectionTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private GameCatalog catalog;
  private List<String> remembered;
  private GameSelection selection;
  private Game alpha;
  private Game beta;

  @Before
  public void setUp() throws Exception
  {
    catalog = new GameCatalog(new File(temp.getRoot(), "SavedGames.xml"));
    alpha = game("Alpha", "A");
    beta = game("Beta", "B");
    catalog.add(alpha);
    catalog.add(beta);
    remembered = new ArrayList<>();
    selection = new GameSelection(catalog, remembered::add);
  }

  private static Game game(String title, String label)
  {
    Game game = new Game();
    game.setGameTitle(title);
    game.setFilenameLabel(label);
    game.setDisplayed(true);
    return game;
  }

  @Test
  public void startsWithNoGame()
  {
    assertNull(selection.getSelectedGame());
    assertTrue(selection.gameLabel().isEmpty());
  }

  @Test
  public void selectingAGameRemembersItsTitle()
  {
    selection.select(beta);

    assertSame(beta, selection.getSelectedGame());
    assertEquals("B", selection.gameLabel().orElse(null));
    assertEquals(List.of("Beta"), remembered);
  }

  @Test
  public void selectingNothingIsRememberedAsNone()
  {
    selection.select(alpha);
    selection.select(null);

    assertNull(selection.getSelectedGame());
    assertEquals(List.of("Alpha", GameSelection.NONE), remembered);
  }

  @Test
  public void aGameWithoutALabelGivesNoLabel()
  {
    Game unlabeled = game("Gamma", "  ");
    assertTrue(selection.gameLabel().isEmpty());
    try
    {
      catalog.add(unlabeled);
    }
    catch (Exception e)
    {
      throw new AssertionError(e);
    }

    selection.select(unlabeled);

    assertTrue(selection.gameLabel().isEmpty());
  }

  @Test
  public void cannotSelectAGameThatIsNotInTheList()
  {
    assertThrows(IllegalArgumentException.class, () -> selection.select(game("Stranger", "S")));
    assertNull(selection.getSelectedGame());
  }

  @Test
  public void restoringByTitleSelectsWithoutRememberingAgain()
  {
    selection.restore("beta"); // as stored by a previous run; case does not matter

    assertSame(beta, selection.getSelectedGame());
    assertTrue("nothing new to remember", remembered.isEmpty());
  }

  @Test
  public void restoringAnUnknownOrNoneTitleMeansNoGame()
  {
    selection.select(alpha);

    selection.restore("Deleted Game");
    assertNull(selection.getSelectedGame());

    selection.select(alpha);
    selection.restore(GameSelection.NONE);
    assertNull(selection.getSelectedGame());

    selection.select(alpha);
    selection.restore(null);
    assertNull(selection.getSelectedGame());
  }

  @Test
  public void followsTheGameWhenItIsEditedEvenIfRenamed() throws Exception
  {
    selection.select(alpha);
    remembered.clear();

    Game renamed = game("Alpha Prime", "AP");
    catalog.replace(alpha, renamed);

    assertSame(renamed, selection.getSelectedGame());
    assertEquals(List.of("Alpha Prime"), remembered);
    assertEquals("AP", selection.gameLabel().orElse(null));
  }

  @Test
  public void clearsWhenTheSelectedGameIsDeleted() throws Exception
  {
    selection.select(alpha);

    catalog.remove(alpha);

    assertNull(selection.getSelectedGame());
    assertEquals(List.of("Alpha", GameSelection.NONE), remembered);
  }

  @Test
  public void keepsTheSelectionWhenOtherGamesChangeOrTheListIsReordered() throws Exception
  {
    selection.select(alpha);
    remembered.clear();

    catalog.moveDown(alpha); // reorders the whole list at once
    catalog.setDisplayed(beta, false);
    catalog.add(game("Gamma", "G"));
    catalog.remove(beta);

    assertSame(alpha, selection.getSelectedGame());
    assertTrue(remembered.isEmpty());
  }

  @Test
  public void keepsTheSelectionWhenTheSelectedGameIsHidden() throws Exception
  {
    selection.select(alpha);

    catalog.setDisplayed(alpha, false);

    assertSame(alpha, selection.getSelectedGame());
    assertFalse(alpha.isDisplayed());
  }

  @Test
  public void listenersHearAboutChanges()
  {
    AtomicInteger changes = new AtomicInteger();
    selection.selectedGameProperty().addListener((observable, oldGame, newGame) -> changes.incrementAndGet());

    selection.select(alpha);
    selection.select(beta);
    selection.select(null);

    assertEquals(3, changes.get());
  }
}
