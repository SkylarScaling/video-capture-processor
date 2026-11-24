package com.wheezy.apps.vidcapproc.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.data.GameCatalog;
import com.wheezy.apps.vidcapproc.data.GameValidation;
import com.wheezy.apps.vidcapproc.ui.GameIcons;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;
import com.wheezy.components.FXMLController;
import com.wheezy.utils.ui.AlertDialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/** Adds a game, or edits an existing one: title, filename label and icon. */
public class GameEditorController extends FXMLController
{
  private static final Logger logger = Logger.getLogger(GameEditorController.class.getName());

  @FXML
  private TextField gameTitleField;
  @FXML
  private TextField filenameLabelField;
  @FXML
  private Label filenameExampleLabel;
  @FXML
  private Label errorLabel;
  @FXML
  private ImageView iconImageView;
  @FXML
  private Button saveButton;

  private GameCatalog catalog;
  private File imagesDirectory = GameIcons.defaultImagesDirectory();
  private Game editing;
  private File chosenImage;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    super.initialize(location, resources);
    catalog = GameCatalog.getInstance();

    gameTitleField.textProperty().addListener((observable, oldText, newText) -> revalidate());
    filenameLabelField.textProperty().addListener((observable, oldText, newText) -> revalidate());
    iconImageView.setImage(GameIcons.defaultIcon());
    revalidate();
  }

  /** Uses the given catalog, which tests use to avoid touching the real games file. */
  void useCatalog(GameCatalog newCatalog)
  {
    catalog = newCatalog;
    revalidate();
  }

  /** Where new icons are stored. Tests use this to avoid writing into the working directory. */
  void useImagesDirectory(File directory)
  {
    imagesDirectory = directory;
  }

  /** @param game the game to edit, or null to add a new one */
  public void setGame(Game game)
  {
    editing = game;
    chosenImage = null;
    if (game == null)
    {
      gameTitleField.clear();
      filenameLabelField.clear();
      iconImageView.setImage(GameIcons.defaultIcon());
    }
    else
    {
      gameTitleField.setText(game.getGameTitle());
      filenameLabelField.setText(game.getFilenameLabel() == null ? "" : game.getFilenameLabel());
      iconImageView.setImage(GameIcons.load(game));
    }
    revalidate();
  }

  @FXML
  void browseForIcon(ActionEvent event)
  {
    VideoCaptureProcessorProperties props = VideoCaptureProcessorProperties.getInstance();
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Select Image for Game Icon");
    chooser.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
    File start = VideoCaptureProcessorSettingsController
        .resolveInitialDirectory(null, props.getProperty(VideoCaptureProcessorProperty.FILE_CHOOSER_LOCATION_PROPERTY));
    if (start != null)
    {
      chooser.setInitialDirectory(start);
    }

    File selected = chooser.showOpenDialog(getStage());
    if (selected != null && useChosenImage(selected) && selected.getParentFile() != null)
    {
      props.setProperty(VideoCaptureProcessorProperty.FILE_CHOOSER_LOCATION_PROPERTY,
          selected.getParentFile().getAbsolutePath());
      try
      {
        props.storeProperties();
      }
      catch (IOException e)
      {
        logger.log(Level.WARNING, "Property Store Error", e);
      }
    }
  }

  /**
   * Uses a picked image as the game's new icon, showing it as a preview.
   *
   * @return false if the file could not be read as an image, after telling the user
   */
  boolean useChosenImage(File image)
  {
    try
    {
      Image preview = GameIcons.loadScaled(image);
      chosenImage = image;
      iconImageView.setImage(preview);
      return true;
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, "Image Load Error", e);
      AlertDialog.displayErrorDialog(owner(), "Error Loading Image",
          "That file could not be loaded as an image.", e.getMessage());
      return false;
    }
  }

  @FXML
  void save(ActionEvent event)
  {
    if (firstError().isPresent())
    {
      return;
    }

    String title = gameTitleField.getText().strip();
    Game game = new Game();
    game.setGameTitle(title);
    game.setFilenameLabel(filenameLabelField.getText().strip());
    game.setDisplayed(editing == null || editing.isDisplayed());
    game.setIconImageFilepath(editing == null ? null : editing.getIconImageFilepath());

    if (chosenImage != null)
    {
      try
      {
        List<Game> others = catalog.getGames().stream().filter(other -> other != editing).toList();
        game.setIconImageFilepath(GameIcons.saveIcon(chosenImage, title, others, imagesDirectory));
      }
      catch (IOException e)
      {
        // The game is still usable: it just points at the original image instead of a stored copy
        logger.log(Level.WARNING, "Image Save Error", e);
        game.setIconImageFilepath(chosenImage.getAbsolutePath());
      }
    }

    try
    {
      if (editing == null)
      {
        catalog.add(game);
      }
      else
      {
        catalog.replace(editing, game);
      }
      closeEditor();
    }
    catch (IOException e)
    {
      logger.log(Level.SEVERE, "Game Save Error", e);
      AlertDialog.displayErrorDialog(owner(), "Error Saving Game",
          "The game could not be saved, so nothing was changed.", e.getMessage());
    }
    catch (IllegalArgumentException e)
    {
      AlertDialog.displayErrorDialog(owner(), "Error Saving Game", "The game could not be saved.", e.getMessage());
    }
  }

  /** The first thing wrong with what is typed, ignoring the title being empty (it just keeps Save disabled). */
  private Optional<String> firstError()
  {
    String title = gameTitleField.getText();
    if (title == null || title.isBlank())
    {
      return Optional.of("");
    }
    Optional<String> error = GameValidation.titleError(title, editing, catalog);
    return error.isPresent() ? error : GameValidation.filenameLabelError(filenameLabelField.getText());
  }

  private void revalidate()
  {
    if (gameTitleField == null || saveButton == null)
    {
      return; // still loading
    }
    Optional<String> error = firstError();
    errorLabel.setText(error.orElse(""));
    saveButton.setDisable(error.isPresent());

    String label = filenameLabelField.getText();
    filenameExampleLabel.setText("Saved files will be named "
        + (label == null || label.isBlank() ? "<filename>" : label.strip() + "_<filename>"));
  }

  private void closeEditor()
  {
    if (getStage() != null)
    {
      getStage().close();
    }
  }

  private Window owner()
  {
    return getStage();
  }
}
