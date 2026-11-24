package com.wheezy.apps.vidcapproc.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Predicate;

import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.data.GameCatalog;
import com.wheezy.apps.vidcapproc.data.GameSelection;
import com.wheezy.apps.vidcapproc.ui.ButtonFlash;
import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;
import com.wheezy.apps.vidcapproc.ui.elements.GameSelectorButton;
import com.wheezy.apps.vidcapproc.ui.elements.GameSelectorPanel;
import com.wheezy.apps.vidcapproc.utils.FileCleanupTask;
import com.wheezy.apps.vidcapproc.utils.FileMoveTask;
import com.wheezy.apps.vidcapproc.utils.SaveActionType;
import com.wheezy.apps.vidcapproc.utils.VideoCaptureProcessorUtils;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;
import com.wheezy.components.FXMLController;
import com.wheezy.utils.ui.AlertDialog;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class VideoCaptureProcessorController extends FXMLController
{
  private static Logger logger = Logger.getLogger(VideoCaptureProcessorController.class.getName());

  private VideoCaptureProcessorProperties propertiesInstance;
  
  @FXML
  private GameSelectorButton gameButton;

  @FXML
  private GameSelectorPanel gameSelectorPanel;

  @FXML
  private Button keepersButton;

  @FXML
  private Button clipsButton;

  @FXML
  private Button markClipButton;

  @FXML
  private TextField clipLabelField;

  @FXML
  private TextField filenameLabelField;

  @FXML
  private Button cleanupButton;

  @FXML
  private Label statusLabel;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    super.initialize(location, resources);

    keepersButton.setTooltip(new Tooltip("Move latest video to keepers folder (F1)"));
    clipsButton.setTooltip(new Tooltip("Move latest video to clips folder (F2)"));
    markClipButton.setTooltip(new Tooltip("Mark the timestamp for a clip (F3)"));
    clipLabelField.setTooltip(new Tooltip("Add a label to the clip mark"));
    filenameLabelField.setTooltip(new Tooltip(
        "Text added to the saved file names, before the original name (used by Keepers and Clips)"));
    cleanupButton.setTooltip(new Tooltip("Delete all files from the capture folder"));

    // The scene doesn't exist until the stage wraps the loaded FXML, so wait for it
    markClipButton.sceneProperty().addListener((observable, oldScene, scene) ->
    {
      if (scene != null)
      {
        installHotkeys(scene);
      }
    });

    try
    {
      propertiesInstance = VideoCaptureProcessorProperties.getInstance();
      propertiesInstance.loadProperties();
    }
    catch (IOException ioe)
    {
      AlertDialog.displayErrorDialog("Error Reading Properties",
          "An error was encountered while reading from the properties file.", ioe.getMessage());

      logger.log(Level.SEVERE, "Property Read Error", ioe);
    }

    // After the properties are loaded: the remembered game and selector state come from them
    setUpGameSelector();
  }

  private void setUpGameSelector()
  {
    GameSelection selection = GameSelection.getInstance();
    gameSelectorPanel.bind(GameCatalog.getInstance(), selection);
    gameSelectorPanel.setOnManageGames(this::openGameManager);

    // The game button always shows the selected game, so it is useful even with the selector collapsed
    showSelectedGame(selection.getSelectedGame());
    selection.selectedGameProperty().addListener((observable, oldGame, newGame) -> showSelectedGame(newGame));

    // Start as the user left it. This comes before the listener below, so it neither saves nor resizes.
    gameSelectorPanel.setExpanded(Boolean
        .parseBoolean(propertiesInstance.getProperty(VideoCaptureProcessorProperty.GAME_SELECTOR_VISIBLE_PROPERTY)));
    gameSelectorPanel.expandedProperty()
        .addListener((observable, wasExpanded, isExpanded) -> gameSelectorToggled(isExpanded));
  }

  private void showSelectedGame(Game game)
  {
    gameButton.setGame(game == null ? new Game() : game);
  }

  /** Remembers whether the selector is open, and grows or shrinks the window to match. */
  private void gameSelectorToggled(boolean isExpanded)
  {
    propertiesInstance.setProperty(VideoCaptureProcessorProperty.GAME_SELECTOR_VISIBLE_PROPERTY,
        Boolean.toString(isExpanded));
    try
    {
      propertiesInstance.storeProperties();
    }
    catch (IOException e)
    {
      // Not worth interrupting the user over
      logger.log(Level.WARNING, "Property Store Error", e);
    }

    Stage stage = getStage();
    if (stage != null && stage.isShowing())
    {
      stage.sizeToScene();
      keepOnScreen(stage);
    }
  }

  /** A window that grew to the right near the edge of the screen is moved left so it is not cut off. */
  private static void keepOnScreen(Stage stage)
  {
    Rectangle2D bounds = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1).stream().findFirst()
        .orElse(Screen.getPrimary()).getVisualBounds();
    double overflow = stage.getX() + stage.getWidth() - bounds.getMaxX();
    if (overflow > 0)
    {
      stage.setX(Math.max(bounds.getMinX(), stage.getX() - overflow));
    }
  }

  /**
   * F1, F2 and F3 press the Keepers, Clips and Mark Clip buttons, so a hotkey does
   * exactly what a click does, and does nothing while a button is disabled (for
   * example during a file move).
   */
  private void installHotkeys(Scene scene)
  {
    scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), keepersButton::fire);
    scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), clipsButton::fire);
    scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F3), markClipButton::fire);
  }

  @FXML
  void saveKeeper(ActionEvent event)
  {
    ButtonFlash.flash(keepersButton);
    startSave(SaveActionType.SAVE_KEEPER);
  }

  @FXML
  void saveClip(ActionEvent event)
  {
    ButtonFlash.flash(clipsButton);
    startSave(SaveActionType.SAVE_CLIP);
  }

  @FXML
  void markClip(ActionEvent event)
  {
    ButtonFlash.flash(markClipButton);
    String clipTime = VideoCaptureProcessorUtils.markClip(clipLabelField.getText());
    if (clipTime != null)
    {
      statusLabel.setText("Clip marked: " + clipTime);
      clipLabelField.clear();
    }
  }

  @FXML
  void cleanupFiles(ActionEvent event)
  {
    ButtonFlash.flash(cleanupButton);
    FileCleanupTask task = VideoCaptureProcessorUtils.cleanupCaptureDirectory();
    if (task != null)
    {
      // Marking a clip while the clip marks are being deleted would be pointless, so that is blocked too
      trackTask(task, List.of(keepersButton, clipsButton, markClipButton, cleanupButton),
          result -> result.succeeded(), "Cleanup complete", "Cleanup failed", null);
    }
  }

  private void startSave(SaveActionType action)
  {
    FileMoveTask task = VideoCaptureProcessorUtils.saveNewestFile(action, filenameLabelField.getText());
    if (task != null)
    {
      // Block repeat clicks, and a cleanup, until the move finishes. The label was used up by this save.
      trackTask(task, List.of(keepersButton, clipsButton, cleanupButton), result -> result.succeeded(),
          "Move complete", "Move failed", filenameLabelField::clear);
    }
  }

  /**
   * Keeps the given controls disabled while a background task runs, shows its
   * progress messages in the status line, and reports how it ended.
   *
   * @param succeeded decides, from the task's result, whether it counts as a success
   * @param onSuccess runs after a successful task, or null
   */
  private <R> void trackTask(Task<R> task, List<? extends Node> disabledWhileRunning, Predicate<R> succeeded,
      String doneText, String failedText, Runnable onSuccess)
  {
    disabledWhileRunning.forEach(node -> node.setDisable(true));
    task.messageProperty().addListener((observable, oldMessage, newMessage) -> statusLabel.setText(newMessage));
    task.stateProperty().addListener((observable, oldState, newState) ->
    {
      if (newState == Worker.State.SUCCEEDED || newState == Worker.State.FAILED || newState == Worker.State.CANCELLED)
      {
        boolean ok = newState == Worker.State.SUCCEEDED && succeeded.test(task.getValue());
        statusLabel.setText(ok ? doneText : failedText);
        disabledWhileRunning.forEach(node -> node.setDisable(false));
        if (ok && onSuccess != null)
        {
          onSuccess.run();
        }
      }
    });
  }

  @FXML
  void openSettingsDialog(ActionEvent event)
  {
    VideoCaptureProcessorStage stage = new VideoCaptureProcessorStage(
        VideoCaptureProcessorResources.SETTINGS_DIALOG_FXML, "Settings", true);
    stage.initOwner(getStage()); // keeps the dialog above this window even when it is always on top
    stage.showAndWait();

    // The always-on-top setting may have changed
    getStage().setAlwaysOnTop(propertiesInstance.isAlwaysOnTop());
  }

  @FXML
  void toggleGameSelector(ActionEvent event)
  {
    gameSelectorPanel.toggle();
  }

  private void openGameManager()
  {
    VideoCaptureProcessorStage stage = new VideoCaptureProcessorStage(
        VideoCaptureProcessorResources.GAME_MANAGER_FXML, "Manage Games", true);
    stage.initOwner(getStage());
    stage.showAndWait();
  }
}
