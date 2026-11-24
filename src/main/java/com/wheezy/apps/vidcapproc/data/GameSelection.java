package com.wheezy.apps.vidcapproc.data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;

/**
 * The game the user is playing now, or none. Saved files get this game's filename label.
 * <p>
 * The selection is remembered between runs. It follows the game when it is edited in
 * the game manager (even if it is renamed), and is cleared if the game is deleted.
 */
public class GameSelection
{
  /** What is stored for "no game", as the Swing app did. */
  public static final String NONE = "None";

  private static final Logger logger = Logger.getLogger(GameSelection.class.getName());
  private static GameSelection instance;

  private final GameCatalog catalog;
  private final Consumer<String> remember;
  private final ObjectProperty<Game> selected = new SimpleObjectProperty<>();

  /**
   * @param remember told the title of the selected game (or {@link #NONE}) whenever the selection changes
   */
  public GameSelection(GameCatalog catalog, Consumer<String> remember)
  {
    this.catalog = catalog;
    this.remember = remember;
    catalog.getGames().addListener((ListChangeListener<Game>) this::catalogChanged);
  }

  /**
   * The app's selection, remembered in the properties file. The properties must be loaded
   * before this is first called, so that the remembered game can be restored.
   */
  public static synchronized GameSelection getInstance()
  {
    if (instance == null)
    {
      VideoCaptureProcessorProperties props = VideoCaptureProcessorProperties.getInstance();
      instance = new GameSelection(GameCatalog.getInstance(), title ->
      {
        props.setProperty(VideoCaptureProcessorProperty.SELECTED_GAME_PROPERTY, title);
        try
        {
          props.storeProperties();
        }
        catch (IOException e)
        {
          // Not worth interrupting the user over
          logger.log(Level.WARNING, "Property Store Error", e);
        }
      });
      instance.restore(props.getProperty(VideoCaptureProcessorProperty.SELECTED_GAME_PROPERTY));
    }
    return instance;
  }

  /** Replaces the app's selection. Tests use this so that they never change the real settings. */
  public static synchronized void setInstance(GameSelection selection)
  {
    instance = selection;
  }

  public ReadOnlyObjectProperty<Game> selectedGameProperty()
  {
    return selected;
  }

  /** @return the selected game, or null for none */
  public Game getSelectedGame()
  {
    return selected.get();
  }

  /**
   * @param game a game in the catalog, or null for none
   * @throws IllegalArgumentException if the game is not in the catalog
   */
  public void select(Game game)
  {
    if (game != null && !contains(catalog.getGames(), game))
    {
      throw new IllegalArgumentException("That game is not in the list: " + game);
    }
    set(game, true);
  }

  /** Selects the game with the given title, as stored by a previous run. Anything else means none. */
  public void restore(String title)
  {
    set(title == null ? null : catalog.find(title).orElse(null), false);
  }

  /** @return the selected game's filename label, if there is a game and it has a label */
  public Optional<String> gameLabel()
  {
    return Optional.ofNullable(selected.get()).map(Game::getFilenameLabel).filter(label -> !label.isBlank());
  }

  private void set(Game game, boolean saveChoice)
  {
    selected.set(game);
    if (saveChoice)
    {
      remember.accept(game == null ? NONE : game.getGameTitle());
    }
  }

  /** Keeps the selection pointing at the right game after the list changes. */
  private void catalogChanged(ListChangeListener.Change<? extends Game> change)
  {
    Game current = selected.get();
    if (current == null || contains(catalog.getGames(), current))
    {
      return;
    }

    // The game left the list. If it was replaced by an edited copy, follow it; if it was deleted, clear.
    Game successor = null;
    while (change.next())
    {
      if (change.wasReplaced() && change.getAddedSize() == change.getRemovedSize())
      {
        for (int i = 0; i < change.getRemovedSize(); i++)
        {
          if (change.getRemoved().get(i) == current)
          {
            successor = change.getAddedSubList().get(i);
          }
        }
      }
    }
    set(successor, true);
  }

  /** The same instance, not an equal one: an edited copy may even have a different title. */
  private static boolean contains(List<Game> games, Game game)
  {
    for (Game candidate : games)
    {
      if (candidate == game)
      {
        return true;
      }
    }
    return false;
  }
}
