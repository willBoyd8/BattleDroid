package c3po.base;
import battlecode.common.*;
import c3po.utility.ActionHelper;
import c3po.utility.DebugHelper;

import java.util.Random;

public abstract class Unit {
    public RobotController rc;
    public MapLocation spawn;
    public Team enemy;
    public Team myTeam;
    public int age; // The number of rounds this unit has been alive
    public int birthday;
    public MapLocation hqLocation;
    public Random rand;
    public int hqElevation;
    public RobotInfo hqInfo;
    public MapLocation targetLocation;

    public Unit(RobotController rc){
        this.rc = rc;
        spawn = rc.getLocation();
        myTeam = rc.getTeam();
        enemy = myTeam.opponent();
        rand = new Random();
        hqElevation = 5;
        targetLocation = null;

        if(rc.getType() != RobotType.HQ) {
            hqInfo = ActionHelper.findHQ(rc);
            hqLocation = hqInfo.getLocation();
            try {
                if(rc.canSenseLocation(hqLocation)){
                    hqElevation = rc.senseElevation(hqLocation);
                }
            } catch (Exception e){
                hqElevation = 5;
            }
        }

        age = 0;
    }

    public final void run() throws KillMeNowException {
        System.out.println("I'm a " + rc.getType().toString());

        try {
            onInitialization();

            //noinspection InfiniteLoopStatement
            while (true) {

                try {
                    preStart();
                    roundStart();
                    turn();
                    preEnd();
                    roundEnd();
                } catch (KillMeNowException e) {
                    return;
                    //throw new KillMeNowException();
                } catch (Exception e) {
                    System.out.println(rc.getType().toString() + " Exception");
                    e.printStackTrace();
                    Clock.yield(); // if we fail, yield the clock
                }
            }
        } catch (Exception e) {
            System.out.println(rc.getType().toString() + " Exception");
            e.printStackTrace();
            Clock.yield();
        }

    }

    /**
     * This method should be implemented for every bot. This is the code that the bot will run every round
     * This is effectively where the individual unique unit types "strategy" should go.
     * @throws GameActionException
     */
    public abstract void turn() throws GameActionException, KillMeNowException;

    /**
     * This method is run at the start of every round for every unit we own
     */
    public final void roundStart(){
        age = rc.getRoundNum() - birthday; // Update age
    }

    /**
     * This method is run at the end of every round for every unit
     */
    public final void roundEnd(){
        DebugHelper.setIndicatorLine(rc.getLocation(), targetLocation, 255,0,0, rc);
        Clock.yield();
    }

    /**
     * Overload this method to run stuff before the normal round start gets run
     */
    public void preStart(){

    }

    /**
     * Overload this method to run stuff before the normal round ending
     */
    public void preEnd(){

    }

    /**
     * Run once on startup, immediately before the main loop begins
     * @throws GameActionException
     */
    public void onInitialization() throws GameActionException {}
}
