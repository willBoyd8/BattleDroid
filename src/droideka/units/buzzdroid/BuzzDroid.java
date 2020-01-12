package droideka.units.buzzdroid;

import battlecode.common.*;
import droideka.base.MobileUnit;
import droideka.pathing.Simple;
import droideka.utility.ActionHelper;
import droideka.utility.Constants;
import droideka.utility.DebugHelper;

import java.util.ArrayList;

public class BuzzDroid extends MobileUnit {
    public DroneState state;
    public ArrayList<MapLocation> enemyHQLocations;
    public int homeQuad;
    public RaidState raidState;
    public RobotInfo holding;
    public MapLocation nearestWater;
    public MapLocation enemyHQ;

    public BuzzDroid (RobotController rc) {
        super(rc);
        state = DroneState.GENERATE;
        targetLocation = null;
        enemyHQLocations = new ArrayList<MapLocation>();
        homeQuad = 0;
        raidState = null;
        holding = null;
        nearestWater = null;
        enemyHQ = null;
    }

    // TODO: This isn't 100% safe because it can take any mobile unit
    public BuzzDroid(MobileUnit unit){
        super(unit);
        state = DroneState.GENERATE;
        targetLocation = null;
        enemyHQLocations = new ArrayList<MapLocation>();
        homeQuad = 0;
        raidState = null;
        holding = null;
        nearestWater = null;
        enemyHQ = null;
    }

    enum DroneState {
        GENERATE,
        LOOK,
        MOVE_TO_POINT,
        CAN_SENSE,
        FOUND,
        REMOVE,
        WAIT_TO_RAID,
        RAID
    }

    enum RaidState {
        BUZZ,
        KIDNAP,
        DROPPING,
        MOVING,
    }

    public void turn() throws GameActionException{



        switch(state){
            case GENERATE: generate(); break;

            case LOOK: look(); break;

            case MOVE_TO_POINT: moveToPoint(); break;

            case CAN_SENSE: canSense(); break;

            case FOUND: found(); break;

            case WAIT_TO_RAID: waitToRaid(); break;

            case REMOVE: removePoint(); break;

            case RAID: raid(); break;

        }

    }

    public  void preEnd() throws GameActionException {
        if(rc.senseFlooding(rc.getLocation())){
            nearestWater = rc.getLocation();
        }

        for(MapLocation loc : enemyHQLocations){
            DebugHelper.setIndicatorDot(loc, 255, 0, 0, rc);
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
        int xOffset = mapWidth-hqLocation.x-1;
        int yOffset = mapHeight-hqLocation.y-1;
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

        // TODO: When we actually fix rotation, this will need to be removed
        if(rc.isReady() && !rc.isCurrentlyHoldingUnit()){
            if(rc.getLocation().isAdjacentTo(hqLocation)){
                RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(Direction.EAST));
                if(robot != null && robot.getType() == RobotType.LANDSCAPER){
                    if(ActionHelper.tryPickup(robot.ID, rc)){
                        holding = robot;
                    }
                }
            }
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
        if (Simple.moveToLocationFuzzy(enemyHQLocations.get(0), rc)){
            state = DroneState.LOOK;
            return;
        }
        else{
            if (Simple.tryMove(Direction.SOUTH, rc) && rc.getLocation().distanceSquaredTo(hqLocation) < 5){
                state = DroneState.LOOK;
                return;
            }

        }
    }

    private void canSense() throws GameActionException {
        RobotInfo potHQ = null;
        potHQ = rc.senseRobotAtLocation(enemyHQLocations.get(0));
        if (potHQ == null || potHQ.getType() != RobotType.HQ) {
            state = DroneState.REMOVE;
            removePoint();
            return;
        } else {
            state = DroneState.FOUND;
            enemyHQLocations.clear();
            enemyHQLocations.add(potHQ.getLocation());
            enemyHQ = potHQ.getLocation();
            found();
            return;
        }
    }

    private void found() throws GameActionException {
        //TODO: Implement the broadcasting of enemy HQ location
        state = DroneState.WAIT_TO_RAID;
        waitToRaid();
        return;
    }

    private void waitToRaid() throws GameActionException {
        if(rc.getRoundNum() > Constants.RAID_START_ROUND){
            state = DroneState.RAID;
            targetLocation = null;
            raid();
            return;
        } else {
            // TODO: Wasting time here?
            return;
        }
    }

    private void removePoint() throws  GameActionException {
        enemyHQLocations.remove(0);
        state = DroneState.LOOK;
        look();
        return;
    }

    private void raid() throws GameActionException {
        if(rc.isCurrentlyHoldingUnit()){
            if(holding.getTeam() == myTeam){
                raidState = RaidState.BUZZ;
            } else  {
                raidState = RaidState.DROPPING;
            }
        } else {
            raidState = RaidState.KIDNAP;
        }

        switch(raidState){
            case BUZZ: buzz(); return;
            case KIDNAP: kidnap(); return;
            case DROPPING: dropping(); return;
            case MOVING: moving(); return;
        }


    }

    private void buzz() throws GameActionException {
        ArrayList<Direction> notFlooded = new ArrayList<>();

        for(Direction dir : Direction.allDirections()){
            if(!rc.senseFlooding(rc.getLocation().add(dir))){
                notFlooded.add(dir);
            }
        }

        if(notFlooded.size() <= 0){
            targetLocation = enemyHQ;
            moving();
            return;
        } else {
            for(Direction dir : notFlooded){
                if(ActionHelper.tryDrop(dir, rc)){
                    raidState = RaidState.KIDNAP;
                    return;
                }
            }

            if(rc.isReady()){
                if(ActionHelper.tryDrop(Direction.CENTER, rc)){
                    raidState = RaidState.KIDNAP;
                    return;
                }

            }
        }

    }

    private void kidnap() throws GameActionException {
        RobotInfo robots[] = rc.senseNearbyRobots(-1, enemy);

        int closest = Integer.MAX_VALUE;
        RobotInfo victim = null;

        if(robots.length <= 0){
            // TODO: This shouldn't happen, but what if it does? Maybe move towards any production buildings

        } else {
            for(RobotInfo robot : robots){
                if(robot.getType() == RobotType.MINER
                    || robot.getType() == RobotType.LANDSCAPER
                    || robot.getType() == RobotType.COW){

//                    if(ActionHelper.tryPickup(victim.ID, rc)){
//                        holding = victim;
//                        targetLocation = nearestWater;
//                        raidState = RaidState.DROPPING;
//                        return;
//                    }

                    if(robot.getLocation().distanceSquaredTo(rc.getLocation()) < closest){
                        closest = robot.getLocation().distanceSquaredTo(rc.getLocation());
                        victim = robot;
                    }
                }
            }

            if(victim != null){
                //if(victim.getLocation().isAdjacentTo(rc.getLocation())){
                if(ActionHelper.tryPickup(victim.ID, rc)){
                    holding = victim;
                    targetLocation = nearestWater;
                    raidState = RaidState.DROPPING;
                    return;
                //}
                } else {
                    targetLocation = victim.getLocation();
                    raidState = RaidState.MOVING;
                    moving();
                    return;
                }
            }
        }

        Simple.tryMove(rc);


    }

    // TODO: maybe if enough bytecode check for near water

    private void dropping() throws GameActionException {
        if(rc.getLocation().isAdjacentTo(nearestWater)){
            if(ActionHelper.tryDrop(rc.getLocation().directionTo(nearestWater), rc)){
                holding = null;
                raidState = RaidState.KIDNAP;
                kidnap();
                return;
            }
        }
        targetLocation = nearestWater;
        moving();
        return;
    }

    private void moving() throws GameActionException {
        if(rc.getLocation().isAdjacentTo(targetLocation)){
            targetLocation = null;
            raid();
            return;
        } else {
            Simple.tryMove(rc.getLocation().directionTo(targetLocation), rc);
            //Simple.moveToLocationFuzzy(targetLocation, rc);
            return;
        }
    }
}
