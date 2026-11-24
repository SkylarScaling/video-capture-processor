package com.wheezy.apps.vidcapproc.data;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The games as they are stored in {@code SavedGames.xml}: a {@code gameCollection}
 * element holding one {@code gameSet} element per game, in order. This is the same
 * format the Swing app writes, so its file can be read as it is.
 */
@XmlRootElement(name = "gameCollection")
public class GameCollection
{
  private final List<Game> gameSet = new ArrayList<>();

  @XmlElement(name = "gameSet")
  public List<Game> getGameSet()
  {
    return gameSet;
  }
}
