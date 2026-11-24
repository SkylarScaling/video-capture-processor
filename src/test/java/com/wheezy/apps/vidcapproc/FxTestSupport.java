package com.wheezy.apps.vidcapproc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javafx.application.Platform;

/** Helpers for tests that need the JavaFX toolkit. */
public final class FxTestSupport
{
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
