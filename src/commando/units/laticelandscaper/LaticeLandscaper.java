package commando.units.laticelandscaper;

import battlecode.common.*;
import commando.base.KillMeNowException;
import commando.base.MobileUnit;
import commando.communication.CommunicationHelper;
import commando.pathing.Bug;
import commando.pathing.Simple;
import commando.units.wallsittinglandscaper.WallSittingLandscaper;
import commando.utility.*;

public class LaticeLandscaper extends MobileUnit {

    LaticeState state;
    DroidList<MapLocation> wallLocations;
    int gridOffsetX, gridOffsetY;
    Bug path;
    DroidList<MapLocation> enemyHQBlacklist;
    DroidList<MapLocation> digBlacklist;
    DroidList<MapLocation> baseTiles;
    DroidList<MapLocation> bases;
    MapLocation baseBuildingLocation;

    public LaticeLandscaper(RobotController rc){
        super(rc);
        state = LaticeState.MOVING_TO_WALL;
        wallLocations = new DroidList<>();
        gridOffsetX = 0;
        gridOffsetY = 0;
        path = null;
        enemyHQBlacklist = new DroidList<>();
        digBlacklist = new DroidList<>();
        baseTiles = new DroidList<>();
        bases = new DroidList<>();
        baseBuildingLocation = null;
    }

    enum LaticeState {
        MOVING_TO_WALL,
        EARLY_WALLING,
        LATE_WALLING,
        LATICE_BUILDING,
        MOVING_TO_LATICE_EDGE,
        SECONDARY_WALL,
    }

    @Override
    public void onInitialization() throws GameActionException, KillMeNowException {
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
        bases.add(hqLocation);

        for(MapLocation base : bases){
            if(spawn.add(Direction.WEST).add(Direction.WEST).equals(base) || spawn.add(Direction.NORTHWEST).equals(base)){
                state = LaticeState.SECONDARY_WALL;
                // TODO: do this better;
                WallSittingLandscaper landscaper = new WallSittingLandscaper(rc);
                landscaper.hqLocation = base;
                landscaper.hqElevation = rc.senseElevation(base);
                landscaper.generateHQTiles();
                landscaper.run();
                throw new KillMeNowException();
            }
        }
    }

    public void turn() throws GameActionException {
        checkMessages();
        checkEnemyHQ();

        if(rc.getLocation().isAdjacentTo(hqLocation)){
            state = LaticeState.EARLY_WALLING;
        }

        if(state == LaticeState.EARLY_WALLING && GameConstants.getWaterLevel(rc.getRoundNum()) >= hqElevation - Constants.WALL_SAFETY_BARRIER){
            state = LaticeState.LATE_WALLING;
        } else if(wallLocations.size() <= 0 && state != LaticeState.EARLY_WALLING && state != LaticeState.LATE_WALLING){
            state = LaticeState.LATICE_BUILDING;
        } else if(wallLocations.size() > 0 && !(state == LaticeState.EARLY_WALLING || state == LaticeState.LATE_WALLING || state == LaticeState.MOVING_TO_WALL)){
            state = LaticeState.MOVING_TO_WALL;
        }

        if(rc.getRoundNum() > 500 && wallLocations.size() < 3 && !(state == LaticeState.EARLY_WALLING || state == LaticeState.LATE_WALLING || state == LaticeState.MOVING_TO_LATICE_EDGE || state == LaticeState.SECONDARY_WALL)) {
            state = LaticeState.LATICE_BUILDING;
        }

        if(attacking()){
            return;
        }

        if(wallLocations.size() <= 0 && bases.size() < (rc.getRoundNum() / 500) + 1) {
            checkBase();
        }

        switch(state){
            case MOVING_TO_WALL: DebugHelper.setIndicatorDot(rc.getLocation(), 255, 0, 0, rc); movingToWall(); break;
            case EARLY_WALLING: DebugHelper.setIndicatorDot(rc.getLocation(), 0, 255, 0, rc); earlyWalling(); break;
            case LATE_WALLING: DebugHelper.setIndicatorDot(rc.getLocation(), 0, 0, 255, rc); lateWalling(); break;
            case LATICE_BUILDING: laticeBuilding(); break;
            case MOVING_TO_LATICE_EDGE: movingToLaticeEdge(); break;
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

        MapLocation closest = Unsorted.getClosestMapLocation(wallLocations, rc);

        if(path == null || path.end.equals(closest)) {
            path = new Bug(rc.getLocation(), closest, rc);
        }

        path.run();

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
            if(rc.getLocation().isAdjacentTo(hqLocation) && rc.canDigDirt(rc.getLocation().directionTo(hqLocation))){
                rc.digDirt(rc.getLocation().directionTo(hqLocation));
                return;
            }

            for (Direction dir : Constants.DIRECTIONS) {
                MapLocation loc = rc.getLocation().add(dir);
                if (rc.onTheMap(loc) && !loc.isAdjacentTo(hqLocation) && (loc.x - gridOffsetX) % 2 == 1 && (loc.y - gridOffsetY) % 2 == 1) {
                    ActionHelper.tryDig(dir, rc);
                }
            }
        } else {
            RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

            if(robots != null && robots.length > 0){
                for(RobotInfo robot : robots){
                    if(ActionHelper.isBuilding(robot) && rc.getLocation().isAdjacentTo(robot.getLocation())){
                        if(ActionHelper.tryDepositDirt(rc.getLocation().directionTo(robot.getLocation()), rc)){
                            return;
                        }
                    }
                }
            }


            ActionHelper.tryDepositDirt(Direction.CENTER, rc);
        }
    }

    /**
     * Method for walling really late into the game. This isn't any different, except that it tries to average the wall height
     * @throws GameActionException
     */
    public void lateWalling() throws GameActionException{
        if(rc.getDirtCarrying() <= 0) {
            if(rc.getLocation().isAdjacentTo(hqLocation) && rc.canDigDirt(rc.getLocation().directionTo(hqLocation))){
                rc.digDirt(rc.getLocation().directionTo(hqLocation));
                return;
            }

            for (Direction dir : Constants.DIRECTIONS) {
                MapLocation loc = rc.getLocation().add(dir);
                if (rc.onTheMap(loc) && !loc.isAdjacentTo(hqLocation) && (loc.x -gridOffsetX) % 2 == 1 && (loc.y -gridOffsetY) % 2 == 1) {
                    if(ActionHelper.tryDig(dir, rc)){
                        break;
                    }
                }
            }
        } else {
            RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

            if(robots != null && robots.length > 0){
                for(RobotInfo robot : robots){
                    if(ActionHelper.isBuilding(robot) && rc.getLocation().isAdjacentTo(robot.getLocation())){
                        if(ActionHelper.tryDepositDirt(rc.getLocation().directionTo(robot.getLocation()), rc)){
                            return;
                        }
                    }
                }
            }

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
                if (!digBlacklist.contains(loc) && rc.onTheMap(loc) && !loc.isAdjacentTo(hqLocation) && (loc.x - gridOffsetX) % 2 == 1 && (loc.y - gridOffsetY) % 2 == 1) {
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
                try {
                    targetLocation = enemyHQLocations.get(rand.nextInt(enemyHQLocations.size()));
                } catch (IllegalArgumentException e){
                    targetLocation = new MapLocation(rand.nextInt(rc.getMapWidth()), rand.nextInt(rc.getMapHeight()));
                }
                path = new Bug(rc.getLocation(), targetLocation, rc);
            }
        }

        path.run(gridOffsetX, gridOffsetY);
    }

    public boolean checkBase() throws GameActionException {

        for(Direction dir : Direction.allDirections()) {

            if (isGoodBaseLocation(rc.getLocation().add(dir))) {
                int[] message = new int[7];
                message[0] = Constants.MESSAGE_KEY;
                message[1] = 15;
                message[2] = CommunicationHelper.convertLocationToMessage(rc.getLocation().add(dir));
                messageQueue.add(message);

                bases.add(rc.getLocation());

                return true;
            }
        }

        return false;

    }

    private boolean isGoodBaseLocation(MapLocation loc){
//        boolean good = true;

        if((loc.x - gridOffsetX) % 2 == 0 && (loc.y - gridOffsetY) % 2 == 0){
            for(MapLocation base : bases){
                if(rc.getLocation().distanceSquaredTo(base) < Constants.SECONDARY_BASE_MIN_SPREAD_DISTANCE){
                    return false;
                }
            }

            DroidList<MapLocation> possibleTiles = new DroidList<>();
            possibleTiles.add(rc.getLocation());
            possibleTiles.addAll(Unsorted.getTilesAtSquareRadius(rc.getLocation(), 1, rc));
            possibleTiles.addAll(Unsorted.getTilesAtSquareRadius(rc.getLocation(), 2, rc));


            try{
                for(MapLocation tile : possibleTiles){
                    // TODO: conditions here can be more precise.
                    if(((loc.x - gridOffsetX) % 2 == 0 || (loc.y - gridOffsetY) % 2 == 0) && rc.senseFlooding(tile)){
                        return false;
                    }
                    if(!rc.onTheMap(tile)){
                        return false;
                    }
                }
            } catch (GameActionException e){
                return false;
            }
            return true;

        } else {

            return false;

        }



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

                boolean baseCheck = false;

                for(MapLocation base : bases){
                    if(loc.distanceSquaredTo(base) < 16){
                        baseCheck = true;
                        break;
                    }
                }
                // TODO: we probably aren't handling all the possible cases here
                // height < Constants.LATICE_HEIGHT for a static height
                // use round number and water elevation for a less static grid
                if(!baseCheck && !isOccupied && height >= Constants.MIN_LATICE_BUILDING_ELEVATION && (height < GameConstants.getWaterLevel(rc.getRoundNum()) + 4 || height < 5) && ((loc.x - gridOffsetX) % 2 == 0 || (loc.y - gridOffsetY) % 2 == 0)){
                    adjacent.add(loc);
                }
            }
        }

        return adjacent;
    }

    public boolean attacking() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        if(robots == null || robots.length <= 0){
            return false;
        }

        RobotInfo bestTarget = null;
        int closest = Integer.MAX_VALUE;

        for(RobotInfo robot : robots){
            int dist = rc.getLocation().distanceSquaredTo(robot.getLocation());

            if(robot.getType() == RobotType.DELIVERY_DRONE){

            } else if(rc.canSenseLocation(robot.getLocation()) && (rc.senseElevation(rc.getLocation()) > rc.senseElevation(robot.getLocation()) + 10)){

            } else if(dist < closest){


                bestTarget = robot;
                closest = dist;
            }

            if(ActionHelper.isBuilding(robot) && !digBlacklist.contains(robot.getLocation())){
                digBlacklist.add(robot.getLocation());
                bestTarget = robot;
                break;
            }
        }

        if(bestTarget != null && rc.getLocation().isAdjacentTo(bestTarget.getLocation())){
            if(ActionHelper.isBuilding(bestTarget)){
                if(rc.getDirtCarrying() > 0){
                    return ActionHelper.tryDepositDirt(rc.getLocation().directionTo(bestTarget.getLocation()), rc);
                } else {
                    for(Direction dir : Constants.DIRECTIONS){
                        if(!digBlacklist.contains(rc.getLocation().add(dir)) && ActionHelper.tryDig(dir, rc)){
                            return true;
                        }
                    }
                }
            } else {
                if(rc.getDirtCarrying() < RobotType.LANDSCAPER.dirtLimit){
                    return ActionHelper.tryDig(rc.getLocation().directionTo(bestTarget.getLocation()), rc);
                } else {
                    // TODO: this and the part above might be able to be more intelligent about where they are placing and digging dirt
                    for(Direction dir : Constants.DIRECTIONS){
                        if(ActionHelper.tryDepositDirt(dir, rc)){
                            return true;
                        }
                    }
                }
            }
        } else {
            if(bestTarget == null){
                return false;
            }

            if(Simple.moveToLocationFuzzy(bestTarget.getLocation(), rc)){
                return true;
            } else if (rc.isReady()){
                MapLocation loc = rc.getLocation().add(rc.getLocation().directionTo(bestTarget.getLocation()));
                int height = Integer.MIN_VALUE;
                if(rc.canSenseLocation(loc)){
                    height = rc.senseElevation(loc);
                }

                if(rc.senseElevation(rc.getLocation()) < height && !digBlacklist.contains(loc)){
                    return ActionHelper.tryDig(rc.getLocation().directionTo(loc), rc);
                } else if (rc.senseElevation(rc.getLocation()) > height){
                    if (rc.getDirtCarrying() > 0) {
                        return ActionHelper.tryDepositDirt(rc.getLocation().directionTo(loc), rc);
                    } else {
                        for (Direction dir : Constants.DIRECTIONS) {
                            MapLocation dirLoc = rc.getLocation().add(dir);
                            if (rc.onTheMap(dirLoc) && !dirLoc.equals(loc) && !dirLoc.isAdjacentTo(hqLocation)/* && (dirLoc.x - gridOffsetX) % 2 == 1 && (dirLoc.y - gridOffsetY) % 2 == 1*/) {
                                if (ActionHelper.tryDig(dir, rc)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;

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
                bases.add(hqLocation);
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
                if(targetLocation != null && targetLocation.equals(CommunicationHelper.convertMessageToLocation(message[2]))){
                    targetLocation = null;
                }
                break;
            case 5:
                enemyHQ = CommunicationHelper.convertMessageToLocation(message[2]);
                targetLocation = null;
                break;
            case 15:
                MapLocation loc = CommunicationHelper.convertMessageToLocation(message[2]);
                if(!bases.contains(loc)){
                    bases.add(loc);
                }

                for(int x = -3; x <= 3; x++){
                    for(int y = -3; y <= 3; y++){
                        digBlacklist.add(new MapLocation(loc.x + x, loc.y + y));
                    }
                }
                break;
        }

    }

}
