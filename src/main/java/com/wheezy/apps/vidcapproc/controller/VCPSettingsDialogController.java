package com.wheezy.apps.vidcapproc.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.wheezy.apps.vidcapproc.data.VCPSettings;
import com.wheezy.components.controller.FXMLController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;

public class VCPSettingsDialogController extends FXMLController
{
  private VCPSettings vcpSettings;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    // TODO (don't really need to do anything here).
  }

  public VCPSettings getVCPSettings()
  {
    return vcpSettings;
  }

  public void setVCPSettings(VCPSettings vcpSettings)
  {
    this.vcpSettings = vcpSettings;
  }

  @FXML
  void openFileBrowser(ActionEvent event)
  {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Select Directory");
    File selectedDirectory = directoryChooser.showDialog(getStage());
    if (selectedDirectory != null)
    {
      // TODO Set Directory
    }
  }
}
