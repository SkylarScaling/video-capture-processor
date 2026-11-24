package com.wheezy.apps.vidcapproc.ui;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.util.Duration;

/**
 * Briefly darkens a button so there is visible feedback that an action ran, which
 * matters most when it was triggered by a hotkey instead of a click. Must be used
 * on the JavaFX thread.
 */
public final class ButtonFlash
{
  public static final Duration DURATION = Duration.millis(250);

  private static final Effect FLASH_EFFECT = new ColorAdjust(0, 0, -0.35, 0);
  private static final String STATE_KEY = ButtonFlash.class.getName();

  private record Flashing(PauseTransition pause, Effect originalEffect)
  {
  }

  private ButtonFlash()
  {
  }

  public static void flash(Button button)
  {
    // If the button is already flashing, restart the timer and keep the real original effect
    Flashing current = (Flashing) button.getProperties().get(STATE_KEY);
    Effect original = button.getEffect();
    if (current != null)
    {
      current.pause().stop();
      original = current.originalEffect();
    }

    final Effect effectToRestore = original;
    PauseTransition pause = new PauseTransition(DURATION);
    pause.setOnFinished(event ->
    {
      button.setEffect(effectToRestore);
      button.getProperties().remove(STATE_KEY);
    });

    button.setEffect(FLASH_EFFECT);
    button.getProperties().put(STATE_KEY, new Flashing(pause, original));
    pause.play();
  }
}
