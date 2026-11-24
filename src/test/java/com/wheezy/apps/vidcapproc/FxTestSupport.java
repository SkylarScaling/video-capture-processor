package com.wheezy.apps.vidcapproc;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.wheezy.apps.vidcapproc.data.GameCatalog;
import com.wheezy.apps.vidcapproc.data.GameSelection;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;

import javafx.application.Platform;

/** Helpers for tests that need the JavaFX toolkit. */
public final class FxTestSupport
{
  /** The app's settings, games and selected game, as the tests see them. */
  public record AppData(VideoCaptureProcessorProperties properties, GameCatalog catalog, GameSelection selection)
  {
  }

  /**
   * Points the app's settings, games and game selection at fresh files in the given
   * directory, so a test that loads the main window never reads or changes the real ones.
   */
  public static AppData useTemporaryAppData(File directory)
  {
    VideoCaptureProcessorProperties properties = new VideoCaptureProcessorProperties(
        new File(directory, "CapProcConfig.xml").getPath());
    VideoCaptureProcessorProperties.setInstance(properties);

    GameCatalog catalog = new GameCatalog(new File(directory, "SavedGames.xml"));
    GameCatalog.setInstance(catalog);

    GameSelection.setInstance(null); // so the next getInstance() is built from the settings and games above
    return new AppData(properties, catalog, GameSelection.getInstance());
  }

  private FxTestSupport()
  {
  }

  /** Starts the toolkit if it is not running yet. */
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

  /** Runs the work on the JavaFX thread and returns its result, rethrowing anything it throws. */
  public static <T> T onFxThread(Supplier<T> work) throws Exception
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

  /** Like {@link #onFxThread(Supplier)} for work with no result. */
  public static void onFxThread(Runnable work) throws Exception
  {
    onFxThread(() ->
    {
      work.run();
      return null;
    });
  }
}
