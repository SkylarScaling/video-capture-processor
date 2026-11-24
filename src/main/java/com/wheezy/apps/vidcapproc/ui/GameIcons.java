package com.wheezy.apps.vidcapproc.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.wheezy.apps.vidcapproc.data.Game;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

/**
 * Loads and stores game icons. Icons are shown at most {@value #ICON_SIZE} pixels
 * square, with the aspect ratio kept. A game whose icon file is missing or
 * unreadable (for example, because the drive it was on is gone) shows the bundled
 * default icon instead of failing.
 */
public final class GameIcons
{
  public static final int ICON_SIZE = 64;

  private static final Logger logger = Logger.getLogger(GameIcons.class.getName());

  private GameIcons()
  {
  }

  /** Where new icons are stored: an {@code images} folder in the working directory. */
  public static File defaultImagesDirectory()
  {
    return new File("images").getAbsoluteFile();
  }

  public static Image load(Game game)
  {
    return load(game == null ? null : game.getIconImageFilepath());
  }

  /**
   * @param path an absolute path, or one relative to the working directory; may be null
   * @return the icon, or the default icon if there is no usable file at the path
   */
  public static Image load(String path)
  {
    if (path != null && !path.isBlank())
    {
      File file = resolve(path);
      if (file.isFile())
      {
        Image image = new Image(file.toURI().toString(), ICON_SIZE, ICON_SIZE, true, true);
        if (!image.isError())
        {
          return image;
        }
        logger.warning("Unable to read the game icon " + file + ": " + image.getException());
      }
    }
    return defaultIcon();
  }

  public static Image defaultIcon()
  {
    return new Image(GameIcons.class.getClassLoader().getResourceAsStream(
        VideoCaptureProcessorResources.DEFAULT_GAME_ICON_PATH), ICON_SIZE, ICON_SIZE, true, true);
  }

  /**
   * Reads an image file, scaled down to fit the icon size.
   *
   * @throws IOException if the file is not an image this app can read
   */
  public static Image loadScaled(File file) throws IOException
  {
    Image image = new Image(file.toURI().toString(), ICON_SIZE, ICON_SIZE, true, true);
    if (image.isError())
    {
      throw new IOException("Unable to read the image " + file.getAbsolutePath(), image.getException());
    }
    return image;
  }

  /**
   * Saves a chosen image as a game's icon: scaled to fit the icon size and stored as
   * a PNG named after the game.
   *
   * @param others every other game, so a file one of them uses is not overwritten; the game being
   *               edited should not be included, so it can replace its own icon
   * @param imagesDir where to store the icon
   * @return the path to record in the game: relative to the working directory when the icon is inside it
   * @throws IOException if the image cannot be read or the icon cannot be written
   */
  public static String saveIcon(File source, String title, Collection<Game> others, File imagesDir)
      throws IOException
  {
    BufferedImage icon = toBufferedImage(loadScaled(source));

    Files.createDirectories(imagesDir.toPath());
    String baseName = title.strip().replaceAll("[^A-Za-z0-9\\-_.]", "_");
    if (baseName.isEmpty())
    {
      baseName = "game";
    }

    File target = new File(imagesDir, baseName + ".png");
    for (int copy = 2; target.exists() && usedByAnother(target, others); copy++)
    {
      target = new File(imagesDir, baseName + "_" + copy + ".png");
    }

    if (!ImageIO.write(icon, "png", target))
    {
      throw new IOException("Unable to write the icon " + target.getAbsolutePath());
    }
    return pathToRecord(target);
  }

  /** Relative paths are resolved against the working directory, like the settings file and clips folder. */
  static File resolve(String path)
  {
    return new File(path).getAbsoluteFile();
  }

  private static boolean usedByAnother(File target, Collection<Game> others) throws IOException
  {
    for (Game other : others)
    {
      String otherPath = other.getIconImageFilepath();
      if (otherPath != null && !otherPath.isBlank()
          && resolve(otherPath).getCanonicalFile().equals(target.getCanonicalFile()))
      {
        return true;
      }
    }
    return false;
  }

  private static String pathToRecord(File target)
  {
    Path workingDirectory = Path.of("").toAbsolutePath().normalize();
    Path absolute = target.toPath().toAbsolutePath().normalize();
    return absolute.startsWith(workingDirectory)
        ? workingDirectory.relativize(absolute).toString().replace('\\', '/')
        : absolute.toString();
  }

  private static BufferedImage toBufferedImage(Image image)
  {
    int width = (int) image.getWidth();
    int height = (int) image.getHeight();
    PixelReader pixels = image.getPixelReader();
    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++)
    {
      for (int x = 0; x < width; x++)
      {
        result.setRGB(x, y, pixels.getArgb(x, y));
      }
    }
    return result;
  }
}
