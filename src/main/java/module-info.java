/**
 * The whole app is one module. It is open because FXML, JAXB and the resource lookups
 * all reach in by reflection or by resource path (the FXML files and icons live in packages).
 */
open module com.wheezy.apps.vidcapproc
{
  requires javafx.controls;
  requires javafx.fxml;
  requires jakarta.xml.bind;
  requires java.desktop; // javax.imageio and java.awt.image, for writing game icons
  requires java.logging;

  exports com.wheezy.apps.vidcapproc;
}
