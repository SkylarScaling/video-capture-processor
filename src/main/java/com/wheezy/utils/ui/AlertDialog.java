package com.wheezy.utils.ui;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

public class AlertDialog extends Alert
{
  private static Window defaultOwner;

  public AlertDialog(AlertType alertType)
  {
    super(alertType);
  }

  /**
   * Sets the window that dialogs shown through the static helpers belong to. An
   * owned dialog always appears above its owner, which matters when the owner is
   * an always-on-top window: without an owner the dialog can open behind it.
   */
  public static void setDefaultOwner(Window owner)
  {
    defaultOwner = owner;
  }

  public static void displayAlertDialog(AlertType alertType, String title, String header, String content)
  {
    Alert alert = new Alert(alertType);
    if (defaultOwner != null)
    {
      alert.initOwner(defaultOwner);
    }
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }

  /**
   * Shows a dialog with the given buttons and waits for the answer.
   *
   * @return the button pressed, or empty if the dialog was closed without choosing
   */
  public static Optional<ButtonType> displayChoiceDialog(AlertType alertType, String title, String header,
      String content, ButtonType... buttons)
  {
    Alert alert = new Alert(alertType, content, buttons);
    if (defaultOwner != null)
    {
      alert.initOwner(defaultOwner);
    }
    alert.setTitle(title);
    alert.setHeaderText(header);
    return alert.showAndWait();
  }
  
  public static void displayErrorDialog(String title, String header, String content)
  {
    displayAlertDialog(AlertType.ERROR, title, header, content);
  }
  
  public static void displayInfoDialog(String title, String header, String content)
  {
    displayAlertDialog(AlertType.INFORMATION, title, header, content);
  } 
}
