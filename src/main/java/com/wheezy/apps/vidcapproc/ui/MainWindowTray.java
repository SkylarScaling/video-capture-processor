package com.wheezy.apps.vidcapproc.ui;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * A system tray icon for the main window, so the buttons are within reach when the window
 * is minimized: clicking the icon restores the window and brings it to the front, and its menu has
 * Keepers, Clips, Mark Clip and Exit. The icon is removed when the window closes.
 * <p>
 * Closing the window still ends the app. The tray is an extra way in, not a place to hide.
 */
public final class MainWindowTray
{
  private static final Logger logger = Logger.getLogger(MainWindowTray.class.getName());
  private static final String ICON_RESOURCE = "com/wheezy/apps/vidcapproc/icons/VCPAppIcon.png";

  private MainWindowTray()
  {
  }

  /**
   * Puts the icon in the tray. Call on the JavaFX thread.
   *
   * @return the icon, or null if the system has no tray (or it could not be added), in which case the app works as before
   */
  public static TrayIcon install(Stage stage, Runnable keepers, Runnable clips, Runnable markClip)
  {
    if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported())
    {
      return null;
    }
    try
    {
      SystemTray tray = SystemTray.getSystemTray();
      TrayIcon icon = new TrayIcon(loadIcon(tray), stage.getTitle(),
          buildMenu(() -> exit(stage), keepers, clips, markClip));
      icon.setImageAutoSize(true);
      icon.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent event)
        {
          if (event.getButton() == MouseEvent.BUTTON1)
          {
            Platform.runLater(() -> show(stage));
          }
        }
      });
      tray.add(icon);

      // Whatever closes the window takes the icon with it, or it would stay behind
      stage.showingProperty().addListener((observable, wasShowing, isShowing) ->
      {
        if (!isShowing)
        {
          tray.remove(icon);
        }
      });
      return icon;
    }
    catch (AWTException | IOException | RuntimeException e)
    {
      logger.log(Level.WARNING, "Unable to add the tray icon", e);
      return null;
    }
  }

  /**
   * The icon's menu. Each choice runs on the JavaFX thread, since the tray calls back on its own thread.
   */
  static PopupMenu buildMenu(Runnable exit, Runnable keepers, Runnable clips, Runnable markClip)
  {
    PopupMenu menu = new PopupMenu();
    menu.add(item("Keepers", keepers));
    menu.add(item("Clips", clips));
    menu.add(item("Mark Clip", markClip));
    menu.addSeparator();
    menu.add(item("Exit", exit));
    return menu;
  }

  private static MenuItem item(String label, Runnable action)
  {
    MenuItem item = new MenuItem(label);
    item.addActionListener(event -> Platform.runLater(action));
    return item;
  }

  /**
   * Brings the window to the front, restoring it first if it is minimized. (It cannot also minimize on a
   * second click: clicking the tray takes focus from the window, so a focused window cannot be told apart.)
   */
  static void show(Stage stage)
  {
    stage.setIconified(false);
    stage.toFront();
    stage.requestFocus();
  }

  /** Closes the window as its close button would, so the "work in progress" question is still asked. */
  private static void exit(Stage stage)
  {
    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
  }

  private static Image loadIcon(SystemTray tray) throws IOException
  {
    try (InputStream in = MainWindowTray.class.getClassLoader().getResourceAsStream(ICON_RESOURCE))
    {
      if (in == null)
      {
        throw new IOException("Missing " + ICON_RESOURCE);
      }
      return ImageIO.read(in).getScaledInstance(tray.getTrayIconSize().width, tray.getTrayIconSize().height,
          Image.SCALE_SMOOTH);
    }
  }
}
