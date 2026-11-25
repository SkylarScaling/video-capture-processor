package com.wheezy.apps.vidcapproc.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Sends everything the app logs to {@code logs/CapProc.log} in the working directory, as well as
 * to the console. Without it a problem on someone else's machine leaves nothing to look at.
 */
public final class AppLogging
{
  public static final Path DEFAULT_LOG_FILE = Path.of("logs", "CapProc.log");

  /** A log bigger than this is set aside at startup (as {@code CapProc.log.1}) and a fresh one is started. */
  public static final long MAX_LOG_BYTES = 1_000_000;

  private static final Logger logger = Logger.getLogger(AppLogging.class.getName());

  private static FileHandler installed;

  private AppLogging()
  {
  }

  /** Logs to the default file. See {@link #install(Path)}. */
  public static synchronized boolean install()
  {
    return install(DEFAULT_LOG_FILE);
  }

  /**
   * Starts logging to the given file (appending), and logs uncaught exceptions to it too.
   * A log that cannot be created is not worth stopping the app for: the problem goes to the
   * console and the app carries on without a log file.
   *
   * @return true if the file is being logged to, including when it already was
   */
  public static synchronized boolean install(Path logFile)
  {
    if (installed != null)
    {
      return true;
    }
    try
    {
      Path parent = logFile.toAbsolutePath().getParent();
      if (parent != null)
      {
        Files.createDirectories(parent);
      }
      rotateIfLarge(logFile, MAX_LOG_BYTES);

      FileHandler handler = new FileHandler(logFile.toString(), true);
      handler.setEncoding(StandardCharsets.UTF_8.name());
      handler.setFormatter(new SimpleFormatter());
      Logger.getLogger("").addHandler(handler);
      installed = handler;

      Thread.setDefaultUncaughtExceptionHandler(
          (thread, error) -> logger.log(Level.SEVERE, "Uncaught exception in thread " + thread.getName(), error));
      return true;
    }
    catch (IOException | RuntimeException e)
    {
      logger.log(Level.SEVERE, "Unable to create the log file " + logFile, e);
      return false;
    }
  }

  /** Stops logging to the file and closes it. */
  public static synchronized void remove()
  {
    if (installed != null)
    {
      Logger.getLogger("").removeHandler(installed);
      installed.close();
      installed = null;
    }
  }

  /** Moves a log of more than {@code maxBytes} to {@code <name>.1}, replacing the previous one. */
  static void rotateIfLarge(Path logFile, long maxBytes) throws IOException
  {
    if (Files.exists(logFile) && Files.size(logFile) > maxBytes)
    {
      Path old = logFile.resolveSibling(logFile.getFileName() + ".1");
      Files.move(logFile, old, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
