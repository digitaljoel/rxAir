package com.nerdherders.air;

import static com.nerdherders.air.Blip.BlipType.CRASH;
import static com.nerdherders.air.Blip.BlipType.LAND;
import static com.nerdherders.air.Blip.BlipType.MOVE;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;


public class Plane implements Observer<RadioMessage>, Observable.OnSubscribe<Blip> {

  public final int flightNumber;
  private Pair location;
  private Pair towerLocation;
  private int speed;
  private AtomicBoolean flying = new AtomicBoolean(false);

  Observable<Blip> radar;
  Subscription radarSubscription;
  Subscriber<? super Blip> blipSubscriber;

  /**
   * Create a new plane
   * @param flightNumber
   * @param speed
   * @param startingLocation
   * @param towerId
   * @param radar we need the radar so that we can observe it since we can't implement
   *        the Observer interface twice.
   */
  public Plane( int flightNumber, int speed, Pair startingLocation,
        Pair towerLocation, Observable<Blip> radar ) {
    this.flightNumber = flightNumber;
    this.speed = speed;
    this.radar = radar;
    this.location = startingLocation;
    this.towerLocation = towerLocation;
  }

  @Override
  public void onCompleted() {
    System.out.println( "Completed: " + this );
  }

  @Override
  public void onError(Throwable e) {
    System.out.println( e );
  }

  @Override
  public void onNext(RadioMessage m) {
    Observable.timer( speed, TimeUnit.MILLISECONDS)
    .first()
    .subscribe( n -> {
        // when the timer goes off, it calls this onNext message
        if (flying.get()) {
          // if we haven't crashed while traveling to our new location then set our current to the new.
          this.location = m.location;
          if ( this.location.equals(towerLocation) ) {
            land();
          }
          else {
            // if we haven't landed, then send a blip to tell the tower our new location.
            move();
          }
        }});
  }

  @Override
  public void call( Subscriber<? super Blip> t ) {
    blipSubscriber = t;
  }

  private void sendBlip( Blip blip ) {
    if ( !blipSubscriber.isUnsubscribed()) {
      blipSubscriber.onNext(blip);
    }
  }

  public void takeoff() {
    flying.set(true);
    connectRadar();
    sendBlip( new Blip( flightNumber, location, MOVE ));
  }

  private void land() {
    flying.set(false);
    sendBlip(new Blip(flightNumber, location, LAND));
    radarSubscription.unsubscribe();
  }

  private void move() {
    sendBlip(new Blip(flightNumber, location, MOVE));
  }

  /**
   * Subscribe to the radar
   */
  private void connectRadar() {
    // get blips that are in our airspace that is not us.
    // if we get a blip it must be another airplane that will cause us to crash.
    radarSubscription = radar.filter(b -> b.id != this.flightNumber)
      .filter(b -> b.location.equals(this.location))
      .filter(b -> b.type == MOVE || b.type == CRASH )
      .observeOn(Schedulers.computation())
      .subscribe(blip -> {
          // on any blip on the radar in our space, it will cause us to crash if we are still flying.
          if ( flying.get()) {
            sendBlip(getRadarResponse(blip));
          }
      });
  }

  private Blip getRadarResponse( Blip blip ) {
    // whether landing or crashing we are done with this flight.
    flying.set(false);
    radarSubscription.unsubscribe();
    return new Blip( flightNumber, location, Blip.BlipType.CRASH);
  }

  @Override
  public String toString() {
    return "Plane [flightNumber=" + flightNumber + ", location=" + location + ", speed=" + speed + ", flying=" + flying
      + "]";
  }

}
