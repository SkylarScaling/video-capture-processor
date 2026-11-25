package com.wheezy.apps.vidcapproc.utils;

import java.io.File;

import com.wheezy.utils.file.FileUtility;

/** Where a game's keepers and clips go when each game has its own folder. */
public final class GameFolders
{
  private GameFolders()
  {
  }

  /**
   * @param base      the Keepers or Clips folder
   * @param gameTitle title of the selected game, or null for none
   * @param perGame   whether games get their own folders
   * @return {@code base/<game title>} if enabled and there is a game, otherwise {@code base}
   */
  public static File resolve(File base, String gameTitle, boolean perGame)
  {
    if (!perGame || gameTitle == null)
    {
      return base;
    }
    // Windows does not allow a folder name to end in a dot or a space
    String name = FileUtility.sanitizeStringForFilename(gameTitle.strip()).replaceAll("[. ]+$", "");
    return name.isEmpty() ? base : new File(base, name);
  }
}
