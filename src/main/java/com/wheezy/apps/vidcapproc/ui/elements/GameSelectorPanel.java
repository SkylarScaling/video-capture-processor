package com.wheezy.apps.vidcapproc.ui.elements;

import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.data.GameCatalog;
import com.wheezy.apps.vidcapproc.data.GameSelection;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The game selector: a panel on the right side of the window that is collapsed to a
 * narrow handle until the user clicks it. Expanded, it shows an icon for each displayed
 * game (and one for no game) and the user clicks one to select it.
 * <p>
 * The panel does not resize the window itself. It reports being expanded or collapsed
 * through {@link #expandedProperty()}, and the window follows.
 */
public class GameSelectorPanel extends HBox
{
  /** Wide enough for four game buttons in a row, beside the scroll bar. */
  public static final double CONTENT_WIDTH = 430;
  private static final double HANDLE_WIDTH = 22;

  private final BooleanProperty expanded = new SimpleBooleanProperty(false);
  private final Button handle = new Button();
  private final VBox content = new VBox(8);
  private final Label gameNameLabel = new Label();
  private final FlowPane gamesPane = new FlowPane(6, 6);
  private final Button manageButton = new Button("Manage Games...");

  private final ListChangeListener<Game> catalogListener = change -> rebuild();
  private final ChangeListener<Game> selectionListener = (observable, oldGame, newGame) -> refreshSelection();

  private GameCatalog catalog;
  private GameSelection selection;
  private GameSelectorButton noneButton;
  private Runnable onManageGames = () -> {};

  public GameSelectorPanel()
  {
    handle.setMinWidth(HANDLE_WIDTH);
    handle.setPrefWidth(HANDLE_WIDTH);
    handle.setMaxWidth(HANDLE_WIDTH);
    handle.setMaxHeight(Double.MAX_VALUE);
    handle.setPadding(Insets.EMPTY);
    handle.setFont(Font.font(null, FontWeight.BOLD, 16)); // the arrow is the only clue that this is clickable
    handle.setStyle("-fx-background-color: #ececec; -fx-background-radius: 0;");
    handle.setOnAction(event -> toggle());

    gameNameLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

    gamesPane.setPadding(new Insets(8));
    ScrollPane scroll = new ScrollPane(gamesPane);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setMinHeight(0);
    VBox.setVgrow(scroll, Priority.ALWAYS);

    manageButton.setOnAction(event -> onManageGames.run());

    content.setPadding(new Insets(10));
    content.setMinWidth(CONTENT_WIDTH);
    content.setPrefWidth(CONTENT_WIDTH);
    content.setMaxWidth(CONTENT_WIDTH);
    content.getChildren().addAll(gameNameLabel, scroll, manageButton);

    // The panel takes the height of the window instead of asking for its own
    setMinHeight(0);
    setPrefHeight(0);
    content.setMinHeight(0);
    content.setPrefHeight(0);

    setStyle("-fx-background-color: white;");
    getChildren().addAll(handle, content);

    expanded.addListener((observable, wasExpanded, isExpanded) -> showExpanded(isExpanded));
    showExpanded(false);
    refreshSelection();
  }

  /**
   * Shows the games of the catalog and the selected game. Call again to switch to other
   * ones, which tests do to avoid touching the real games.
   */
  public void bind(GameCatalog newCatalog, GameSelection newSelection)
  {
    if (catalog != null)
    {
      catalog.getGames().removeListener(catalogListener);
      selection.selectedGameProperty().removeListener(selectionListener);
    }
    catalog = newCatalog;
    selection = newSelection;
    catalog.getGames().addListener(catalogListener);
    selection.selectedGameProperty().addListener(selectionListener);
    rebuild();
  }

  /** @param action what the "Manage Games..." button does */
  public void setOnManageGames(Runnable action)
  {
    onManageGames = action == null ? () -> {} : action;
  }

  public BooleanProperty expandedProperty()
  {
    return expanded;
  }

  public boolean isExpanded()
  {
    return expanded.get();
  }

  public void setExpanded(boolean isExpanded)
  {
    expanded.set(isExpanded);
  }

  public void toggle()
  {
    setExpanded(!isExpanded());
  }

  /** The icon buttons, starting with the one for no game. Tests use this: nodes inside a scroll pane cannot be looked up. */
  java.util.List<GameSelectorButton> gameButtons()
  {
    java.util.List<GameSelectorButton> buttons = new java.util.ArrayList<>();
    for (Node node : gamesPane.getChildren())
    {
      buttons.add((GameSelectorButton) node);
    }
    return buttons;
  }

  private void showExpanded(boolean isExpanded)
  {
    // A hidden node that is still managed would keep taking up room, so it is not managed either
    content.setVisible(isExpanded);
    content.setManaged(isExpanded);
    handle.setText(isExpanded ? "◂" : "▸");
    handle.setTooltip(new Tooltip(isExpanded ? "Hide the game selector" : "Show the game selector"));
  }

  /** Rebuilds the icons: one for no game, then one for each displayed game. */
  private void rebuild()
  {
    gamesPane.getChildren().clear();

    Game none = new Game(); // a stand-in with no title, which shows the default icon
    noneButton = new GameSelectorButton(none);
    noneButton.setOnAction(event -> selection.select(null));
    gamesPane.getChildren().add(noneButton);

    for (Game game : catalog.getGames())
    {
      if (game.isDisplayed())
      {
        GameSelectorButton button = new GameSelectorButton(game);
        button.setOnAction(event -> selection.select(game));
        gamesPane.getChildren().add(button);
      }
    }
    refreshSelection();
  }

  /** Highlights the selected game's icon and shows its name. */
  private void refreshSelection()
  {
    Game current = selection == null ? null : selection.getSelectedGame();
    gameNameLabel.setText("Game: " + (current == null ? "None" : current.getGameTitle()));

    for (Node node : gamesPane.getChildren())
    {
      GameSelectorButton button = (GameSelectorButton) node;
      boolean isSelected = button == noneButton ? current == null : button.getGame() == current;
      if (isSelected)
      {
        button.selected();
      }
      else
      {
        button.unselected();
      }
    }
  }
}
