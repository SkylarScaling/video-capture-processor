package com.wheezy.apps.vidcapproc.controller;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** The main window while a move or cleanup runs: the progress bar and the close warning. */
public class MainWindowTaskTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private Stage stage;
  private VideoCaptureProcessorController controller;

  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  @Before
  public void showMainWindow() throws Exception
  {
    FxTestSupport.useTemporaryAppData(temp.getRoot());
    VideoCaptureProcessorStage mainStage = onFxThread(() ->
    {
      VideoCaptureProcessorStage created = new VideoCaptureProcessorStage(
          VideoCaptureProcessorResources.VIDEO_CAPTURE_PROCESSOR_FXML, "test", false);
      created.show();
      return created;
    });
    stage = mainStage;
    controller = (VideoCaptureProcessorController) mainStage.getController();
  }

  /** A task that reports half its progress, then waits until the test lets it finish. */
  private static class SlowTask extends Task<Boolean>
  {
    final CountDownLatch halfWay = new CountDownLatch(1);
    final CountDownLatch finish = new CountDownLatch(1);

    @Override
    protected Boolean call() throws Exception
    {
      updateMessage("Moving...");
      updateProgress(1, 2);
      halfWay.countDown();
      finish.await(10, TimeUnit.SECONDS);
      return true;
    }
  }

  private ProgressBar progressBar()
  {
    return (ProgressBar) stage.getScene().getRoot().lookup("#progressBar");
  }

  private void startTracking(SlowTask task) throws Exception
  {
    Button keepers = (Button) stage.getScene().getRoot().lookup("#keepersButton");
    onFxThread(() ->
    {
      controller.trackTask(task, List.of(keepers), ok -> ok, "done", "failed", null);
      return null;
    });
    new Thread(task).start();
  }

  private void waitFor(BooleanSupplier condition) throws Exception
  {
    for (int i = 0; i < 100; i++)
    {
      if (onFxThread(condition::getAsBoolean))
      {
        return;
      }
      Thread.sleep(50);
    }
    throw new AssertionError("Timed out waiting");
  }

  @Test
  public void theProgressBarIsHiddenUntilSomethingRuns() throws Exception
  {
    assertFalse(onFxThread(() -> progressBar().isVisible()));
  }

  @Test
  public void theProgressBarShowsTheTaskProgressAndHidesWhenItEnds() throws Exception
  {
    SlowTask task = new SlowTask();
    startTracking(task);
    assertTrue(task.halfWay.await(5, TimeUnit.SECONDS));

    assertTrue(onFxThread(() -> progressBar().isVisible()));
    waitFor(() -> progressBar().getProgress() == 0.5);
    assertEquals("Moving...", onFxThread(() -> ((Label) stage.getScene().getRoot().lookup("#statusLabel")).getText()));

    task.finish.countDown();
    waitFor(() -> !progressBar().isVisible());
  }

  @Test
  public void aTaskThatReportsNoProgressShowsAnIndeterminateBar() throws Exception
  {
    SlowTask task = new SlowTask()
    {
      @Override
      protected Boolean call() throws Exception
      {
        halfWay.countDown();
        finish.await(10, TimeUnit.SECONDS);
        return true;
      }
    };
    startTracking(task);
    assertTrue(task.halfWay.await(5, TimeUnit.SECONDS));

    assertTrue(onFxThread(() -> progressBar().isIndeterminate()));
    task.finish.countDown();
    waitFor(() -> !progressBar().isVisible());
  }

  @Test
  public void closingWhileIdleDoesNotAsk() throws Exception
  {
    AtomicInteger asked = new AtomicInteger();
    controller.setCloseAnywayConfirmation(() ->
    {
      asked.incrementAndGet();
      return false;
    });

    onFxThread(() ->
    {
      stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
      return null;
    });

    assertEquals(0, asked.get());
    assertFalse(onFxThread(() -> stage.isShowing()));
  }

  @Test
  public void cancellingTheCloseWarningKeepsTheWindowOpen() throws Exception
  {
    AtomicInteger asked = new AtomicInteger();
    controller.setCloseAnywayConfirmation(() ->
    {
      asked.incrementAndGet();
      return false;
    });
    SlowTask task = new SlowTask();
    startTracking(task);
    assertTrue(task.halfWay.await(5, TimeUnit.SECONDS));

    onFxThread(() ->
    {
      stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
      return null;
    });

    assertEquals(1, asked.get());
    assertTrue(onFxThread(() -> stage.isShowing()));

    task.finish.countDown();
    waitFor(() -> !progressBar().isVisible());
    onFxThread(() ->
    {
      stage.close();
      return null;
    });
  }

  @Test
  public void closeAnywayClosesTheWindowWhileTheWorkRuns() throws Exception
  {
    controller.setCloseAnywayConfirmation(() -> true);
    SlowTask task = new SlowTask();
    startTracking(task);
    assertTrue(task.halfWay.await(5, TimeUnit.SECONDS));

    onFxThread(() ->
    {
      stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
      return null;
    });

    assertFalse(onFxThread(() -> stage.isShowing()));
    task.finish.countDown();
  }

  @Test
  public void closingIsFreeAgainOnceTheWorkIsDone() throws Exception
  {
    AtomicInteger asked = new AtomicInteger();
    controller.setCloseAnywayConfirmation(() ->
    {
      asked.incrementAndGet();
      return false;
    });
    SlowTask task = new SlowTask();
    startTracking(task);
    task.finish.countDown();
    waitFor(() -> !progressBar().isVisible());

    onFxThread(() ->
    {
      stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
      return null;
    });

    assertEquals(0, asked.get());
    assertFalse(onFxThread(() -> stage.isShowing()));
  }
}
