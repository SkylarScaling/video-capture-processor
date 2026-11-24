package com.wheezy.apps.vidcapproc.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The user's games, in order, stored in {@code SavedGames.xml}.
 * <p>
 * The games are exposed as a read-only {@link ObservableList}, so screens can
 * listen for changes. Every change is saved to disk straight away, and undone in
 * memory if the save fails, so the list and the file never disagree. Game titles
 * are unique, ignoring case.
 */
public class GameCatalog
{
  public static final String DEFAULT_FILENAME = "SavedGames.xml";

  private static final Logger logger = Logger.getLogger(GameCatalog.class.getName());
  private static GameCatalog instance;

  private final File file;
  private final ObservableList<Game> games = FXCollections.observableArrayList();
  private final ObservableList<Game> readOnlyGames = FXCollections.unmodifiableObservableList(games);
  private String loadFailure;

  public GameCatalog(File file)
  {
    this.file = file;
  }

  /** The app's catalog, stored in the working directory. A file that cannot be read is reported by {@link #takeLoadFailure()}. */
  public static synchronized GameCatalog getInstance()
  {
    if (instance == null)
    {
      instance = new GameCatalog(new File(DEFAULT_FILENAME));
      try
      {
        instance.load();
      }
      catch (IOException e)
      {
        logger.log(Level.SEVERE, "Game Load Error", e);
        instance.loadFailure = e.getMessage();
      }
    }
    return instance;
  }

  /** Replaces the app's catalog. Tests use this so that they never read or change the real games file. */
  public static synchronized void setInstance(GameCatalog catalog)
  {
    instance = catalog;
  }

  public ObservableList<Game> getGames()
  {
    return readOnlyGames;
  }

  /** @return what went wrong loading the games the first time, once; empty after that or if nothing did */
  public synchronized Optional<String> takeLoadFailure()
  {
    String failure = loadFailure;
    loadFailure = null;
    return Optional.ofNullable(failure);
  }

  public Optional<Game> find(String title)
  {
    return games.stream().filter(game -> sameTitle(game.getGameTitle(), title)).findFirst();
  }

  /** @param ignoring a game that may keep the title (the one being edited), or null */
  public boolean isTitleTaken(String title, Game ignoring)
  {
    return games.stream().anyMatch(game -> game != ignoring && sameTitle(game.getGameTitle(), title));
  }

  /**
   * Reads the games from the file; a missing file means no games.
   * <p>
   * Games with no title, and repeats of a title (ignoring case), are skipped, since
   * titles identify games. If the file cannot be parsed, a copy is kept next to it as
   * {@code .unreadable} so the next save cannot silently destroy it.
   *
   * @throws IOException if the file exists but cannot be read
   */
  public synchronized void load() throws IOException
  {
    if (!file.exists())
    {
      games.clear();
      return;
    }

    GameCollection collection;
    try
    {
      collection = (GameCollection) JAXBContext.newInstance(GameCollection.class).createUnmarshaller().unmarshal(file);
    }
    catch (JAXBException e)
    {
      File copy = new File(file.getPath() + ".unreadable");
      Files.copy(file.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
      throw new IOException("The games file " + file.getAbsolutePath() + " could not be read. A copy was kept as "
          + copy.getName() + ".", e);
    }

    Map<String, Game> byTitle = new LinkedHashMap<>();
    for (Game game : collection.getGameSet())
    {
      String title = game.getGameTitle();
      if (title == null || title.isBlank())
      {
        logger.warning("Skipping a game with no title in " + file);
      }
      else if (byTitle.putIfAbsent(title.toLowerCase(Locale.ROOT), game) != null)
      {
        logger.warning("Skipping a repeat of the game title '" + title + "' in " + file);
      }
    }
    games.setAll(byTitle.values());
  }

  /**
   * Writes the games to the file. The new file is written beside the old one and
   * swapped in, and the previous version is kept as {@code .bak}.
   *
   * @throws IOException if the file cannot be written
   */
  public synchronized void save() throws IOException
  {
    GameCollection collection = new GameCollection();
    collection.getGameSet().addAll(games);

    File temp = new File(file.getPath() + ".tmp");
    try
    {
      Marshaller marshaller = JAXBContext.newInstance(GameCollection.class).createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(collection, temp);
    }
    catch (JAXBException e)
    {
      throw new IOException("Unable to write the games file " + file.getAbsolutePath(), e);
    }

    if (file.exists())
    {
      Files.copy(file.toPath(), new File(file.getPath() + ".bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    try
    {
      Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
    catch (AtomicMoveNotSupportedException e)
    {
      Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Adds a game at the end of the list.
   *
   * @throws IllegalArgumentException if the title is blank or already used
   * @throws IOException              if the change could not be saved (nothing changed)
   */
  public void add(Game game) throws IOException
  {
    requireUsableTitle(game, null);
    games.add(game);
    saveOrUndo(() -> games.remove(games.size() - 1));
  }

  /**
   * Replaces a game with an edited version, in the same position.
   *
   * @throws IllegalArgumentException if the new title is blank or used by another game
   * @throws IOException              if the change could not be saved (nothing changed)
   */
  public void replace(Game original, Game updated) throws IOException
  {
    int index = indexOf(original);
    requireUsableTitle(updated, original);
    games.set(index, updated);
    saveOrUndo(() -> games.set(index, original));
  }

  /** @throws IOException if the change could not be saved (nothing changed) */
  public void remove(Game game) throws IOException
  {
    int index = indexOf(game);
    games.remove(index);
    saveOrUndo(() -> games.add(index, game));
  }

  /** Moves a game one place up the list. Does nothing if it is already first. */
  public void moveUp(Game game) throws IOException
  {
    move(game, -1);
  }

  /** Moves a game one place down the list. Does nothing if it is already last. */
  public void moveDown(Game game) throws IOException
  {
    move(game, 1);
  }

  /** Shows or hides a game in the game selector. Listeners are told the game changed. */
  public void setDisplayed(Game game, boolean displayed) throws IOException
  {
    int index = indexOf(game);
    boolean previous = game.isDisplayed();
    game.setDisplayed(displayed);
    games.set(index, game); // the same instance, so listeners hear about the change
    saveOrUndo(() ->
    {
      game.setDisplayed(previous);
      games.set(index, game);
    });
  }

  private void move(Game game, int offset) throws IOException
  {
    int from = indexOf(game);
    int to = from + offset;
    if (to < 0 || to >= games.size())
    {
      return;
    }

    List<Game> original = new ArrayList<>(games);
    List<Game> reordered = new ArrayList<>(games);
    reordered.set(from, games.get(to));
    reordered.set(to, game);
    games.setAll(reordered);
    saveOrUndo(() -> games.setAll(original));
  }

  private void saveOrUndo(Runnable undo) throws IOException
  {
    try
    {
      save();
    }
    catch (IOException | RuntimeException e)
    {
      undo.run();
      throw e;
    }
  }

  private void requireUsableTitle(Game game, Game ignoring)
  {
    String title = game.getGameTitle();
    if (title == null || title.isBlank())
    {
      throw new IllegalArgumentException("A game needs a title");
    }
    if (isTitleTaken(title, ignoring))
    {
      throw new IllegalArgumentException("A game titled '" + title + "' already exists");
    }
  }

  /** Finds the exact instance in the list, since an edited copy may have a new title. */
  private int indexOf(Game game)
  {
    for (int i = 0; i < games.size(); i++)
    {
      if (games.get(i) == game)
      {
        return i;
      }
    }
    int byTitle = games.indexOf(game);
    if (byTitle < 0)
    {
      throw new IllegalArgumentException("That game is not in the list: " + game);
    }
    return byTitle;
  }

  private static boolean sameTitle(String a, String b)
  {
    return a != null && b != null && a.equalsIgnoreCase(b);
  }
}
