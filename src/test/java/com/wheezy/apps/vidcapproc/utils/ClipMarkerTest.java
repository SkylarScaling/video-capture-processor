package com.wheezy.apps.vidcapproc.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ClipMarkerTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final LocalDateTime START = LocalDateTime.of(2024, 5, 17, 20, 14, 6);

  @Test
  public void parsesObsStyleCaptureNames()
  {
    assertEquals(START, ClipMarker.parseCaptureStart("2024-05-17 20-14-06"));
  }

  @Test
  public void formatsOffsetsAsMinutesAndPaddedSeconds()
  {
    assertEquals("0:00", ClipMarker.formatOffset(Duration.ZERO));
    assertEquals("1:35", ClipMarker.formatOffset(Duration.ofSeconds(95)));
    assertEquals("75:03", ClipMarker.formatOffset(Duration.ofMinutes(75).plusSeconds(3)));
  }

  @Test
  public void formatsEntriesWithOptionalLabel()
  {
    assertEquals("1:35", ClipMarker.formatEntry(Duration.ofSeconds(95), null));
    assertEquals("1:35", ClipMarker.formatEntry(Duration.ofSeconds(95), "   "));
    assertEquals("1:35 (nice shot)", ClipMarker.formatEntry(Duration.ofSeconds(95), "  nice shot "));
  }

  @Test
  public void marksAppendOneLinePerMark() throws Exception
  {
    File clipsDir = new File(temp.getRoot(), "clips"); // does not exist yet
    String name = "2024-05-17 20-14-06";

    String first = ClipMarker.mark(name, clipsDir, "", START.plusSeconds(95));
    String second = ClipMarker.mark(name, clipsDir, "ace", START.plusMinutes(12).plusSeconds(4));

    assertEquals("1:35", first);
    assertEquals("12:04", second);
    List<String> lines = Files.readAllLines(new File(clipsDir, name + ".clips").toPath());
    assertEquals(List.of("1:35", "12:04 (ace)"), lines);
  }

  @Test
  public void rejectsNamesThatAreNotTimestamps() throws Exception
  {
    File clipsDir = temp.newFolder("clips");
    assertThrows(DateTimeParseException.class, () -> ClipMarker.mark("game1", clipsDir, "", START));
    assertEquals("nothing written", 0, clipsDir.list().length);
  }

  @Test
  public void rejectsCapturesThatStartInTheFuture() throws Exception
  {
    File clipsDir = temp.newFolder("clips");
    assertThrows(IllegalArgumentException.class,
        () -> ClipMarker.mark("2024-05-17 20-14-06", clipsDir, "", START.minusSeconds(1)));
    assertEquals("nothing written", 0, clipsDir.list().length);
  }
}
