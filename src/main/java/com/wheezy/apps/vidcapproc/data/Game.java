package com.wheezy.apps.vidcapproc.data;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Game
{
  private String gameTitle;
  private String filenameLabel;
  private String iconImageFilepath;
  private boolean isDisplayed;

  public Game()
  {
  }

  @XmlElement
  public String getGameTitle()
  {
    return gameTitle;
  }

  public void setGameTitle(String gameTitle)
  {
    this.gameTitle = gameTitle;
  }

  @XmlElement
  public String getFilenameLabel()
  {
    return filenameLabel;
  }

  public void setFilenameLabel(String filenameLabel)
  {
    this.filenameLabel = filenameLabel;
  }

  @XmlElement
  public String getIconImageFilepath()
  {
    return iconImageFilepath;
  }

  public void setIconImageFilepath(String iconImageFilepath)
  {
    this.iconImageFilepath = iconImageFilepath;
  }

  @XmlElement
  public boolean isDisplayed()
  {
    return isDisplayed;
  }

  public void setDisplayed(boolean isDisplayed)
  {
    this.isDisplayed = isDisplayed;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof Game && ((Game) obj).getGameTitle() != null)
    {
      if (gameTitle != null)
      {
        return gameTitle.equalsIgnoreCase(((Game) obj).getGameTitle());
      }
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    if (gameTitle != null)
    {
      return gameTitle.hashCode();
    }

    return super.hashCode();
  }

  @Override
  public String toString()
  {
    return "Game Title [" + this.gameTitle + "], Filename Label [" + this.filenameLabel + "], Icon Image Filepath ["
        + this.iconImageFilepath + "]";
  }

  public static void main(String[] args) throws Throwable
  {
    // =============================================================================================================
    // Setup JAXB
    // =============================================================================================================

    // Create a JAXB context passing in the class of the object we want to
    // marshal/unmarshal
    JAXBContext context = JAXBContext.newInstance(Game.class);

    // =============================================================================================================
    // Marshalling OBJECT to XML
    // =============================================================================================================

    // Create the marshaller, this is the nifty little thing that will actually
    // transform the object into XML
    Marshaller marshaller = context.createMarshaller();

    // Create a stringWriter to hold the XML
    StringWriter stringWriter = new StringWriter();

    // Create the sample object we wish to transform into XML
    Game game = new Game();
    game.setGameTitle("Black Ops");
    game.setFilenameLabel("BO");
    game.setIconImageFilepath("/images/BlackOps.jpg");

    // Marshal the javaObject and write the XML to the stringWriter
    marshaller.marshal(game, stringWriter);

    // Print out the contents of the stringWriter
    System.out.println(stringWriter.toString());

    // =============================================================================================================
    // Unmarshalling XML to OBJECT
    // =============================================================================================================

    // Create the unmarshaller, this is the nifty little thing that will actually
    // transform the XML back into an object
    Unmarshaller unmarshaller = context.createUnmarshaller();

    // Unmarshal the XML in the stringWriter back into an object
    Game javaObject2 = (Game) unmarshaller.unmarshal(new StringReader(stringWriter.toString()));

    // Print out the contents of the JavaObject we just unmarshalled from the XML
    System.out.println(javaObject2.toString());
  }
}
