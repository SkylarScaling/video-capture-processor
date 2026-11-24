package com.wheezy.apps.vidcapproc.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wheezy.apps.vidcapproc.controller.VideoCaptureProcessorController;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MainWindowFxmlTest
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
  public void mainWindowLoadsWithActionButtonsWired() throws Exception
  {
    CompletableFuture<FXMLLoader> loaded = new CompletableFuture<>();
    Platform.runLater(() ->
    {
      try
      {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
            .getResource(VideoCaptureProcessorResources.VIDEO_CAPTURE_PROCESSOR_FXML));
        loader.load();
        loaded.complete(loader);
      }
      catch (Throwable t)
      {
        loaded.completeExceptionally(t);
      }
    });

    FXMLLoader loader = loaded.get(20, TimeUnit.SECONDS);
    assertTrue(loader.getController() instanceof VideoCaptureProcessorController);

    Parent root = loader.getRoot();
    for (String id : new String[] { "keepersButton", "clipsButton", "markClipButton" })
    {
      Button button = (Button) root.lookup("#" + id);
      assertNotNull(id + " present", button);
      assertNotNull(id + " has an action", button.getOnAction());
    }
    assertTrue(root.lookup("#clipLabelField") instanceof TextField);
    assertTrue(root.lookup("#statusLabel") instanceof Label);
  }
}
