package com.wheezy.apps.vidcapproc.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.Test;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

public class HotkeysTest
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

  private static <T> T onFxThread(Supplier<T> work) throws Exception
  {
    CompletableFuture<T> result = new CompletableFuture<>();
    Platform.runLater(() ->
    {
      try
      {
        result.complete(work.get());
      }
      catch (Throwable t)
      {
        result.completeExceptionally(t);
      }
    });
    return result.get(20, TimeUnit.SECONDS);
  }

  private static Scene loadMainWindowScene()
  {
    try
    {
      FXMLLoader loader = new FXMLLoader(HotkeysTest.class.getClassLoader()
          .getResource(VideoCaptureProcessorResources.VIDEO_CAPTURE_PROCESSOR_FXML));
      Parent root = loader.load();
      return new Scene(root); // the controller installs hotkeys once the root has a scene
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private static void press(Scene scene, KeyCode key)
  {
    Runnable action = scene.getAccelerators().get(new KeyCodeCombination(key));
    assertNotNull(key + " has a hotkey", action);
    action.run();
  }

  @Test
  public void f1F2F3PressKeepersClipsAndMarkClip() throws Exception
  {
    int[] counts = onFxThread(() ->
    {
      Scene scene = loadMainWindowScene();
      AtomicInteger keepers = new AtomicInteger();
      AtomicInteger clips = new AtomicInteger();
      AtomicInteger mark = new AtomicInteger();
      ((Button) scene.getRoot().lookup("#keepersButton")).setOnAction(e -> keepers.incrementAndGet());
      ((Button) scene.getRoot().lookup("#clipsButton")).setOnAction(e -> clips.incrementAndGet());
      ((Button) scene.getRoot().lookup("#markClipButton")).setOnAction(e -> mark.incrementAndGet());

      press(scene, KeyCode.F1);
      press(scene, KeyCode.F2);
      press(scene, KeyCode.F2);
      press(scene, KeyCode.F3);
      press(scene, KeyCode.F3);
      press(scene, KeyCode.F3);
      return new int[] { keepers.get(), clips.get(), mark.get() };
    });

    assertEquals(1, counts[0]);
    assertEquals(2, counts[1]);
    assertEquals(3, counts[2]);
  }

  @Test
  public void hotkeyDoesNothingWhileItsButtonIsDisabled() throws Exception
  {
    int count = onFxThread(() ->
    {
      Scene scene = loadMainWindowScene();
      AtomicInteger keepers = new AtomicInteger();
      Button button = (Button) scene.getRoot().lookup("#keepersButton");
      button.setOnAction(e -> keepers.incrementAndGet());

      button.setDisable(true); // what a file move in progress does
      press(scene, KeyCode.F1);
      return keepers.get();
    });

    assertEquals(0, count);
  }

  @Test
  public void tooltipsTellTheUserTheKeys() throws Exception
  {
    String[] tips = onFxThread(() ->
    {
      Scene scene = loadMainWindowScene();
      return new String[] { ((Button) scene.getRoot().lookup("#keepersButton")).getTooltip().getText(),
          ((Button) scene.getRoot().lookup("#clipsButton")).getTooltip().getText(),
          ((Button) scene.getRoot().lookup("#markClipButton")).getTooltip().getText() };
    });

    assertTrue(tips[0], tips[0].contains("(F1)"));
    assertTrue(tips[1], tips[1].contains("(F2)"));
    assertTrue(tips[2], tips[2].contains("(F3)"));
  }

  @Test
  public void realKeyEventsReachTheHotkeys() throws Exception
  {
    int count = onFxThread(() ->
    {
      Scene scene = loadMainWindowScene();
      AtomicInteger clips = new AtomicInteger();
      ((Button) scene.getRoot().lookup("#clipsButton")).setOnAction(e -> clips.incrementAndGet());

      // Fired at the label field, since that is where focus usually is while recording
      javafx.scene.Node target = scene.getRoot().lookup("#clipLabelField");
      javafx.event.Event.fireEvent(target, new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED,
          "", "", KeyCode.F2, false, false, false, false));
      return clips.get();
    });

    assertEquals(1, count);
  }
}
