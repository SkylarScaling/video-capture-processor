package com.wheezy.apps.vidcapproc.ui.elements;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wheezy.apps.vidcapproc.FxTestSupport;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class GameSelectorButtonSizeTest
{
  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  @Test
  public void theSelectedBorderDoesNotChangeTheButtonSize() throws Exception
  {
    double[] widths = onFxThread(() ->
    {
      GameSelectorButton button = new GameSelectorButton();
      new Scene(new StackPane(button));
      button.applyCss();
      double plain = button.prefWidth(-1);
      button.selected();
      button.applyCss();
      return new double[] { plain, button.prefWidth(-1), button.minWidth(-1) };
    });

    assertEquals(widths[0], widths[1], 0.01);
    assertEquals(84.0, widths[1], 0.01);
    assertEquals(84.0, widths[2], 0.01);
  }
}
