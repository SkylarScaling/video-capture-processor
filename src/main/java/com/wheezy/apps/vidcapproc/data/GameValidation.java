package com.wheezy.apps.vidcapproc.data;

import java.util.Optional;
import java.util.regex.Pattern;

/** Checks for what a user types when adding or editing a game. Each returns an error message, or empty if it is fine. */
public final class GameValidation
{
  /** What a filename label may contain: it becomes part of file names. */
  private static final Pattern FILENAME_LABEL = Pattern.compile("[A-Za-z0-9_.\\-]*");

  private GameValidation()
  {
  }

  /**
   * @param editing the game being edited, which may keep its own title, or null when adding
   */
  public static Optional<String> titleError(String title, Game editing, GameCatalog catalog)
  {
    if (title == null || title.isBlank())
    {
      return Optional.of("Enter a game title.");
    }
    if (catalog.isTitleTaken(title.strip(), editing))
    {
      return Optional.of("A game with this title already exists.");
    }
    return Optional.empty();
  }

  /** A blank label is allowed: saved files then simply get no game label. */
  public static Optional<String> filenameLabelError(String label)
  {
    if (label != null && !FILENAME_LABEL.matcher(label.strip()).matches())
    {
      return Optional.of("The filename label can only use letters, numbers, dashes, underscores and periods.");
    }
    return Optional.empty();
  }
}
