package com.wheezy.apps.vidcapproc.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AppLoggingTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private final Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();

  @After
  public void removeLogging()
  {
    AppLogging.remove();
    Thread.setDefaultUncaughtExceptionHandler(originalHandler);
  }

  private static void flush()
  {
    for (Handler handler : Logger.getLogger("").getHandlers())
    {
      handler.flush();
    }
  }

  private static String read(Path file) throws IOException
  {
    return Files.readString(file, StandardCharsets.UTF_8);
  }

  @Test
  public void createsTheLogFolderAndWritesLogsToTheFile() throws Exception
  {
    Path file = temp.getRoot().toPath().resolve("logs").resolve("CapProc.log"); // logs/ does not exist yet

    assertTrue(AppLogging.install(file));
    Logger.getLogger("com.wheezy.test.Something").warning("something went wrong é");
    flush();

    String text = read(file);
    assertTrue(text, text.contains("something went wrong é"));
    assertTrue("the level is recorded", text.contains("WARNING"));
  }

  @Test
  public void appendsToAnExistingLog() throws Exception
  {
    Path file = temp.newFile("CapProc.log").toPath();
    Files.writeString(file, "from the last run\n", StandardCharsets.UTF_8);

    AppLogging.install(file);
    Logger.getLogger("com.wheezy.test.Something").warning("this run");
    flush();

    String text = read(file);
    assertTrue(text.startsWith("from the last run"));
    assertTrue(text.contains("this run"));
  }

  @Test
  public void exceptionsAreLoggedWithTheirStackTrace() throws Exception
  {
    Path file = temp.getRoot().toPath().resolve("CapProc.log");
    AppLogging.install(file);

    Logger.getLogger("com.wheezy.test.Something").log(Level.SEVERE, "Save failed", new IOException("disk full"));
    flush();

    String text = read(file);
    assertTrue(text.contains("Save failed"));
    assertTrue(text.contains("java.io.IOException: disk full"));
  }

  @Test
  public void uncaughtExceptionsEndUpInTheLog() throws Exception
  {
    Path file = temp.getRoot().toPath().resolve("CapProc.log");
    AppLogging.install(file);

    Thread worker = new Thread(() ->
    {
      throw new IllegalStateException("worker blew up");
    }, "test-worker");
    worker.start();
    worker.join();
    flush();

    String text = read(file);
    assertTrue(text, text.contains("Uncaught exception in thread test-worker"));
    assertTrue(text.contains("worker blew up"));
  }

  @Test
  public void installingTwiceDoesNotLogEverythingTwice() throws Exception
  {
    Path file = temp.getRoot().toPath().resolve("CapProc.log");
    AppLogging.install(file);
    assertTrue(AppLogging.install(file));

    Logger.getLogger("com.wheezy.test.Something").warning("only once please");
    flush();

    String text = read(file);
    assertEquals(1, text.split("only once please", -1).length - 1);
  }

  @Test
  public void stopsLoggingAfterItIsRemoved() throws Exception
  {
    Path file = temp.getRoot().toPath().resolve("CapProc.log");
    AppLogging.install(file);
    AppLogging.remove();

    Logger.getLogger("com.wheezy.test.Something").warning("after removal");
    flush();

    assertFalse(read(file).contains("after removal"));
  }

  @Test
  public void aLogThatCannotBeCreatedDoesNotStopTheApp() throws Exception
  {
    File blocker = temp.newFile("logs"); // a file where the log folder should be

    assertFalse(AppLogging.install(blocker.toPath().resolve("CapProc.log")));
  }

  @Test
  public void aBigLogIsSetAsideAtStartup() throws Exception
  {
    Path file = temp.newFile("CapProc.log").toPath();
    Files.writeString(file, "x".repeat(50), StandardCharsets.UTF_8);

    AppLogging.rotateIfLarge(file, 10);

    assertFalse(Files.exists(file));
    assertEquals("x".repeat(50), read(temp.getRoot().toPath().resolve("CapProc.log.1")));
  }

  @Test
  public void aSmallLogIsLeftAlone() throws Exception
  {
    Path file = temp.newFile("CapProc.log").toPath();
    Files.writeString(file, "small", StandardCharsets.UTF_8);

    AppLogging.rotateIfLarge(file, 10);

    assertEquals("small", read(file));
    assertFalse(Files.exists(temp.getRoot().toPath().resolve("CapProc.log.1")));
  }

  @Test
  public void anOlderSetAsideLogIsReplaced() throws Exception
  {
    Path file = temp.newFile("CapProc.log").toPath();
    Path old = temp.newFile("CapProc.log.1").toPath();
    Files.writeString(old, "ancient", StandardCharsets.UTF_8);
    Files.writeString(file, "y".repeat(50), StandardCharsets.UTF_8);

    AppLogging.rotateIfLarge(file, 10);

    assertEquals("y".repeat(50), read(old));
  }
}
