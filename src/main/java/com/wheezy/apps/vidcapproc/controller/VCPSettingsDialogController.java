package com.wheezy.apps.vidcapproc.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.wheezy.apps.vidcapproc.data.VCPSettings;
import com.wheezy.components.controller.FXMLController;
import com.wheezy.utils.file.FileUtility;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

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
  void browseForCaptureDirectory(ActionEvent event)
  {
    File selectedDirectory = FileUtility.browseForDirectory(getStage(), "Select Capture Directory");
    if (selectedDirectory != null)
    {
      vcpSettings.setCaptureDirectory(selectedDirectory);
    }
  }

  @FXML
  void browseForKeepersDirectory(ActionEvent event)
  {
    File selectedDirectory = FileUtility.browseForDirectory(getStage(), "Select Keepers Directory");
    if (selectedDirectory != null)
    {
      vcpSettings.setCaptureDirectory(selectedDirectory);
    }
  }

  @FXML
  void browseForClipsDirectory(ActionEvent event)
  {
    File selectedDirectory = FileUtility.browseForDirectory(getStage(), "Select Clips Directory");
    if (selectedDirectory != null)
    {
      vcpSettings.setCaptureDirectory(selectedDirectory);
    }
  }
}
