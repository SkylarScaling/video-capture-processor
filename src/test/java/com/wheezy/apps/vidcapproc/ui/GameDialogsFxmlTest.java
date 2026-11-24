package com.wheezy.apps.vidcapproc.ui;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.data.GameCatalog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;

public class GameDialogsFxmlTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  private Parent load(String fxml) throws Exception
  {
    GameCatalog.setInstance(new GameCatalog(new File(temp.getRoot(), "SavedGames.xml")));
    return onFxThread(() ->
    {
      try
      {
        return new FXMLLoader(getClass().getClassLoader().getResource(fxml)).load();
      }
      catch (Exception e)
      {
        throw new RuntimeException("Unable to load " + fxml, e);
      }
    });
  }

  @Test
  public void theGameSelectorLoadsAndCanOpenTheManager() throws Exception
  {
    Parent root = load(VideoCaptureProcessorResources.GAME_SELECTOR_DIALOG_FXML);

    Button manage = (Button) root.lookup("#manageGamesButton");
    assertNotNull(manage);
    assertNotNull("Manage Games... does something", manage.getOnAction());
    assertNotNull("Done closes the dialog", ((Button) root.lookup("#doneButton")).getOnAction());
  }

  @Test
  public void theGameManagerLoadsWithItsControls() throws Exception
  {
    Parent root = load(VideoCaptureProcessorResources.GAME_MANAGER_FXML);

    assertNotNull(((ScrollPane) root.lookup("#gameScrollPane")).getContent());

    assertNotNull(((Button) root.lookup("#addGameButton")).getOnAction());
  }

  @Test
  public void theGameEditorLoadsWithItsControls() throws Exception
  {
    Parent root = load(VideoCaptureProcessorResources.GAME_EDITOR_FXML);

    for (String id : new String[] { "gameTitleField", "filenameLabelField", "iconImageView", "browseButton",
        "saveButton", "errorLabel", "filenameExampleLabel" })
    {
      assertNotNull(id, root.lookup("#" + id));
    }
    assertTrue(((Button) root.lookup("#saveButton")).isDefaultButton());
  }
}
