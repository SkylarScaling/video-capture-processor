package com.wheezy.apps.vidcapproc.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wheezy.apps.vidcapproc.FxTestSupport;

import javafx.stage.Stage;

public class MainWindowTrayTest
{
  @BeforeClass
  public static void startToolkit()
  {
    FxTestSupport.startToolkit();
  }

  @Test
  public void theMenuOffersTheMainActionsAndEachRunsItsAction() throws Exception
  {
    Assume.assumeFalse(GraphicsEnvironment.isHeadless());
    List<String> ran = Collections.synchronizedList(new ArrayList<>());
    CountDownLatch all = new CountDownLatch(4);
    PopupMenu menu = MainWindowTray.buildMenu(() -> record(ran, "exit", all), () -> record(ran, "keepers", all),
        () -> record(ran, "clips", all), () -> record(ran, "mark", all));

    assertEquals(List.of("Keepers", "Clips", "Mark Clip", "-", "Exit"),
        IntStream.range(0, menu.getItemCount()).mapToObj(i -> menu.getItem(i).getLabel()).toList());

    for (int i = 0; i < menu.getItemCount(); i++)
    {
      MenuItem item = menu.getItem(i);
      if (!item.getLabel().equals("-"))
      {
        for (ActionListener listener : item.getActionListeners())
        {
          listener.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, item.getLabel()));
        }
      }
    }
    assertTrue("actions ran", all.await(10, TimeUnit.SECONDS));
    assertEquals(List.of("keepers", "clips", "mark", "exit"), ran);
  }

  private static void record(List<String> ran, String what, CountDownLatch all)
  {
    ran.add(what);
    all.countDown();
  }

  @Test
  public void showRestoresAMinimizedWindow() throws Exception
  {
    Stage stage = FxTestSupport.onFxThread(() ->
    {
      Stage created = new Stage();
      created.show();
      created.setIconified(true);
      return created;
    });

    FxTestSupport.onFxThread(() -> MainWindowTray.show(stage));

    assertTrue(FxTestSupport.onFxThread(() -> !stage.isIconified()));
    FxTestSupport.onFxThread(stage::close);
  }
}
