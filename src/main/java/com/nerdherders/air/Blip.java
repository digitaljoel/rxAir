package com.nerdherders.air;


public class Blip {
  enum BlipType { MOVE, LAND, CRASH }
  public final Integer id;
  public final Pair location;
  public final BlipType type;
  public Blip( Integer id, Pair location, BlipType type ) {
    this.id = id;
    this.location = location;
    this.type = type;
  }
  @Override
  public String toString() {
    return "Blip [id=" + id + ", location=" + location + ", type=" + type + "]";
  }

}
