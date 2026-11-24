package com.wheezy.apps.vidcapproc.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.Test;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;

public class ButtonFlashTest
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

  private static void waitForFlashToEnd() throws InterruptedException
  {
    Thread.sleep((long) ButtonFlash.DURATION.toMillis() + 600);
  }

  @Test
  public void flashShowsAnEffectThenRestoresTheButton() throws Exception
  {
    Button button = onFxThread(Button::new);

    Effect during = onFxThread(() ->
    {
      ButtonFlash.flash(button);
      return button.getEffect();
    });
    assertNotNull("effect while flashing", during);

    waitForFlashToEnd();
    assertNull("effect restored", onFxThread(button::getEffect));
  }

  @Test
  public void flashingAgainMidFlashStillRestoresTheRealOriginalEffect() throws Exception
  {
    DropShadow original = new DropShadow();
    Button button = onFxThread(() ->
    {
      Button b = new Button();
      b.setEffect(original);
      return b;
    });

    onFxThread(() ->
    {
      ButtonFlash.flash(button);
      ButtonFlash.flash(button); // retrigger before the first one ended
      return null;
    });

    waitForFlashToEnd();
    assertSame("original effect, not the flash effect", original, onFxThread(button::getEffect));
  }
}
