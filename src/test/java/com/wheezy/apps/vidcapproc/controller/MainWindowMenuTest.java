package com.wheezy.apps.vidcapproc.controller;

import static com.wheezy.apps.vidcapproc.FxTestSupport.onFxThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.wheezy.apps.vidcapproc.FxTestSupport;
import com.wheezy.apps.vidcapproc.FxTestSupport.AppData;
import com.wheezy.apps.vidcapproc.ui.VideoCaptureProcessorResources;
import com.wheezy.apps.vidcapproc.ui.components.VideoCaptureProcessorStage;
import com.wheezy.apps.vidcapproc.ui.elements.GameSelectorPanel;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.file.properties.VideoCaptureProcessorProperty;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** The main window's File and Games menus. */
public class MainWindowMenuTest
{
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  private Stage showMainWindow() throws Exception
  {
    return onFxThread(() ->
    {
      VideoCaptureProcessorStage created = new VideoCaptureProcessorStage(
          VideoCaptureProcessorResources.VIDEO_CAPTURE_PROCESSOR_FXML, "test", false);
      created.show();
      return created;
    });
  }

  private static MenuBar menuBar(Stage stage)
  {
    return (MenuBar) stage.getScene().getRoot().lookup("#menuBar");
  }

  private static Menu menu(Stage stage, String text)
  {
    return menuBar(stage).getMenus().stream().filter(menu -> menu.getText().equals(text)).findFirst().orElseThrow();
  }

  private static MenuItem item(Stage stage, String menuText, String itemText)
  {
    return menu(stage, menuText).getItems().stream().filter(item -> item.getText().equals(itemText)).findFirst()
        .orElseThrow();
  }

  private static KeyCodeCombination ctrl(KeyCode code)
  {
    return new KeyCodeCombination(code, KeyCombination.CONTROL_DOWN);
  }

  /** What a click on a check item does: it flips, then fires. */
  private static void click(CheckMenuItem item)
  {
    item.setSelected(!item.isSelected());
    item.fire();
  }

  @Test
  public void hasFileAndGamesMenusWithTheSwingItemsAndShortcuts() throws Exception
  {
    FxTestSupport.useTemporaryAppData(temp.getRoot());
    Stage stage = showMainWindow();

    assertEquals(List.of("_File", "_Games"), menuBar(stage).getMenus().stream().map(Menu::getText).toList());
    assertEquals(List.of("_Always On Top", "_Set Folders..."),
        menu(stage, "_File").getItems().stream().map(MenuItem::getText).toList());
    assertEquals(List.of("_Show Game Selector", "_Manage Games..."),
        menu(stage, "_Games").getItems().stream().map(MenuItem::getText).toList());

    assertEquals(ctrl(KeyCode.A), item(stage, "_File", "_Always On Top").getAccelerator());
    assertEquals(ctrl(KeyCode.S), item(stage, "_Games", "_Show Game Selector").getAccelerator());
    assertEquals(ctrl(KeyCode.G), item(stage, "_Games", "_Manage Games...").getAccelerator());
    assertNotNull(item(stage, "_File", "_Set Folders...").getOnAction());
    assertNotNull(item(stage, "_Games", "_Manage Games...").getOnAction());
  }

  @Test
  public void theMenuBarSitsAboveTheControlsAndTheSelector() throws Exception
  {
    FxTestSupport.useTemporaryAppData(temp.getRoot());
    Stage stage = showMainWindow();

    VBox root = (VBox) stage.getScene().getRoot();
    assertEquals(2, root.getChildren().size());
    assertSame(menuBar(stage), root.getChildren().get(0));
    HBox row = (HBox) root.getChildren().get(1);
    assertSame(stage.getScene().getRoot().lookup("#gameSelectorPanel"), row.getChildren().get(1));
  }

  @Test
  public void alwaysOnTopIsCheckedByDefault() throws Exception
  {
    FxTestSupport.useTemporaryAppData(temp.getRoot());
    Stage stage = showMainWindow();

    assertTrue(((CheckMenuItem) item(stage, "_File", "_Always On Top")).isSelected());
  }

  @Test
  public void alwaysOnTopIsUncheckedWhenSavedAsOff() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    data.properties().setProperty(VideoCaptureProcessorProperty.ALWAYS_ON_TOP_PROPERTY, "false");
    data.properties().storeProperties();
    Stage stage = showMainWindow();

    assertFalse(((CheckMenuItem) item(stage, "_File", "_Always On Top")).isSelected());
  }

  @Test
  public void alwaysOnTopAppliesToTheWindowAndIsRemembered() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    Stage stage = showMainWindow();
    CheckMenuItem alwaysOnTop = (CheckMenuItem) item(stage, "_File", "_Always On Top");

    onFxThread(() -> click(alwaysOnTop)); // off
    assertFalse(stage.isAlwaysOnTop());
    assertFalse(data.properties().isAlwaysOnTop());
    VideoCaptureProcessorProperties saved = new VideoCaptureProcessorProperties(
        new File(temp.getRoot(), "CapProcConfig.xml").getPath());
    saved.loadProperties();
    assertFalse("saved to the file", saved.isAlwaysOnTop());

    onFxThread(() -> click(alwaysOnTop)); // on again
    assertTrue(stage.isAlwaysOnTop());
    assertTrue(data.properties().isAlwaysOnTop());
  }

  @Test
  public void showGameSelectorOpensAndClosesThePanelAndStaysInStepWithIt() throws Exception
  {
    FxTestSupport.useTemporaryAppData(temp.getRoot());
    Stage stage = showMainWindow();
    CheckMenuItem showSelector = (CheckMenuItem) item(stage, "_Games", "_Show Game Selector");
    GameSelectorPanel panel = (GameSelectorPanel) stage.getScene().getRoot().lookup("#gameSelectorPanel");
    assertFalse(showSelector.isSelected());

    onFxThread(() -> click(showSelector));
    assertTrue("menu opens the panel", panel.isExpanded());

    onFxThread(() -> panel.toggle()); // the strip or the game button
    assertFalse("panel closing unticks the menu", showSelector.isSelected());

    onFxThread(() -> panel.setExpanded(true));
    assertTrue("panel opening ticks the menu", showSelector.isSelected());
  }

  @Test
  public void showGameSelectorStartsTickedWhenTheSelectorWasLeftOpen() throws Exception
  {
    AppData data = FxTestSupport.useTemporaryAppData(temp.getRoot());
    data.properties().setProperty(VideoCaptureProcessorProperty.GAME_SELECTOR_VISIBLE_PROPERTY, "true");
    data.properties().storeProperties();
    Stage stage = showMainWindow();

    assertTrue(((CheckMenuItem) item(stage, "_Games", "_Show Game Selector")).isSelected());
  }
}
