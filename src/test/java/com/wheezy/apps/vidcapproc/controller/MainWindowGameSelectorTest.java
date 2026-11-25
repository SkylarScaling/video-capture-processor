package com.wheezy.apps.vidcapproc.controller;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.FxTestSupport.AppData;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.elements.GameSelectorButton;
import com.wheezy.apps.vidcapproc.ui.elements.GameSelectorPanel;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/** How the main window and the collapsible game selector work together. */
public class MainWindowGameSelectorTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  private static Game game(String title, String label)
  {
    Game game = new Game();
    game.setGameTitle(title);
    game.setFilenameLabel(label);
    game.setDisplayed(true);
    return game;
  }

  private Parent loadMainWindow() throws Exception
  {
    return onFxThread(() ->
    {
      try
      {
        return new FXMLLoader(getClass().getClassLoader()
            .getResource(VideoCaptureProcessorResources.VIDEO_CAPTURE_PROCESSOR_FXML)).load();
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    });
  }

  private static GameSelectorPanel panel(Parent root)
  {
    return (GameSelectorPanel) root.lookup("#gameSelectorPanel");
  }

  private static GameSelectorButton gameButton(Parent root)
  {
    return (GameSelectorButton) root.lookup("#gameButton");
  }

  @Test
  public void thePanelSitsToTheRightOfTheMainControls() throws Exception
  {
    FxTestSupport.useTemporaryAppData(temp.getRoot());
    Parent root = loadMainWindow();

    // The menu bar is above the row; the row is the controls, then the selector on the right
    javafx.scene.layout.HBox row = (javafx.scene.layout.HBox) panel(root).getParent();
    assertEquals("controls first, then the selector on the right", 2, row.getChildren().size());
    assertSame(panel(root), row.getChildren().get(1));
  }

  @Test
  public void startsCollapsedWhenNothingWasSaved() throws Exception
  {
    FxTestSupport.useTemporaryAppData(temp.getRoot());
    Parent root = loadMainWindow();

    assertFalse(panel(root).isExpanded());
  }

  @Test
  public void startsExpandedWhenTheUserLeftItOpen() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    data.properties().setProperty(VideoCaptureProcessorProperty.GAME_SELECTOR_VISIBLE_PROPERTY, "true");
    data.properties().storeProperties();

    Parent root = loadMainWindow();

    assertTrue(panel(root).isExpanded());
  }

  @Test
  public void theGameButtonOpensAndClosesTheSelectorAndRemembersIt() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    Parent root = loadMainWindow();

    onFxThread(() -> gameButton(root).fire());
    assertTrue(panel(root).isExpanded());
    assertEquals("true", data.properties().getProperty(VideoCaptureProcessorProperty.GAME_SELECTOR_VISIBLE_PROPERTY));

    onFxThread(() -> gameButton(root).fire());
    assertFalse(panel(root).isExpanded());
    assertEquals("false", data.properties().getProperty(VideoCaptureProcessorProperty.GAME_SELECTOR_VISIBLE_PROPERTY));

    // It was saved to the file, not just kept in memory
    assertTrue(new File(temp.getRoot(), "CapProcConfig.xml").length() > 0);
  }

  @Test
  public void theGameButtonShowsTheSelectedGameWhateverTheSelectorIsDoing() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    Game alpha = game("Alpha", "A");
    data.catalog().add(alpha);
    Parent root = loadMainWindow();
    assertNull("no game yet", onFxThread(() -> gameButton(root).getGame().getGameTitle()));

    data.selection().select(alpha);

    assertEquals("Alpha", onFxThread(() -> gameButton(root).getGame().getGameTitle()));
    assertFalse("the selector stayed collapsed", panel(root).isExpanded());

    data.selection().select(null);
    assertNull(onFxThread(() -> gameButton(root).getGame().getGameTitle()));
  }

  @Test
  public void theSelectedGameIsRememberedAndRestoredNextTime() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    Game alpha = game("Alpha", "A");
    data.catalog().add(alpha);
    loadMainWindow();
    data.selection().select(alpha);
    assertEquals("Alpha", data.properties().getProperty(VideoCaptureProcessorProperty.SELECTED_GAME_PROPERTY));

    // A new run: the same files, a fresh app
    AppData nextRun = FxTestSupport.useTemporaryAppData(temp.getRoot());
    nextRun.properties().loadProperties();
    nextRun.catalog().load();
    com.wheezy.apps.vidcapproc.data.GameSelection.setInstance(null);
    Parent root = loadMainWindow();

    assertEquals("Alpha", onFxThread(() -> gameButton(root).getGame().getGameTitle()));
  }
}
