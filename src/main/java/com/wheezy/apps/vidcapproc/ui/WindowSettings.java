package com.wheezy.apps.vidcapproc.ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;

import javafx.geometry.Point2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Applies the saved window settings (always on top, position) to the main
 * window, and remembers the position when the window closes.
 */
public final class WindowSettings
{
  private static final Logger logger = Logger.getLogger(WindowSettings.class.getName());

  /** Size of the area at the saved position that must be on a screen for it to be restored. */
  private static final double VISIBLE_WIDTH = 100;
  private static final double VISIBLE_HEIGHT = 50;

  private WindowSettings()
  {
  }

  /**
   * Applies saved settings to a stage that is not showing yet.
   *
   * @param x saved X position as text, or null if never saved
   * @param y saved Y position as text, or null if never saved
   */
  public static void apply(Stage stage, boolean alwaysOnTop, String x, String y)
  {
    stage.setAlwaysOnTop(alwaysOnTop);

    Point2D position = parsePosition(x, y);
    if (position != null && isOnScreen(position))
    {
      stage.setX(position.getX());
      stage.setY(position.getY());
    }
  }

  /** Applies the saved settings from the properties file. */
  public static void apply(Stage stage, VideoCaptureProcessorProperties props)
  {
    apply(stage, props.isAlwaysOnTop(), props.getProperty(VideoCaptureProcessorProperty.FRAME_LOCATION_X_PROPERTY),
        props.getProperty(VideoCaptureProcessorProperty.FRAME_LOCATION_Y_PROPERTY));
  }

  /** Saves the stage's position. A failure to save is logged, not reported: it is not worth interrupting closing. */
  public static void rememberPosition(Stage stage, VideoCaptureProcessorProperties props)
  {
    props.setProperty(VideoCaptureProcessorProperty.FRAME_LOCATION_X_PROPERTY, Double.toString(stage.getX()));
    props.setProperty(VideoCaptureProcessorProperty.FRAME_LOCATION_Y_PROPERTY, Double.toString(stage.getY()));
    try
    {
      props.storeProperties();
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, "Property Store Error", e);
    }
  }

  /** @return the position, or null if either value is missing, not a number, or not finite */
  static Point2D parsePosition(String x, String y)
  {
    if (x == null || y == null)
    {
      return null;
    }
    try
    {
      double parsedX = Double.parseDouble(x.trim());
      double parsedY = Double.parseDouble(y.trim());
      return Double.isFinite(parsedX) && Double.isFinite(parsedY) ? new Point2D(parsedX, parsedY) : null;
    }
    catch (NumberFormatException e)
    {
      return null;
    }
  }

  /** A saved position is ignored if its monitor is gone (or the layout changed), so the window can't open off screen. */
  static boolean isOnScreen(Point2D position)
  {
    return !Screen.getScreensForRectangle(position.getX(), position.getY(), VISIBLE_WIDTH, VISIBLE_HEIGHT).isEmpty();
  }
}
