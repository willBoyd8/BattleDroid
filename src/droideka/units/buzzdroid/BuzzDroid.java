package droideka.units.buzzdroid;

import battlecode.common.*;
import droideka.base.MobileUnit;
import droideka.base.Unit;
import droideka.utility.ActionHelper;

import java.util.ArrayList;

public class BuzzDroid extends MobileUnit {
    public DroneState state;
    public ArrayList<MapLocation> enemyHQLocations;
    public int homeQuad;

    public BuzzDroid (RobotController rc) {
        super(rc);
        state = DroneState.GENERATE;
        targetLocation = null;
        enemyHQLocations = new ArrayList<MapLocation>();
        homeQuad = 0;
    }

    // TODO: This isn't 100% safe because it can take any mobile unit
    public BuzzDroid(MobileUnit unit){
        super(unit);
        state = DroneState.GENERATE;
        targetLocation = null;
        enemyHQLocations = new ArrayList<MapLocation>();
        homeQuad = 0;
    }

    enum DroneState {
        GENERATE,
        LOOK,
        MOVE_TO_POINT,
        CAN_SENSE,
        FOUND,
        REMOVE,
        RAID,
    }

    public void turn() throws GameActionException{

        switch(state){
            case GENERATE: generate(); break;

            case LOOK: look(); break;

            case MOVE_TO_POINT: moveToPoint(); break;

            case CAN_SENSE: canSense(); break;

            case FOUND: found(); break;

            case REMOVE: removePoint(); break;

            case RAID:  break;

        }

    }

    private void generate() throws GameActionException {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();

        //Figure out Home Quadrant
        if ((hqLocation.x > (mapWidth/2))&&(hqLocation.y > (mapHeight/2))){
            homeQuad = 1;
        }
        else if ((hqLocation.x < (mapWidth/2))&&(hqLocation.y > (mapHeight/2))){
            homeQuad = 2;
        }
        else if ((hqLocation.x < (mapWidth/2))&&(hqLocation.y < (mapHeight/2))){
            homeQuad = 3;
        }
        else if ((hqLocation.x > (mapWidth/2))&&(hqLocation.y > (mapHeight/2))){
            homeQuad = 4;
        }

        //Calculate Offsets and instantiate MapLocationObjects
        int xOffset = mapWidth-hqLocation.x;
        int yOffset = mapHeight-hqLocation.y;
        MapLocation loc1;
        MapLocation loc2;
        MapLocation loc3;
        MapLocation loc4;

        //Set Potential Locations based on Home Quadrant
        switch (homeQuad){
            case 1:
                loc2 = new MapLocation(xOffset, hqLocation.y);
                loc3 = new MapLocation(xOffset, yOffset);
                loc4 = new MapLocation(hqLocation.x, yOffset);
                enemyHQLocations.add(loc4);
                enemyHQLocations.add(loc3);
                enemyHQLocations.add(loc2);
                break;
            case 2:
                loc1 = new MapLocation(xOffset, hqLocation.y);
                loc3 = new MapLocation(hqLocation.x, yOffset);
                loc4 = new MapLocation(xOffset, yOffset);
                enemyHQLocations.add(loc3);
                enemyHQLocations.add(loc4);
                enemyHQLocations.add(loc1);
                break;
            case 3:
                loc1 = new MapLocation(xOffset, yOffset);
                loc2 = new MapLocation(hqLocation.x, yOffset);
                loc4 = new MapLocation(xOffset, hqLocation.y);
                enemyHQLocations.add(loc4);
                enemyHQLocations.add(loc1);
                enemyHQLocations.add(loc2);
                break;
            case 4:
                loc1 = new MapLocation(hqLocation.x, yOffset);
                loc2 = new MapLocation(xOffset, yOffset);
                loc3 = new MapLocation(xOffset, hqLocation.y);
                enemyHQLocations.add(loc3);
                enemyHQLocations.add(loc2);
                enemyHQLocations.add(loc1);
                break;
        }
        state = DroneState.LOOK;
        look();
        return;
    }

    private void look() throws GameActionException {
        if (rc.canSenseLocation(enemyHQLocations.get(0))){
            state = DroneState.CAN_SENSE;
            canSense();
            return;
        } else {
            state = DroneState.MOVE_TO_POINT;
            moveToPoint();
            return;
        }
    }

    private void moveToPoint() throws GameActionException {
        if (ActionHelper.tryMove(rc.getLocation().directionTo(enemyHQLocations.get(0)), rc)){
            state = DroneState.LOOK;
            look();
            return;
        }
        else{
            if (ActionHelper.tryMove(Direction.SOUTH, rc)){
                state = DroneState.LOOK;
                look();
                return;
            }

        }
    }

    private void canSense() throws GameActionException {
        RobotInfo potHQ = null;
        potHQ = rc.senseRobotAtLocation(enemyHQLocations.get(0));
        if (potHQ == null) {
            state = DroneState.REMOVE;
            removePoint();
            return;
        } else {
            state = DroneState.FOUND;
            found();
            return;
        }
    }

    private void found() throws GameActionException {
        //TODO: Implement the broadcasting of enemy HQ location
        state = DroneState.RAID;
        raid();
        return;
    }

    private void removePoint() throws  GameActionException {
        enemyHQLocations.remove(0);
        state = DroneState.LOOK;
        look();
        return;
    }

    private void raid() throws GameActionException {
        //TODO: Add Will's raid method
    }
}
