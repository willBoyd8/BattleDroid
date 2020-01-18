package commando.units.laticelandscaper;

import battlecode.common.*;
import commando.base.KillMeNowException;
import commando.base.MobileUnit;
import commando.communication.CommunicationHelper;
import commando.pathing.Bug;
import commando.pathing.Simple;
import commando.utility.*;

import javax.swing.*;
import java.util.Random;

public class LaticeLandscaper extends MobileUnit {

    LaticeState state;
    DroidList<MapLocation> wallLocations;
    int gridOffsetX, gridOffsetY;
    Bug path;
    DroidList<MapLocation> enemyHQBlacklist;

    public LaticeLandscaper(RobotController rc){
        super(rc);
        state = LaticeState.MOVING_TO_WALL;
        wallLocations = new DroidList<>();
        gridOffsetX = 0;
        gridOffsetY = 0;
        path = null;
        enemyHQBlacklist = new DroidList<>();
    }

    enum LaticeState {
        MOVING_TO_WALL,
        EARLY_WALLING,
        LATE_WALLING,
        LATICE_BUILDING,
        MOVING_TO_LATICE_EDGE,
        ATTACKING
    }

    @Override
    public void onInitialization() throws GameActionException {
        catchup();
//        wallLocations.addAll(ActionHelper.generateAdjacentTiles(hqLocation, rc));
        if(hqLocation.x % 2 == 0){
            gridOffsetX = 1;
        }
        if(hqLocation.y % 2 == 0){
            gridOffsetY = 1;
        }

        enemyHQLocations = Unsorted.generatePossibleEnemyHQLocation(hqLocation, rc);
        enemyHQBlacklist.removeAll(enemyHQBlacklist);
    }

    public void turn() throws GameActionException {
        checkMessages();
        checkEnemyHQ();

        if(rc.getLocation().isAdjacentTo(hqLocation)){
            state = LaticeState.EARLY_WALLING;
        }

        if(state == LaticeState.EARLY_WALLING && GameConstants.getWaterLevel(rc.getRoundNum()) >= hqElevation - Constants.WALL_SAFTEY_BARRIER){
            state = LaticeState.LATE_WALLING;
        } else if(wallLocations.size() <= 0 && state != LaticeState.EARLY_WALLING && state != LaticeState.LATE_WALLING){
            state = LaticeState.LATICE_BUILDING;
        } else if(wallLocations.size() > 0 && !(state == LaticeState.EARLY_WALLING || state == LaticeState.LATE_WALLING || state == LaticeState.MOVING_TO_WALL)){
            state = LaticeState.MOVING_TO_WALL;
        }



        switch(state){
            case MOVING_TO_WALL: DebugHelper.setIndicatorDot(rc.getLocation(), 255, 0, 0, rc); movingToWall(); break;
            case EARLY_WALLING: DebugHelper.setIndicatorDot(rc.getLocation(), 0, 255, 0, rc); earlyWalling(); break;
            case LATE_WALLING: DebugHelper.setIndicatorDot(rc.getLocation(), 0, 0, 255, rc); lateWalling(); break;
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

        if(path == null) {
            path = new Bug(rc.getLocation(), wallLocations.get(0), rc);
        } else {
            path.updateDestination(wallLocations.get(0));
        }

        path.run();

//        // TODO:  Implement Bug Pathing
//        if(!Simple.moveToLocationFuzzy(wallLocations.get(0), rc) && rc.isReady()){
//            MapLocation loc = rc.getLocation().add(rc.getLocation().directionTo(wallLocations.get(0)));
//            int height = Integer.MIN_VALUE;
//            if(rc.canSenseLocation(loc)){
//                height = rc.senseElevation(loc);
//            }
//
//            if(rc.senseElevation(rc.getLocation()) > height){
//                ActionHelper.tryDig(rc.getLocation().directionTo(loc), rc);
//            } else {
//                if(rc.getDirtCarrying() > 0){
//                    ActionHelper.tryDepositDirt(rc.getLocation().directionTo(loc), rc);
//                } else {
//                    for (Direction dir : Constants.DIRECTIONS) {
//                        MapLocation dirLoc = rc.getLocation().add(dir);
//                        if (rc.onTheMap(dirLoc) && !dirLoc.isAdjacentTo(hqLocation) && (dirLoc.x - gridOffsetX) % 2 == 1 && (dirLoc.y - gridOffsetY) % 2 == 1) {
//                            if(ActionHelper.tryDig(dir, rc)){
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    /**
     * Logic for state where Landscaper just builds itself straight up into the air.
     * @throws GameActionException
     */
    public void earlyWalling() throws GameActionException{
        if(rc.getDirtCarrying() <= 0) {
            for (Direction dir : Constants.DIRECTIONS) {
                MapLocation loc = rc.getLocation().add(dir);
                if (rc.onTheMap(loc) && !loc.isAdjacentTo(hqLocation) && (loc.x - gridOffsetX) % 2 == 1 && (loc.y - gridOffsetY) % 2 == 1) {
                    ActionHelper.tryDig(dir, rc);
                }
            }
        } else {
            ActionHelper.tryDepositDirt(Direction.CENTER, rc);
        }
    }

    /**
     * Method for walling really late into the game. This isn't any different, except that it tries to average the wall height
     * @throws GameActionException
     */
    public void lateWalling() throws GameActionException{
        if(rc.getDirtCarrying() <= 0) {
            for (Direction dir : Constants.DIRECTIONS) {
                MapLocation loc = rc.getLocation().add(dir);
                if (rc.onTheMap(loc) && !loc.isAdjacentTo(hqLocation) && (loc.x -gridOffsetX) % 2 == 1 && (loc.y -gridOffsetY) % 2 == 1) {
                    if(ActionHelper.tryDig(dir, rc)){
                        break;
                    }
                }
            }
        } else {
            Direction best = Direction.CENTER;
            int lowest = Integer.MAX_VALUE;
            for(Direction  dir : Direction.allDirections()){
                MapLocation loc = rc.getLocation().add(dir);
                if(rc.canSenseLocation(loc)){
                    int height = rc.senseElevation(loc);
                    if(height < lowest && !loc.equals(hqLocation) && loc.isAdjacentTo(hqLocation)){
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
                if (rc.onTheMap(loc) && !loc.isAdjacentTo(hqLocation) && (loc.x - gridOffsetX) % 2 == 1 && (loc.y - gridOffsetY) % 2 == 1) {
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
            if(enemyHQ != null){
                targetLocation = enemyHQ;
                path = new Bug(rc.getLocation(), targetLocation, rc);
            } else {
                targetLocation = enemyHQLocations.get(rand.nextInt(enemyHQLocations.size()));
                path = new Bug(rc.getLocation(), targetLocation, rc);
            }
        }

        path.run(gridOffsetX, gridOffsetY);
    }

    private DroidList<MapLocation> adjacentUncompletedLaticeTiles() throws GameActionException{
        DroidList<MapLocation> adjacent = new DroidList<>();

        for(Direction dir : Direction.allDirections()){
            MapLocation loc = rc.getLocation().add(dir);
            if(rc.canSenseLocation(loc)){
                int height = rc.senseElevation(loc);
                RobotInfo occupied = rc.senseRobotAtLocation(loc);
                boolean isOccupied = false;
                if(occupied != null){
                    if(ActionHelper.isBuilding(occupied) && occupied.getTeam() == myTeam){
                        isOccupied = true;
                    }
                }
                // TODO: we probably aren't handling all the possible cases here
                // height < Constants.LATICE_HEIGHT for a static height
                // use round number and water elevation for a less static grid
                if(!isOccupied && (height < GameConstants.getWaterLevel(rc.getRoundNum()) + 4 || height < 5) && ((loc.x - gridOffsetX) % 2 == 0 || (loc.y - gridOffsetY) % 2 == 0)){
                    adjacent.add(loc);
                }
            }
        }

        return adjacent;
    }

    public void attacking() throws GameActionException {
        // TODO: Implement this state
    }

    public void checkEnemyHQ() throws GameActionException{
        if(enemyHQ == null && targetLocation != null){
            if(rc.canSenseLocation(targetLocation)){
                RobotInfo robot = rc.senseRobotAtLocation(targetLocation);
                if(robot == null) {
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 4;
                    message[2] = CommunicationHelper.convertLocationToMessage(targetLocation);
                    messageQueue.add(message);
                } else {
                    if(robot.getType() == RobotType.HQ){
                        int[] message = new int[7];
                        message[0] = Constants.MESSAGE_KEY;
                        message[1] = 5;
                        message[2] = CommunicationHelper.convertLocationToMessage(targetLocation);
                        messageQueue.add(message);
                    }
                }
            }
        }
    }

    public void catchup() throws GameActionException{
        int counter = 1;
        while(counter < rc.getRoundNum()){
            checkMessages(counter);
            counter++;
        }
    }

    public void checkMessages() throws GameActionException {
        checkMessages(rc.getRoundNum() - 1);
    }

    public void checkMessages(int roundNumber) throws GameActionException{
        Transaction[] transactions = rc.getBlock(roundNumber);

        for(Transaction trans : transactions){
            int[] message = trans.getMessage();
            if(message[0] == Constants.MESSAGE_KEY){
                processMessage(message);
            }
        }
    }

    public void processMessage(int[] message){
        switch(message[1]){
            case 0:
                hqLocation = CommunicationHelper.convertMessageToLocation(message[2]);
                hqElevation = message[3];
                break;
            case 1:
                wallLocations.remove(CommunicationHelper.convertMessageToLocation(message[2]));
                break;
            case 2:
                wallLocations.add(CommunicationHelper.convertMessageToLocation(message[2]));
                state = LaticeState.MOVING_TO_WALL;
                break;
            case 3:
                break;
            case 4:
                enemyHQBlacklist.add(CommunicationHelper.convertMessageToLocation(message[2]));
                if(targetLocation.equals(CommunicationHelper.convertMessageToLocation(message[2]))){
                    targetLocation = null;
                }
                break;
            case 5:
                enemyHQ = CommunicationHelper.convertMessageToLocation(message[2]);
                targetLocation = null;
                break;
        }

    }

}
