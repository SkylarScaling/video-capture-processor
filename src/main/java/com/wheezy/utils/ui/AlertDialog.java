package com.wheezy.utils.ui;

import javafx.scene.control.Alert;

public class AlertDialog extends Alert
{

  public AlertDialog(AlertType alertType)
  {
    super(alertType);
  }
  
  public static void displayAlertDialog(AlertType alertType, String title, String header, String content)
  {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
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
