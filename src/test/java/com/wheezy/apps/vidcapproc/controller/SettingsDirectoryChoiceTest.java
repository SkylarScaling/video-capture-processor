package com.wheezy.apps.vidcapproc.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SettingsDirectoryChoiceTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void opensAtTheDirectoryAlreadyInTheField() throws Exception
  {
    File current = temp.newFolder("current");
    File last = temp.newFolder("last");

    File result = VideoCaptureProcessorSettingsController.resolveInitialDirectory(current.getAbsolutePath(),
        last.getAbsolutePath());

    assertEquals(current, result);
  }

  @Test
  public void fallsBackToTheLastChoiceWhenTheFieldIsEmptyOrInvalid() throws Exception
  {
    File last = temp.newFolder("last");

    assertEquals(last, VideoCaptureProcessorSettingsController.resolveInitialDirectory("", last.getAbsolutePath()));
    assertEquals(last, VideoCaptureProcessorSettingsController.resolveInitialDirectory(null, last.getAbsolutePath()));
    assertEquals(last, VideoCaptureProcessorSettingsController
        .resolveInitialDirectory(new File(temp.getRoot(), "gone").getAbsolutePath(), "  " + last.getAbsolutePath()));
  }

  @Test
  public void usesTheSystemDefaultWhenNothingUsable() throws Exception
  {
    File aFile = temp.newFile("not-a-directory.txt");

    assertNull(VideoCaptureProcessorSettingsController.resolveInitialDirectory(null, null));
    assertNull(VideoCaptureProcessorSettingsController.resolveInitialDirectory("   ", ""));
    assertNull(VideoCaptureProcessorSettingsController.resolveInitialDirectory(aFile.getAbsolutePath(),
        new File(temp.getRoot(), "gone").getAbsolutePath()));
  }
}
