package com.nerdherders.air;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import static com.nerdherders.air.Blip.BlipType.*;

public class AirTrafficControl {

  private static long seed = System.currentTimeMillis();
//  private static long seed = 1441228849953l;
  private static Random random = new Random(seed);

  private static int VAR_SLEEP = 500; // maximum extra time to sleep before introducing a new plane.
  private static int BASE_SLEEP = 50; // minimum time to sleep before introducing a new plane.
  private static int BASE_SPEED = 250; // minimum speed of all planes.
  private static int VAR_SPEED = 250; // maximum additional speed of planes.
  private static int PLANES = 100; // number of planes to simulate.
  private static int GRID_SIZE = 15; // The playing field will be this size, square.
  private static Pair TOWER_LOCATION = new Pair(GRID_SIZE/2, GRID_SIZE/2); // Where the tower is located in the grid.

  /**
   * Run stuff and do things!
   * @param args
   * @throws Exception
   */
  public static void main( String... args ) throws Exception {

    // output the seed so we can recreate this if something goes wrong.
    System.out.println( "Seed: " + seed );
    // create the subjects for the radio and the radar
    PublishSubject<RadioMessage> radio = PublishSubject.create();
    PublishSubject<Blip> radar = PublishSubject.create();

    // subscription merely for debugging to show all traffic on the radar and radio.
//    radio.subscribe( msg -> System.out.println( msg ));
//    radar.subscribe( blip -> System.out.println( blip ));

    // have the radar screen listen to the radar so it can show the plane locations.
    radar.subscribe(new RadarScreen(GRID_SIZE, TOWER_LOCATION));


    // create the tower, which is where the planes try to get to land.
    Tower tower = new Tower(TOWER_LOCATION, radar);
    // and allow the tower to emit on the radio
    Observable.create( tower ).subscribe(radio);

    // Create a latch so we don't end the program prematurely.
    CountDownLatch latch = new CountDownLatch(PLANES);

    // subscribe to the radar so we countdown whenever a plane lands or crashes.
    radar.filter(b -> b.id != -1 && (b.type == LAND || b.type == CRASH))
      .subscribe(b -> latch.countDown());

    // a sleep time before the next plane enters the grid.
    long totalSleep = 0;

    // create all the planes.
    for ( int i = 0; i < PLANES; i++ ) {
      Plane plane = new Plane(i, getNewSpeed(), getStartingPair(GRID_SIZE), TOWER_LOCATION, radar);
      // subscribe the plane to the radio
      radio.filter(msg -> msg.flightNumber == plane.flightNumber )
          // if we subscribe on a different thread, then we may not get our first message.
          .observeOn(Schedulers.computation())
          .subscribe( plane );
      // tell the radar to listen to blips from the plane.
      Observable.create(plane).subscribe(radar);
      // rather than start them all at once, they will enter the grid when this Observable calls onNext.
      Observable.timer(totalSleep, TimeUnit.MILLISECONDS).subscribe( n -> plane.takeoff());
      totalSleep += getNextSleep();
      System.out.println( plane );
    }
    // complete when all planes have landed or crashed.
    latch.await();
  }

  private static int getNewSpeed() {
    return BASE_SPEED + random.nextInt(VAR_SPEED);
  }

  private static int getNextSleep() {
    return BASE_SLEEP + random.nextInt(VAR_SLEEP);
  }

  private static Pair getStartingPair( int size ) {
    int side = random.nextInt(4);
    switch( side ) {
      case 0 :
        return new Pair( 0, random.nextInt(size));
      case 1 :
        return new Pair( size-1, random.nextInt(size));
      case 2 :
        return new Pair( random.nextInt(size), 0 );
      default :
        return new Pair( random.nextInt(size), size-1 );
    }
  }
}
