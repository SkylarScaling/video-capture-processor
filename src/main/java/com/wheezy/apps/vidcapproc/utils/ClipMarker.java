package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.wheezy.utils.file.FileUtility;

/**
 * Records "clip marks" for the capture that is currently being recorded.
 * <p>
 * A mark is the time elapsed since the capture started, taken from the capture's
 * filename, written as one line (e.g. {@code 12:34 (nice shot)}) to a
 * {@code <capture name>.clips} file. Those files are kept in {@link #CLIPS_DIRECTORY}
 * and are moved along with the capture when it is saved.
 */
public final class ClipMarker
{
  /** Directory (relative to the working directory) that holds the .clips mark files. */
  public static final String CLIPS_DIRECTORY = "clips";
  public static final String CLIPS_FILE_EXTENSION = ".clips";

  private ClipMarker()
  {
  }

  /**
   * @param captureName capture filename without extension
   * @throws DateTimeParseException if the name is not in a known format (see {@link CaptureNameFormat})
   */
  public static LocalDateTime parseCaptureStart(String captureName)
  {
    return parseCaptureStart(captureName, CaptureNameFormat.AUTOMATIC);
  }

  public static LocalDateTime parseCaptureStart(String captureName, CaptureNameFormat format)
  {
    return format.parse(captureName);
  }

  /** Formats an elapsed time as minutes:seconds, e.g. 75 minutes 3 seconds is "75:03". */
  public static String formatOffset(Duration elapsed)
  {
    long totalSeconds = elapsed.getSeconds();
    return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
  }

  /** The full line for a mark: the offset, plus the label in parentheses if there is one. */
  public static String formatEntry(Duration elapsed, String label)
  {
    String offset = formatOffset(elapsed);
    return label == null || label.isBlank() ? offset : offset + " (" + label.strip() + ")";
  }

  /**
   * Appends a mark for {@code captureName} to its .clips file.
   *
   * @return the offset that was marked, e.g. "1:35"
   * @throws DateTimeParseException   if the capture name has no parseable start time
   * @throws IllegalArgumentException if the capture appears to start after {@code now}
   * @throws IOException              if the .clips file cannot be written
   */
  public static String mark(String captureName, File clipsDir, String label, LocalDateTime now) throws IOException
  {
    return mark(captureName, clipsDir, label, now, CaptureNameFormat.AUTOMATIC);
  }

  public static String mark(String captureName, File clipsDir, String label, LocalDateTime now,
      CaptureNameFormat format) throws IOException
  {
    Duration elapsed = Duration.between(parseCaptureStart(captureName, format), now);
    if (elapsed.isNegative())
    {
      throw new IllegalArgumentException("Capture start time is in the future: " + captureName);
    }

    if (!clipsDir.isDirectory() && !clipsDir.mkdirs())
    {
      throw new IOException("Unable to create clips directory: " + clipsDir.getAbsolutePath());
    }

    FileUtility.writeStringToFile(formatEntry(elapsed, label), new File(clipsDir, captureName + CLIPS_FILE_EXTENSION),
        true);
    return formatOffset(elapsed);
  }
}
