package com.wheezy.apps.vidcapproc.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GameValidationTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private Game game(String title)
  {
    Game game = new Game();
    game.setGameTitle(title);
    return game;
  }

  @Test
  public void aTitleIsRequired() throws Exception
  {
    GameCatalog catalog = new GameCatalog(new File(temp.getRoot(), "g.xml"));

    assertEquals("Enter a game title.", GameValidation.titleError("", null, catalog).orElse(null));
    assertEquals("Enter a game title.", GameValidation.titleError("   ", null, catalog).orElse(null));
    assertEquals("Enter a game title.", GameValidation.titleError(null, null, catalog).orElse(null));
    assertTrue(GameValidation.titleError("Alpha", null, catalog).isEmpty());
  }

  @Test
  public void aTitleMustBeUniqueIgnoringCaseAndSpacesAtTheEnds() throws Exception
  {
    GameCatalog catalog = new GameCatalog(new File(temp.getRoot(), "g.xml"));
    Game alpha = game("Alpha");
    catalog.add(alpha);

    assertEquals("A game with this title already exists.",
        GameValidation.titleError(" alpha ", null, catalog).orElse(null));
    assertTrue("a game may keep its own title when edited", GameValidation.titleError("ALPHA", alpha, catalog).isEmpty());
    assertTrue(GameValidation.titleError("Beta", alpha, catalog).isEmpty());
  }

  @Test
  public void aFilenameLabelMayBeBlankOrFilenameSafe()
  {
    assertTrue(GameValidation.filenameLabelError(null).isEmpty());
    assertTrue(GameValidation.filenameLabelError("").isEmpty());
    assertTrue(GameValidation.filenameLabelError("  ").isEmpty());
    assertTrue(GameValidation.filenameLabelError("BF3").isEmpty());
    assertTrue(GameValidation.filenameLabelError("Black-Ops_2.v1").isEmpty());
  }

  @Test
  public void aFilenameLabelCannotHoldCharactersThatAreUnsafeInFilenames()
  {
    for (String bad : new String[] { "BF 3", "a/b", "a\\b", "what?", "a:b", "café", "star*" })
    {
      assertTrue(bad, GameValidation.filenameLabelError(bad).isPresent());
    }
  }
}
