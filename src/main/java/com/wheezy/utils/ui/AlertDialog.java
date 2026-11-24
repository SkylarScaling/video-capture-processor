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
    displayAlertDialog(defaultOwner, alertType, title, header, content);
  }

  /** @param owner the window the dialog belongs to, or null for none */
  public static void displayAlertDialog(Window owner, AlertType alertType, String title, String header,
      String content)
  {
    Alert alert = new Alert(alertType);
    if (owner != null)
    {
      alert.initOwner(owner);
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
    return displayChoiceDialog(defaultOwner, alertType, title, header, content, buttons);
  }

  /** @param owner the window the dialog belongs to, or null for none */
  public static Optional<ButtonType> displayChoiceDialog(Window owner, AlertType alertType, String title,
      String header, String content, ButtonType... buttons)
  {
    Alert alert = new Alert(alertType, content, buttons);
    if (owner != null)
    {
      alert.initOwner(owner);
    }
    alert.setTitle(title);
    alert.setHeaderText(header);
    return alert.showAndWait();
  }

  public static void displayErrorDialog(String title, String header, String content)
  {
    displayAlertDialog(AlertType.ERROR, title, header, content);
  }

  /** @param owner the window the dialog belongs to, or null for none */
  public static void displayErrorDialog(Window owner, String title, String header, String content)
  {
    displayAlertDialog(owner, AlertType.ERROR, title, header, content);
  }

  public static void displayInfoDialog(String title, String header, String content)
  {
    displayAlertDialog(AlertType.INFORMATION, title, header, content);
  }
}
