package com.wheezy.apps.vidcapproc.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.Test;

public class CaptureNameFormatTest
{
  private static final LocalDateTime START = LocalDateTime.of(2024, 5, 17, 20, 14, 6);

  @Test
  public void parsesEachFormat()
  {
    assertEquals(START, CaptureNameFormat.OBS.parse("2024-05-17 20-14-06"));
    assertEquals(START, CaptureNameFormat.UNDERSCORES.parse("2024_5_17_20_14_6"));
    assertEquals(START, CaptureNameFormat.UNDERSCORES.parse("2024_05_17_20_14_06"));
    assertEquals(START, CaptureNameFormat.LIVE_GAMER_PORTABLE.parse("20240517201406"));
  }

  @Test
  public void aFormatDoesNotAcceptTheOthers()
  {
    assertThrows(DateTimeParseException.class, () -> CaptureNameFormat.OBS.parse("20240517201406"));
    assertThrows(DateTimeParseException.class,
        () -> CaptureNameFormat.LIVE_GAMER_PORTABLE.parse("2024-05-17 20-14-06"));
    assertThrows(DateTimeParseException.class, () -> CaptureNameFormat.UNDERSCORES.parse("2024-05-17 20-14-06"));
  }

  @Test
  public void automaticRecognisesAllOfThem()
  {
    assertEquals(START, CaptureNameFormat.AUTOMATIC.parse("2024-05-17 20-14-06"));
    assertEquals(START, CaptureNameFormat.AUTOMATIC.parse("2024_5_17_20_14_6"));
    assertEquals(START, CaptureNameFormat.AUTOMATIC.parse("20240517201406"));
  }

  @Test
  public void automaticStillRejectsNamesThatAreNoDate()
  {
    assertThrows(DateTimeParseException.class, () -> CaptureNameFormat.AUTOMATIC.parse("holiday video"));
    assertThrows(DateTimeParseException.class, () -> CaptureNameFormat.AUTOMATIC.parse(""));
  }

  @Test
  public void aSettingIsReadBackAndAnythingElseMeansAutomatic()
  {
    for (CaptureNameFormat format : CaptureNameFormat.values())
    {
      assertEquals(format, CaptureNameFormat.fromSetting(format.name()));
    }
    assertEquals(CaptureNameFormat.AUTOMATIC, CaptureNameFormat.fromSetting(null));
    assertEquals(CaptureNameFormat.AUTOMATIC, CaptureNameFormat.fromSetting("nonsense"));
  }
}
