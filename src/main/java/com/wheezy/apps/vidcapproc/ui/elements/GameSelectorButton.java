package com.wheezy.apps.vidcapproc.ui.elements;

import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.ui.GameIcons;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;

/** A button showing a game's icon, with a red border when it is the selected game. */
public class GameSelectorButton extends Button
{
  private static final String SELECTED_BUTTON_STYLE = "-fx-padding: 0; -fx-border-width: 4; -fx-border-color: #FF0000FF;";
  private static final String UNSELECTED_BUTTON_STYLE = "-fx-padding: 0; -fx-border-width: 0;";
  private static final double GAME_BUTTON_WIDTH = 84.0;
  private static final double GAME_BUTTON_HEIGHT = 84.0;

  private Game game;
  private Runnable onEditRequested;

  public GameSelectorButton()
  {
    this(new Game());
  }

  public GameSelectorButton(Game game)
  {
    super();

    this.setPrefSize(GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
    // Fixed size: the selected border and default padding must not make the button grow and wrap the row
    this.setMinSize(GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
    this.setMaxSize(GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
    unselected();
    setGame(game);
  }

  /** Shows a different game: its icon, and its title as the tooltip. */
  public void setGame(Game game)
  {
    this.game = game;

    ImageView imageView = new ImageView(GameIcons.load(game));
    imageView.setFitHeight(GAME_BUTTON_HEIGHT - 10);
    imageView.setFitWidth(GAME_BUTTON_WIDTH - 10);
    imageView.setPreserveRatio(true);
    imageView.setEffect(new DropShadow());
    this.setGraphic(imageView);

    String title = game.getGameTitle();
    this.setTooltip(new Tooltip(title == null || title.isBlank() ? "No game selected" : title));
  }

  /**
   * Adds a right-click menu with "Edit..." that runs the given action, or removes the menu
   * for null. The button for "no game" has nothing to edit, so it gets none.
   */
  public void setOnEditRequested(Runnable action)
  {
    onEditRequested = action;
    if (action == null)
    {
      setContextMenu(null);
      return;
    }
    MenuItem edit = new MenuItem("Edit...");
    edit.setOnAction(event -> onEditRequested.run());
    setContextMenu(new ContextMenu(edit));
  }

  public Game getGame()
  {
    return game;
  }

  public String getGameTitle()
  {
    return game.getGameTitle();
  }

  public void selected()
  {
    setStyle(SELECTED_BUTTON_STYLE);
  }

  public void unselected()
  {
    setStyle(UNSELECTED_BUTTON_STYLE);
  }
}
