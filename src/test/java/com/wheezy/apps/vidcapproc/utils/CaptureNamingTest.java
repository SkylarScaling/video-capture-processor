package com.wheezy.apps.vidcapproc.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CaptureNamingTest
{
  private static final String ORIGINAL = "2024-05-17 20-14-06";

  @Test
  public void keepsTheOriginalNameWhenThereAreNoLabels()
  {
    assertEquals(ORIGINAL, CaptureNaming.buildName(null, null, ORIGINAL));
    assertEquals(ORIGINAL, CaptureNaming.buildName("", "   ", ORIGINAL));
  }

  @Test
  public void prefixesTheFilenameLabel()
  {
    assertEquals("ace_" + ORIGINAL, CaptureNaming.buildName(null, "ace", ORIGINAL));
    assertEquals("ace_" + ORIGINAL, CaptureNaming.buildName(null, "  ace ", ORIGINAL));
  }

  @Test
  public void putsTheGameLabelBeforeTheFilenameLabel()
  {
    assertEquals("BF3_ace_" + ORIGINAL, CaptureNaming.buildName("BF3", "ace", ORIGINAL));
    assertEquals("BF3_" + ORIGINAL, CaptureNaming.buildName("BF3", null, ORIGINAL));
  }

  @Test
  public void replacesCharactersThatAreNotSafeInFilenames()
  {
    assertEquals("nice_shot__1_" + ORIGINAL, CaptureNaming.buildName(null, "nice shot! 1", ORIGINAL));
    assertEquals("a_b_c_" + ORIGINAL, CaptureNaming.buildName(null, "a/b\\c", ORIGINAL));
  }
}
