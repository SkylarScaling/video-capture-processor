package com.wheezy.apps.vidcapproc.utils;

import java.util.ArrayList;
import java.util.List;

import com.wheezy.utils.file.FileUtility;

/**
 * Builds the name a capture gets when it is saved:
 * {@code <game label>_<filename label>_<original name>}, leaving out whichever
 * labels are blank, so with no labels the original name is kept unchanged.
 */
public final class CaptureNaming
{
  private CaptureNaming()
  {
  }

  /**
   * @param gameLabel     label of the selected game, or null/blank for none
   * @param filenameLabel free text the user typed, or null/blank for none
   * @param originalName  the capture's name without extension
   */
  public static String buildName(String gameLabel, String filenameLabel, String originalName)
  {
    List<String> parts = new ArrayList<>();
    addIfPresent(parts, gameLabel);
    addIfPresent(parts, filenameLabel);
    parts.add(originalName);
    return String.join("_", parts);
  }

  private static void addIfPresent(List<String> parts, String label)
  {
    if (label != null && !label.isBlank())
    {
      // Anything that isn't safe in a filename becomes an underscore
      parts.add(FileUtility.sanitizeStringForFilename(label.strip()));
    }
  }
}
