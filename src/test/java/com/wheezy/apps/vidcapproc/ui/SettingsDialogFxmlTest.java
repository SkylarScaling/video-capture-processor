package com.wheezy.apps.vidcapproc.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wheezy.apps.vidcapproc.controller.VideoCaptureProcessorSettingsController;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;

public class SettingsDialogFxmlTest
{
  @BeforeClass
  public static void startToolkit()
  {
    try
    {
      Platform.startup(() -> {});
    }
    catch (IllegalStateException alreadyStarted)
    {
      // fine: another test started it
    }
  }

  @Test
  public void settingsDialogLoadsWithAllFolderFields() throws Exception
  {
    CompletableFuture<FXMLLoader> loaded = new CompletableFuture<>();
    Platform.runLater(() ->
    {
      try
      {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
            .getResource(VideoCaptureProcessorResources.SETTINGS_DIALOG_FXML));
        Parent root = loader.load();
        assertNotNull(root);
        loaded.complete(loader);
      }
      catch (Throwable t)
      {
        loaded.completeExceptionally(t);
      }
    });

    FXMLLoader loader = loaded.get(20, TimeUnit.SECONDS);
    assertTrue(loader.getController() instanceof VideoCaptureProcessorSettingsController);

    // All three folder fields are injected and discoverable by id
    Parent root = loader.getRoot();
    for (String id : new String[] { "captureTextField", "keepersTextField", "clipsTextField" })
    {
      assertTrue(id + " present", root.lookup("#" + id) instanceof TextField);
    }
  }
}
