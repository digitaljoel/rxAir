package com.nerdherders.air;


public class RadioMessage {
  public final int flightNumber;
  public final Pair location;

  public RadioMessage( int flightNumber, Pair location ) {
    this.flightNumber = flightNumber;
    this.location = location;
  }

  @Override
  public String toString() {
    return "RadioMessage [flightNumber=" + flightNumber + ", location=" + location + "]";
  }

}
