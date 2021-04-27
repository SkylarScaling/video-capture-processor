package com.wheezy.apps.vidcapproc.ui.elements;

import com.jfoenix.controls.JFXButton;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class GameSelectorButton extends JFXButton
{
  private static final String SELECTED_BUTTON_STYLE = "-fx-border-width: 4; -fx-border-color: #FF0000FF;";
  private static final String UNSELECTED_BUTTON_STYLE = "-fx-border-width: 0;";
  private static final double GAME_BUTTON_WIDTH = 74.0;
  private static final double GAME_BUTTON_HEIGHT = 74.0;

  private Game game;
  private ContextMenu rightClickMenu = new ContextMenu();

  public GameSelectorButton()
  {
    this(new Game());
  }

  public GameSelectorButton(Game game)
  {
    super();

    this.game = game;

    MenuItem editGameMenuItem = new MenuItem("Edit...");
    editGameMenuItem.setOnAction((event) -> {
      // GameEditor gameEditor =
      // new GameEditor(GameSelectorButton.this.game);
      // gameEditor.setLocationRelativeTo(GameSelectorButton.this);
      // gameEditor.setVisible(true);
    });
    rightClickMenu.getItems().add(editGameMenuItem);

    ImageView imageView = new ImageView();
    imageView.setImage(new Image(getClass().getClassLoader()
        .getResourceAsStream(game.getIconImageFilepath() == null ? VideoCaptureProcessorResources.DEFAULT_GAME_ICON_PATH
            : game.getIconImageFilepath())));
    imageView.setFitHeight(64);
    imageView.setFitWidth(64);
    DropShadow dropShadow = new DropShadow();
    dropShadow.setColor(new Color(00, 00, 00, 0.5));
    imageView.setEffect(new DropShadow());
    this.setGraphic(imageView);

    this.setTooltip(new Tooltip(game.getGameTitle()));
    this.setPrefSize(GAME_BUTTON_WIDTH, GAME_BUTTON_HEIGHT);
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

  public void updateFrom(GameSelectorButton fromButton)
  {
    this.game = fromButton.game;
    this.setGraphic(fromButton.getGraphic());
    this.rightClickMenu = fromButton.rightClickMenu;
  }
}
