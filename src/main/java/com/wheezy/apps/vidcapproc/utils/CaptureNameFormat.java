package com.wheezy.apps.vidcapproc.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The ways capture software names its recordings after the time they started. Mark Clip
 * reads the start time from the name, so it has to know which one to expect.
 */
public enum CaptureNameFormat
{
  /** Tries every other format and uses the first that fits. */
  AUTOMATIC("Detect automatically", null, "any of the formats below"),
  OBS("OBS (2024-05-17 20-14-06)", "yyyy-MM-dd HH-mm-ss", "yyyy-MM-dd HH-mm-ss"),
  UNDERSCORES("Underscores (2024_5_17_20_14_6)", "yyyy_M_d_H_m_s", "yyyy_M_d_H_m_s"),
  LIVE_GAMER_PORTABLE("Live Gamer Portable (20240517201406)", "yyyyMMddHHmmss", "yyyyMMddHHmmss");

  private final String displayName;
  private final DateTimeFormatter formatter;
  private final String expected;

  CaptureNameFormat(String displayName, String pattern, String expected)
  {
    this.displayName = displayName;
    this.formatter = pattern == null ? null : DateTimeFormatter.ofPattern(pattern);
    this.expected = expected;
  }

  /** What to tell the user a name should look like. */
  public String expected()
  {
    return expected;
  }

  /**
   * @param captureName a capture's name without its extension
   * @throws DateTimeParseException if the name does not fit this format
   */
  public LocalDateTime parse(String captureName)
  {
    if (formatter != null)
    {
      return LocalDateTime.parse(captureName, formatter);
    }
    DateTimeParseException last = null;
    for (CaptureNameFormat format : List.of(OBS, UNDERSCORES, LIVE_GAMER_PORTABLE))
    {
      try
      {
        return format.parse(captureName);
      }
      catch (DateTimeParseException e)
      {
        last = e;
      }
    }
    throw last;
  }

  /** @return the format with this enum name as stored in the settings, or {@link #AUTOMATIC} for anything else */
  public static CaptureNameFormat fromSetting(String setting)
  {
    if (setting != null)
    {
      for (CaptureNameFormat format : values())
      {
        if (format.name().equals(setting.strip()))
        {
          return format;
        }
      }
    }
    return AUTOMATIC;
  }

  @Override
  public String toString()
  {
    return displayName;
  }
}
