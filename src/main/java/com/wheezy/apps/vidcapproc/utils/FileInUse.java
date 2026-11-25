package com.wheezy.apps.vidcapproc.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Finds out whether another program, usually the recorder, still has a capture open.
 * <p>
 * On Windows a file that another program has open cannot be renamed or moved, and
 * moving a recording that is still being written would lose the end of it or fail half
 * way. The probe does exactly what a move does, a rename, and puts the name back at once.
 * Other systems let you rename open files, so there it can find nothing.
 */
final class FileInUse
{
  private static final Logger logger = Logger.getLogger(FileInUse.class.getName());

  private FileInUse()
  {
  }

  /**
   * @return true if the file cannot be renamed, so something else has it open (or it is
   *         protected in a way that would stop a move anyway)
   * @throws IOException if the file was renamed but could not be given its name back; the
   *         message says where it is
   */
  static boolean isInUse(File file) throws IOException
  {
    if (!file.isFile())
    {
      return false;
    }
    File probe = new File(file.getParentFile(), file.getName() + "." + UUID.randomUUID() + ".probe");
    try
    {
      Files.move(file.toPath(), probe.toPath());
    }
    catch (IOException e)
    {
      logger.info(file + " cannot be renamed, so it is in use: " + e);
      return true;
    }
    try
    {
      Files.move(probe.toPath(), file.toPath());
    }
    catch (IOException e)
    {
      throw new IOException("Could not restore the name of " + file + ". It is now " + probe, e);
    }
    return false;
  }
}
