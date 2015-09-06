package com.nerdherders.air;

import static com.nerdherders.air.Blip.BlipType.*;

import java.util.Map;

import rx.Observer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

public class RadarScreen implements Observer<Blip> {

  private BiMap<Integer, Pair> flightMap = Maps.synchronizedBiMap(HashBiMap.create());
  private Map<Pair, Integer> crashes = Maps.newConcurrentMap();

  private int gridSize;
  private Pair location;

  public RadarScreen( int gridSize, Pair location ) {
    this.gridSize = gridSize;
    this.location = location;
  }

  @Override
  public void onNext(Blip t) {
    if ( t.type == LAND ) {
      // if they are landing just remove by id.
      flightMap.remove(t.id);
    }
    else {
      // remove by location
      flightMap.inverse().remove(t.location);
      if ( t.type == MOVE ) {
        flightMap.put(t.id, t.location);
      }
      else if ( t.type == CRASH ) {
        crashes.put(t.location, 10);
      }
    }
    printGraph();
  }

  @Override
  public void onCompleted() {
  }

  @Override
  public void onError(Throwable e) {
  }

  /**
   * Print our radar screen.  We could put this on a timer instead of printing on every radar interaction.
   */
  private void printGraph() {
    StringBuffer buf = new StringBuffer( "\n\n\n" );

    // print the top line
    for ( int i = 0; i < gridSize*5 + 1; i++ ) {
      buf.append( "-" );
    }
    buf.append( "\n" );

    // for each row print the row, then the line underneath.
    for ( int i = 0; i < gridSize; i++ ) {
      buf.append( "|" );
      for ( int j = 0; j < gridSize; j++ ) {
        if ( i == location.x && j == location.y ) {
          buf.append( " TT |");
          continue;
        }
        buf.append( String.format( "%4s|", getSymbol( i, j )));
      }
      buf.append( "\n|" );
      for ( int k = 0; k < gridSize; k++ ) {
        buf.append( "----|");
      }
      buf.append( "\n" );
    }
    synchronized( writeLock ) {
      System.out.println( buf );
    }
  }

  private String getSymbol( int x, int y ) {
    Pair pair = new Pair( x, y );
    Integer i = crashes.get(pair);
    if ( i != null ) {
      if ( i.intValue() == 5 ) {
        // make the crash plume bigger.
        crashes.remove(pair);
        crashes.put(new Pair(pair.x-1, pair.y), 4);
        crashes.put(new Pair(pair.x, pair.y-1), 4);
        crashes.put(new Pair(pair.x+1, pair.y), 4);
        crashes.put(new Pair(pair.x, pair.y+1), 4);
      }
      if ( i.intValue() == 0 ) {
        crashes.remove(pair);
      }
      else {
        crashes.put(pair, new Integer( i.intValue()-1));
      }
      return "****";
    }
    Integer flight = flightMap.inverse().get(pair);
    return flight == null ? "" : flight.toString();
  }

  // so we don't try to output two maps at once.
  private Object writeLock = new Object();
}
