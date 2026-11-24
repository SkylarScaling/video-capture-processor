package com.wheezy.apps.vidcapproc.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.Test;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class WindowSettingsTest
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

  @Test
  public void parsesSavedPositions()
  {
    assertEquals(new Point2D(4235.0, 1217.5), WindowSettings.parsePosition("4235.0", " 1217.5 "));
    assertEquals(new Point2D(-1920.0, 40.0), WindowSettings.parsePosition("-1920", "40")); // monitor to the left
  }

  @Test
  public void ignoresMissingOrBrokenPositions()
  {
    assertNull(WindowSettings.parsePosition(null, "10"));
    assertNull(WindowSettings.parsePosition("10", null));
    assertNull(WindowSettings.parsePosition("", "10"));
    assertNull(WindowSettings.parsePosition("left", "10"));
    assertNull(WindowSettings.parsePosition("NaN", "10"));
    assertNull(WindowSettings.parsePosition("10", "Infinity"));
  }

  @Test
  public void appliesAlwaysOnTopBothWays() throws Exception
  {
    boolean[] results = onFxThread(() ->
    {
      Stage on = new Stage();
      WindowSettings.apply(on, true, null, null);
      Stage off = new Stage();
      WindowSettings.apply(off, false, null, null);
      return new boolean[] { on.isAlwaysOnTop(), off.isAlwaysOnTop() };
    });
    assertTrue(results[0]);
    assertFalse(results[1]);
  }

  @Test
  public void restoresAPositionThatIsOnScreen() throws Exception
  {
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    double x = bounds.getMinX() + 60;
    double y = bounds.getMinY() + 70;

    Point2D position = onFxThread(() ->
    {
      Stage stage = new Stage();
      WindowSettings.apply(stage, true, Double.toString(x), Double.toString(y));
      return new Point2D(stage.getX(), stage.getY());
    });

    assertEquals(x, position.getX(), 0.5);
    assertEquals(y, position.getY(), 0.5);
  }

  @Test
  public void ignoresAPositionOnAMonitorThatIsGone() throws Exception
  {
    Point2D position = onFxThread(() ->
    {
      Stage stage = new Stage();
      WindowSettings.apply(stage, true, "-99999", "-99999");
      return new Point2D(stage.getX(), stage.getY());
    });

    assertNotNull(position);
    assertTrue("x left at its default (unset)", Double.isNaN(position.getX()) || position.getX() > -99999);
    assertTrue("y left at its default (unset)", Double.isNaN(position.getY()) || position.getY() > -99999);
  }
}
