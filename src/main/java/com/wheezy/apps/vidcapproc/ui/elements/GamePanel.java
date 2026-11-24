package com.wheezy.apps.vidcapproc.ui.elements;

import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.ui.GameIcons;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * One row of the game manager: the game's icon, title and filename format, whether it
 * is displayed in the game selector, and buttons to reorder, edit and delete it.
 * Clicking anywhere else on the row shows or hides the game.
 * <p>
 * The row only shows a game and reports what the user asked for through
 * {@link Actions}; it never changes the game itself.
 */
public class GamePanel extends HBox
{
  /** What the user can ask for from a row. */
  public interface Actions
  {
    void toggleDisplayed(Game game);

    void edit(Game game);

    void delete(Game game);

    void moveUp(Game game);

    void moveDown(Game game);
  }

  private static final String DISPLAYED_COLOR = "#2e7d32";
  private static final String HIDDEN_COLOR = "#c62828";
  private static final double HIDDEN_OPACITY = 0.55;

  private final Game game;
  private final Button moveUpButton = new Button("▲");
  private final Button moveDownButton = new Button("▼");
  private final Button editButton = new Button("Edit...");
  private final Button deleteButton = new Button("Delete");

  /**
   * @param isFirst whether the game is first in the list, so it cannot move up
   * @param isLast  whether the game is last in the list, so it cannot move down
   */
  public GamePanel(Game game, boolean isFirst, boolean isLast, Actions actions)
  {
    this.game = game;

    setSpacing(12);
    setPadding(new Insets(6));
    setAlignment(Pos.CENTER_LEFT);
    setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-radius: 3;"
        + " -fx-background-radius: 3;");
    setCursor(Cursor.HAND);
    Tooltip.install(this, new Tooltip("Click to show or hide this game in the game selector"));

    ImageView iconView = new ImageView(GameIcons.load(game));
    iconView.setFitWidth(GameIcons.ICON_SIZE);
    iconView.setFitHeight(GameIcons.ICON_SIZE);
    iconView.setPreserveRatio(true);
    StackPane iconBox = new StackPane(iconView);
    iconBox.setMinSize(GameIcons.ICON_SIZE + 6, GameIcons.ICON_SIZE + 6);
    iconBox.setPrefSize(GameIcons.ICON_SIZE + 6, GameIcons.ICON_SIZE + 6);

    Label titleLabel = new Label(game.getGameTitle());
    titleLabel.setFont(Font.font(null, FontWeight.BOLD, 15));
    titleLabel.setTooltip(new Tooltip("Game title"));

    Label formatLabel = new Label(filenameFormat(game.getFilenameLabel()));
    formatLabel.setStyle("-fx-text-fill: #555555;");
    formatLabel.setTooltip(new Tooltip("Filename label format"));

    Label statusLabel = new Label(game.isDisplayed() ? "Displayed" : "Not displayed");
    statusLabel.setStyle("-fx-text-fill: " + (game.isDisplayed() ? DISPLAYED_COLOR : HIDDEN_COLOR)
        + "; -fx-font-size: 11;");

    VBox text = new VBox(2, titleLabel, formatLabel, statusLabel);
    text.setAlignment(Pos.CENTER_LEFT);

    // A hidden game is dimmed, but its buttons stay fully readable
    HBox shown = new HBox(12, iconBox, text);
    shown.setAlignment(Pos.CENTER_LEFT);
    shown.setOpacity(game.isDisplayed() ? 1.0 : HIDDEN_OPACITY);
    HBox.setHgrow(shown, Priority.ALWAYS);
    shown.setMaxWidth(Double.MAX_VALUE);

    moveUpButton.setTooltip(new Tooltip("Move up"));
    moveUpButton.setDisable(isFirst);
    moveUpButton.setOnAction(event -> actions.moveUp(game));
    moveDownButton.setTooltip(new Tooltip("Move down"));
    moveDownButton.setDisable(isLast);
    moveDownButton.setOnAction(event -> actions.moveDown(game));
    editButton.setOnAction(event -> actions.edit(game));
    deleteButton.setOnAction(event -> actions.delete(game));

    HBox buttons = new HBox(4, moveUpButton, moveDownButton, editButton, deleteButton);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    getChildren().addAll(shown, buttons);

    setOnMouseClicked(event ->
    {
      // A click on one of the buttons bubbles up to here too, but it is not a request to toggle
      if (event.getButton() == MouseButton.PRIMARY && !isOnButton(event.getTarget()))
      {
        actions.toggleDisplayed(game);
      }
    });
  }

  public Game getGame()
  {
    return game;
  }

  /** What saved files will be named: {@code BF3_<filename>}, or just {@code <filename>} with no label. */
  static String filenameFormat(String filenameLabel)
  {
    return filenameLabel == null || filenameLabel.isBlank() ? "<filename>" : filenameLabel.strip() + "_<filename>";
  }

  private static boolean isOnButton(Object target)
  {
    for (Node node = target instanceof Node ? (Node) target : null; node != null; node = node.getParent())
    {
      if (node instanceof ButtonBase)
      {
        return true;
      }
    }
    return false;
  }
}
