package com.nerdherders.air;

import static com.nerdherders.air.Blip.BlipType.MOVE;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class Tower implements Observable.OnSubscribe<RadioMessage>{

  private Pair location;

  Subscriber<? super RadioMessage> radio;
  Observable<Blip>  radar;

  public Tower( Pair location, Observable<Blip> radar ) {
    this.location = location;
    this.radar = radar;

    connectRadar();
  }

  @Override
  public void call(Subscriber<? super RadioMessage> t) {
    radio = t;
  }

  private void connectRadar() {
    // on a MOVE blip from a plane we will send them information on where to go next.
    radar.filter(b -> b.type == MOVE && !b.location.equals(location))
        .observeOn(Schedulers.computation())
        .subscribe(b -> {
          if ( !radio.isUnsubscribed()) {
            radio.onNext(new RadioMessage(b.id, getNewCoordinates(b.location)));
          }
        });
  }

  /**
   * Given one location, figure out what the next location on the map should be
   * @param c
   * @return
   */
  private Pair getNewCoordinates( Pair c ) {
    if ( c.x == location.x ) {
      return new Pair( c.x, c.y > location.y ? c.y-1 : c.y+1 );
    }
    else if ( c.y == location.y ) {
      return new Pair( c.x > location.x ? c.x-1 : c.x+1, c.y );
    }
    else if ( c.x < location.x ) {
      return c.y < location.y ? new Pair( c.x+1, c.y ) : new Pair( c.x, c.y-1 ); }
    else if ( c.x > location.x ) {
      return c.y < location.y ? new Pair( c.x, c.y+1 ) : new Pair( c.x-1, c.y ); }
    else return c;
  }
}
