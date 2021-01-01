package com.wheezy.apps.vidcapproc.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.wheezy.apps.vidcapproc.data.VCPSettings;

import javafx.fxml.Initializable;

public class VCPSettingsDialogController implements Initializable
{
  private VCPSettings vcpSettings;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    // TODO (don't really need to do anything here).
  }
  
  public void setVCPSettings(VCPSettings vcpSettings)
  {
    this.vcpSettings = vcpSettings;
  }
}
