package com.wheezy.apps.vidcapproc.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class GameFoldersTest
{
  private static final File BASE = new File("keepers");

  @Test
  public void usesTheBaseFolderWhenGamesDoNotGetFolders()
  {
    assertEquals(BASE, GameFolders.resolve(BASE, "Battlefield 3", false));
  }

  @Test
  public void usesTheBaseFolderWhenNoGameIsSelected()
  {
    assertEquals(BASE, GameFolders.resolve(BASE, null, true));
  }

  @Test
  public void putsTheGameInAFolderNamedAfterItsTitle()
  {
    assertEquals(new File(BASE, "Battlefield_3"), GameFolders.resolve(BASE, " Battlefield 3 ", true));
  }

  @Test
  public void unsafeCharactersBecomeUnderscores()
  {
    assertEquals(new File(BASE, "Half-Life_2__Episode_One"),
        GameFolders.resolve(BASE, "Half-Life 2: Episode One", true));
    assertEquals(new File(BASE, "Fate_Grand_Order"), GameFolders.resolve(BASE, "Fate/Grand Order", true));
  }

  @Test
  public void aTitleCannotEscapeTheBaseFolder()
  {
    File folder = GameFolders.resolve(BASE, "../../x", true);
    assertEquals(BASE, folder.getParentFile());
  }

  @Test
  public void trailingDotsAndSpacesAreDroppedBecauseWindowsRefusesThem()
  {
    assertEquals(new File(BASE, "Mr_Robot"), GameFolders.resolve(BASE, "Mr Robot...", true));
  }

  @Test
  public void aTitleWithNothingUsableMeansTheBaseFolder()
  {
    assertEquals(BASE, GameFolders.resolve(BASE, "...", true));
    assertEquals(BASE, GameFolders.resolve(BASE, "   ", true));
  }
}
