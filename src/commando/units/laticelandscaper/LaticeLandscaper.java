package commando.units.laticelandscaper;

import battlecode.common.*;
import commando.base.MobileUnit;
import commando.pathing.Simple;
import commando.utility.ActionHelper;
import commando.utility.Constants;
import commando.utility.DroidList;
import commando.utility.Unsorted;

import java.util.Random;

public class LaticeLandscaper extends MobileUnit {

    LaticeState state;
    DroidList<MapLocation> wallLocations;

    public LaticeLandscaper(RobotController rc){
        super(rc);
        state = LaticeState.LATICE_BUILDING;
        wallLocations = new DroidList<>();
    }

    enum LaticeState {
        MOVING_TO_WALL,
        EARLY_WALLING,
        LATE_WALLING,
        LATICE_BUILDING,
        MOVING_TO_LATICE_EDGE,
        ATTACKING
    }

    public void turn() throws GameActionException {

        if(state == LaticeState.EARLY_WALLING && GameConstants.getWaterLevel(rc.getRoundNum()) >= hqElevation - Constants.WALL_SAFTEY_BARRIER){
            state = LaticeState.LATE_WALLING;
        }

        switch(state){
            case MOVING_TO_WALL: movingToWall(); break;
            case EARLY_WALLING: earlyWalling(); break;
            case LATE_WALLING: lateWalling(); break;
            case LATICE_BUILDING: laticeBuilding(); break;
            case MOVING_TO_LATICE_EDGE: movingToLaticeEdge(); break;
            case ATTACKING: attacking(); break;
        }

    }

    /**
     * State for moving the bot to the wall. Kind unreliable right now. It needs some actual bug pathing
     * @throws GameActionException
     */
    public void movingToWall() throws GameActionException {
        if(wallLocations.contains(rc.getLocation())){
            state = LaticeState.EARLY_WALLING;
            earlyWalling();
            return;
        }

        // TODO:  Implement Bug Pathing
        if(!Simple.moveToLocationFuzzy(wallLocations.get(0), rc) && rc.isReady()){
            MapLocation loc = rc.getLocation().add(rc.getLocation().directionTo(wallLocations.get(0)));
            int height = Integer.MIN_VALUE;
            if(rc.canSenseLocation(loc)){
                height = rc.senseElevation(loc);
            }

            if(rc.senseElevation(rc.getLocation()) > height){
                ActionHelper.tryDig(rc.getLocation().directionTo(loc), rc);
            } else {
                if(rc.getDirtCarrying() > 0){
                    ActionHelper.tryDepositDirt(rc.getLocation().directionTo(loc), rc);
                } else {
                    for (Direction dir : Constants.DIRECTIONS) {
                        MapLocation dirLoc = rc.getLocation().add(dir);
                        if (rc.onTheMap(dirLoc) && !loc.isAdjacentTo(hqLocation) && dirLoc.x % 2 == 1 && loc.y == 1) {
                            if(ActionHelper.tryDig(dir, rc)){
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Logic for state where Landscaper just builds itself straight up into the air.
     * @throws GameActionException
     */
    public void earlyWalling() throws GameActionException{
        if(rc.getDirtCarrying() > 0) {
            for (Direction dir : Constants.DIRECTIONS) {
                MapLocation loc = rc.getLocation().add(dir);
                if (rc.onTheMap(loc) && !loc.isAdjacentTo(hqLocation) && loc.x % 2 == 1 && loc.y == 1) {
                    ActionHelper.tryDig(dir, rc);
                }
            }
        } else {
            rc.depositDirt(Direction.CENTER);
        }
    }

    /**
     * Method for walling really late into the game. This isn't any different, except that it tries to average the wall height
     * @throws GameActionException
     */
    public void lateWalling() throws GameActionException{
        if(rc.getDirtCarrying() > 0) {
            for (Direction dir : Constants.DIRECTIONS) {
                MapLocation loc = rc.getLocation().add(dir);
                if (rc.onTheMap(loc) && !loc.isAdjacentTo(hqLocation) && loc.x % 2 == 1 && loc.y == 1) {
                    ActionHelper.tryDig(dir, rc);
                }
            }
        } else {
            Direction best = Direction.CENTER;
            int lowest = Integer.MAX_VALUE;
            for(Direction  dir : Constants.DIRECTIONS){
                MapLocation loc = rc.getLocation().add(dir);
                if(rc.canSenseLocation(loc)){
                    int height = rc.senseElevation(loc);
                    if(height < lowest && loc != hqLocation && loc.isAdjacentTo(hqLocation)){
                        lowest = height;
                        best = dir;
                    }
                }
            }

            ActionHelper.tryDepositDirt(best, rc);
        }

    }

    public void laticeBuilding() throws GameActionException {
        DroidList<MapLocation> unfinished = adjacentUncompletedLaticeTiles();
        if(unfinished.size() <= 0){
            state = LaticeState.MOVING_TO_LATICE_EDGE;
            movingToLaticeEdge();
            return;
        }

        if(rc.getDirtCarrying() <= 0){
            for (Direction dir : Constants.DIRECTIONS) {
                MapLocation loc = rc.getLocation().add(dir);
                if (rc.onTheMap(loc) && !loc.isAdjacentTo(hqLocation) && loc.x % 2 == 1 && loc.y % 2 == 1) {
                    if(ActionHelper.tryDig(dir, rc)){
                            return;
                    }
                }
            }
        } else {
            for(MapLocation loc : unfinished){
                if(ActionHelper.tryDepositDirt(rc.getLocation().directionTo(loc), rc)){
                    break;
                }
            }
        }
    }

    public void movingToLaticeEdge() throws GameActionException {
        DroidList<MapLocation> unfinished = adjacentUncompletedLaticeTiles();
        if(unfinished.size() > 0){
            state = LaticeState.LATICE_BUILDING;
            laticeBuilding();
            return;
        }

        // TODO: This expansion method is sloppy, but might work
        if(targetLocation == null){
            targetLocation = Unsorted.generatePossibleEnemyHQLocation(hqLocation, rc).get(rand.nextInt(3));
        }

        Simple.moveToLocationFuzzy(targetLocation, rc);
    }

    private DroidList<MapLocation> adjacentUncompletedLaticeTiles() throws GameActionException{
        DroidList<MapLocation> adjacent = new DroidList<>();

        for(Direction dir : Direction.allDirections()){
            MapLocation loc = rc.getLocation().add(dir);
            if(rc.canSenseLocation(loc)){
                int height = rc.senseElevation(loc);
                // TODO: we probably aren't handling all the possible cases here
                if(height < Constants.LATICE_HEIGHT && (loc.x % 2 == 0 || loc.y % 2 == 0)){
                    adjacent.add(loc);
                }
            }
        }

        return adjacent;
    }

    public void attacking() throws GameActionException {
        // TODO: Implement this state
    }

}