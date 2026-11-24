package com.wheezy.apps.vidcapproc.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.data.GameCatalog;
import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;
import com.wheezy.apps.vidcapproc.ui.elements.GamePanel;
import com.wheezy.components.FXMLController;
import com.wheezy.utils.ui.AlertDialog;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * The game manager: lists every game and lets the user add, edit, delete, reorder
 * and show or hide them. Each change is saved straight away by the {@link GameCatalog}.
 */
public class GameManagerController extends FXMLController
{
  private static final Logger logger = Logger.getLogger(GameManagerController.class.getName());
  private static final ButtonType DELETE_BUTTON = new ButtonType("Delete", javafx.scene.control.ButtonBar.ButtonData.OTHER);

  @FXML
  private ScrollPane gameScrollPane;
  @FXML
  private VBox gameListBox;
  @FXML
  private Label summaryLabel;

  private GameCatalog catalog;

  // Held strongly here, and registered weakly, so a closed manager is not kept alive by the catalog
  private final ListChangeListener<Game> catalogListener = change -> refresh();

  /** An action that changes the catalog. */
  private interface CatalogChange
  {
    void run() throws IOException;
  }

  private final GamePanel.Actions actions = new GamePanel.Actions()
  {
    @Override
    public void toggleDisplayed(Game game)
    {
      attempt(() -> catalog.setDisplayed(game, !game.isDisplayed()));
    }

    @Override
    public void edit(Game game)
    {
      openEditor(game);
    }

    @Override
    public void delete(Game game)
    {
      Optional<ButtonType> answer = AlertDialog.displayChoiceDialog(owner(), AlertType.WARNING, "Delete Game",
          "Delete \"" + game.getGameTitle() + "\"?",
          "The game is removed from your list. Its icon file is not deleted.", DELETE_BUTTON, ButtonType.CANCEL);
      if (answer.isPresent() && answer.get() == DELETE_BUTTON)
      {
        attempt(() -> catalog.remove(game));
      }
    }

    @Override
    public void moveUp(Game game)
    {
      attempt(() -> catalog.moveUp(game));
    }

    @Override
    public void moveDown(Game game)
    {
      attempt(() -> catalog.moveDown(game));
    }
  };

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    super.initialize(location, resources);
    useCatalog(GameCatalog.getInstance());

    // Tell the user about an unreadable games file once the window is up, not while it is loading
    Platform.runLater(() -> catalog.takeLoadFailure().ifPresent(message -> AlertDialog.displayErrorDialog(owner(),
        "Error Loading Games", "The saved games could not be read, so the list starts empty.", message)));
  }

  /** Shows the games of the given catalog, which tests use to avoid touching the real games file. */
  void useCatalog(GameCatalog newCatalog)
  {
    if (catalog != null)
    {
      catalog.getGames().removeListener(catalogListener);
    }
    catalog = newCatalog;
    catalog.getGames().addListener(new WeakListChangeListener<>(catalogListener));
    refresh();
  }

  @FXML
  void addGame(ActionEvent event)
  {
    openEditor(null);
  }

  /** Rebuilds the rows from the catalog, keeping the scroll position. */
  private void refresh()
  {
    double scrollPosition = gameScrollPane.getVvalue();

    List<Game> games = catalog.getGames();
    gameListBox.getChildren().clear();
    for (int i = 0; i < games.size(); i++)
    {
      gameListBox.getChildren().add(new GamePanel(games.get(i), i == 0, i == games.size() - 1, actions));
    }

    long displayed = games.stream().filter(Game::isDisplayed).count();
    summaryLabel.setText(games.size() + (games.size() == 1 ? " game, " : " games, ") + displayed + " displayed");

    // The new rows are not laid out yet, so put the scroll position back afterwards
    Platform.runLater(() -> gameScrollPane.setVvalue(scrollPosition));
  }

  private void openEditor(Game game)
  {
    VideoCaptureProcessorStage stage = new VideoCaptureProcessorStage(
        VideoCaptureProcessorResources.GAME_EDITOR_FXML, game == null ? "Add Game" : "Edit Game", true);
    if (getStage() != null)
    {
      stage.initOwner(getStage());
    }
    GameEditorController editor = (GameEditorController) stage.getController();
    editor.useCatalog(catalog);
    editor.setGame(game);
    stage.showAndWait();
  }

  private void attempt(CatalogChange change)
  {
    try
    {
      change.run();
    }
    catch (IOException e)
    {
      logger.log(Level.SEVERE, "Game Save Error", e);
      AlertDialog.displayErrorDialog(owner(), "Error Saving Games",
          "Your change could not be saved, so it was undone.", e.getMessage());
    }
  }

  private Window owner()
  {
    return getStage();
  }
}
