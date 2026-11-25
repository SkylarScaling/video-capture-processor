package com.wheezy.apps.vidcapproc.controller;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.FxTestSupport.AppData;
import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.utils.CaptureNameFormat;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

/** The capture file name format and per-game folder choices in the Settings dialog. */
public class SettingsDialogOptionsTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  private FXMLLoader loadDialog() throws Exception
  {
    return onFxThread(() ->
    {
      try
      {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
            .getResource(VideoCaptureProcessorResources.SETTINGS_DIALOG_FXML));
        loader.load();
        return loader;
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    });
  }

  @SuppressWarnings("unchecked")
  private static ComboBox<CaptureNameFormat> formatBox(FXMLLoader loader)
  {
    return (ComboBox<CaptureNameFormat>) ((Parent) loader.getRoot()).lookup("#captureNameFormatComboBox");
  }

  private static CheckBox gameFoldersBox(FXMLLoader loader)
  {
    return (CheckBox) ((Parent) loader.getRoot()).lookup("#gameFoldersCheckBox");
  }

  @Test
  public void startsWithDetectAutomaticallyAndNoGameFolders() throws Exception
  {
    FxTestSupport.useTemporaryAppData(temp.getRoot());
    FXMLLoader loader = loadDialog();

    assertEquals(CaptureNameFormat.AUTOMATIC, formatBox(loader).getValue());
    assertEquals(List_of(CaptureNameFormat.values()), formatBox(loader).getItems());
    assertFalse(gameFoldersBox(loader).isSelected());
  }

  private static java.util.List<CaptureNameFormat> List_of(CaptureNameFormat[] values)
  {
    return java.util.List.of(values);
  }

  @Test
  public void showsWhatWasSaved() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    data.properties().setProperty("CaptureNameFormat", "LIVE_GAMER_PORTABLE");
    data.properties().setProperty("GameFolders", "true");

    FXMLLoader loader = loadDialog();

    assertEquals(CaptureNameFormat.LIVE_GAMER_PORTABLE, formatBox(loader).getValue());
    assertTrue(gameFoldersBox(loader).isSelected());
  }

  @Test
  public void savingWithBlankFoldersIsFine() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    FXMLLoader loader = loadDialog();

    onFxThread(() -> ((VideoCaptureProcessorSettingsController) loader.getController()).applyToProperties());

    assertEquals("", data.properties().getProperty("CaptureLoc"));
  }

  @Test
  public void savesTheChoices() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    FXMLLoader loader = loadDialog();

    onFxThread(() ->
    {
      formatBox(loader).setValue(CaptureNameFormat.UNDERSCORES);
      gameFoldersBox(loader).setSelected(true);
      ((VideoCaptureProcessorSettingsController) loader.getController()).applyToProperties();
    });

    assertEquals(CaptureNameFormat.UNDERSCORES, data.properties().getCaptureNameFormat());
    assertTrue(data.properties().isGameFolders());

    VideoCaptureProcessorProperties saved = new VideoCaptureProcessorProperties(
        new File(temp.getRoot(), "CapProcConfig.xml").getPath());
    saved.loadProperties();
    assertEquals(CaptureNameFormat.UNDERSCORES, saved.getCaptureNameFormat());
    assertTrue(saved.isGameFolders());
  }
}
