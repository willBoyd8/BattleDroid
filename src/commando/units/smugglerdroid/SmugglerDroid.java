package commando.units.smugglerdroid;

import battlecode.common.*;
import commando.utility.DebugHelper;
import commando.base.KillMeNowException;
import commando.base.MobileUnit;
import commando.communication.CommunicationHelper;
import commando.pathing.Bug;
import commando.pathing.Simple;
import commando.utility.ActionHelper;
import commando.utility.Constants;
import commando.utility.DroidList;
import commando.utility.Unsorted;

public class SmugglerDroid extends MobileUnit {
    DroidList<MapLocation> knownSoupLocations;
    DroidList<DropOffLocation> depositLocations;
    SmugglerState state;
    SmugglerState previousState;
    Bug path;
    int gridOffsetX, gridOffsetY;
    boolean initialDesignSchool, initialFulfillmentCenter, initialRefinery;
    boolean economyBuilding;
    boolean productionLocked;
    int buildingType;
    boolean rushing;
    RushState rstate;
    boolean netGunBuilt;
    boolean designSchoolBuilt;
    int vaporCount;
    int lockCounter;
    boolean badLock;
    boolean walling;
    DroidList<MapLocation> bases;
    MapLocation baseBuildingLocation;
    BaseState baseState;
    boolean hasAnnouncedBuildingBase;
    int waitCounter, buildRatioCounter;

    public SmugglerDroid(RobotController rc){
        super(rc);
        knownSoupLocations = new DroidList<>();
        depositLocations = new DroidList<>();
        bases = new DroidList<>();
        state = SmugglerState.MINING;
        previousState = null;
        gridOffsetX = 0;
        gridOffsetY = 0;
        initialDesignSchool = false;
        initialFulfillmentCenter = false;
        economyBuilding = true;
        initialRefinery = false;
        productionLocked = false;
        buildingType = rand.nextInt(2) + 1;
        rushing = false;
        vaporCount = 0;
        rstate = RushState.SETUP;
        enemyHQLocations = new DroidList<>();
        badLock = false;
        lockCounter = 0;
        walling = false;
        baseBuildingLocation = null;
        baseState = BaseState.NETGUN_1;
        hasAnnouncedBuildingBase = false;
        waitCounter = 0;
        buildRatioCounter = 1;
    }

    enum SmugglerState {
        MINING,
        DEPOSITING,
        SEARCHING,
        BASE_BUILDING
    }

    enum RushState {
        SETUP,
    }

    enum BaseState {
        NETGUN_1,
        SCHOOL,
        VAPORATOR_1,
        VAPORATOR_2,
        VAPORATOR_3,
        VAPORATOR_4,
        FULFILLMENT

    }

    @Override
    public void onInitialization() throws GameActionException {
        catchup();
        if(hqLocation.x % 2 == 0){
            gridOffsetX = 1;
        }
        if(hqLocation.y % 2 == 0){
            gridOffsetY = 1;
        }

        enemyHQLocations.addAll(Unsorted.generatePossibleEnemyHQLocation(hqLocation, rc));

    }

    public void turn() throws GameActionException, KillMeNowException {

        DebugHelper.setIndicatorDot(rc.getLocation(), 0, 255, 255, rc);

        if(walling && rc.getLocation().isAdjacentTo(hqLocation)){
            throw new KillMeNowException();
        }
        checkMessages();
        if (!rushing) {
            RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
            for (RobotInfo unit : robots) {
                if (unit.getType() == RobotType.HQ) {
                    rushing = true;
                }
            }
        }
        updateDepositLocations();
        updateSoupLocations();

        if (rushing) {
            DebugHelper.setIndicatorDot(rc.getLocation(), 255, 255, 0, rc);
            switch (rstate) {
                case SETUP: setup(); break;
            }
        }

        if(state == SmugglerState.BASE_BUILDING){
            DebugHelper.setIndicatorDot(rc.getLocation(), 255, 255, 255, rc);
            baseBuilding();
            return;
        }

        if(building()){
            DebugHelper.setIndicatorDot(rc.getLocation(), 0, 0, 0, rc);
            return;
        }

        switch (state){
            case MINING:
                DebugHelper.setIndicatorDot(rc.getLocation(), 255, 0, 0, rc);
                mining();
                break;
            case DEPOSITING:
                DebugHelper.setIndicatorDot(rc.getLocation(), 0, 255, 0, rc);
                depositing();
                break;
            case SEARCHING:
                DebugHelper.setIndicatorDot(rc.getLocation(), 0, 0, 255, rc);
                searching();
                break;
        }
    }

    public void baseBuilding() throws GameActionException, KillMeNowException{
        if(bases.size() <= 0){
            targetLocation = null;
            path = null;
            state = previousState;
            previousState = SmugglerState.MINING;
            return;
        }

        if(targetLocation == null){
            targetLocation = bases.get(0).add(Direction.SOUTH);
            path = new Bug(rc.getLocation(), targetLocation, rc);
            baseBuildingLocation = bases.get(0);
        }

        if(!rc.getLocation().equals(targetLocation)){
            path.run(); // This could cause some issues potentially, maybe switch it to on the grid eventually?
            return;
        }

        switch(baseState){
            case NETGUN_1:
                if(!hasAnnouncedBuildingBase){
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 15;
                    message[2] = CommunicationHelper.convertLocationToMessage(bases.get(0));
                    message[3] = rc.getID();
                    messageQueue.add(message);
                    hasAnnouncedBuildingBase = true;
                }
                if(!targetLocation.equals(baseBuildingLocation.add(Direction.SOUTH))){
                    targetLocation = baseBuildingLocation.add(Direction.SOUTH);
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                    baseBuilding();
                    return;
                }
                if(ActionHelper.tryBuild(RobotType.NET_GUN, Direction.NORTH , rc)){
                    baseState = BaseState.SCHOOL;
                    return;
                }
                break;
            case SCHOOL:
                if(!targetLocation.equals(baseBuildingLocation.add(Direction.SOUTH))){
                    targetLocation = baseBuildingLocation.add(Direction.SOUTH);
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                    baseBuilding();
                    return;
                }
                if(ActionHelper.tryBuild(RobotType.DESIGN_SCHOOL, Direction.NORTHEAST , rc)){
                    baseState = BaseState.VAPORATOR_1;
                    return;
                }
                break;
            case VAPORATOR_1:
                if(!targetLocation.equals(baseBuildingLocation.add(Direction.WEST))){
                    targetLocation = baseBuildingLocation.add(Direction.WEST);
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                    baseBuilding();
                    return;
                }
                if(ActionHelper.tryBuild(RobotType.VAPORATOR, Direction.NORTH , rc)){
                    baseState = BaseState.VAPORATOR_2;
                    return;
                }
                break;
            case VAPORATOR_2:
                if(!targetLocation.equals(baseBuildingLocation.add(Direction.NORTH))){
                    targetLocation = baseBuildingLocation.add(Direction.NORTH);
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                    baseBuilding();
                    return;
                }
                if(ActionHelper.tryBuild(RobotType.VAPORATOR, Direction.EAST , rc)){
                    baseState = BaseState.VAPORATOR_3;
                    return;
                }
                break;
            case VAPORATOR_3:
                if(!targetLocation.equals(baseBuildingLocation.add(Direction.WEST))){
                    targetLocation = baseBuildingLocation.add(Direction.WEST);
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                    baseBuilding();
                    return;
                }
                if(ActionHelper.tryBuild(RobotType.VAPORATOR, Direction.NORTHEAST, rc)){
                    baseState = BaseState.VAPORATOR_4;
                    return;
                }
                break;
            case VAPORATOR_4:
                if(!targetLocation.equals(baseBuildingLocation.add(Direction.SOUTH))){
                    targetLocation = baseBuildingLocation.add(Direction.SOUTH);
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                    baseBuilding();
                    return;
                }
                if(ActionHelper.tryBuild(RobotType.VAPORATOR, Direction.NORTHWEST , rc)){
                    baseState = BaseState.FULFILLMENT;
                    return;
                }
                break;
            case FULFILLMENT:
                if(!targetLocation.equals(baseBuildingLocation.add(Direction.SOUTH))){
                    targetLocation = baseBuildingLocation.add(Direction.SOUTH);
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                    baseBuilding();
                    return;
                }
                if(ActionHelper.tryBuild(RobotType.FULFILLMENT_CENTER, Direction.WEST, rc)){
                    baseState = BaseState.VAPORATOR_1;
                    throw new KillMeNowException();
                }
                break;

        }
        return;

    }

    public boolean building() throws GameActionException{
        MapLocation closestDeposit = getClosestMapLocation(depositLocations, rc);
        if((closestDeposit == null || depositLocations.size() <= 0 || rc.getLocation().distanceSquaredTo(closestDeposit) > Constants.MIN_REFINERY_SPREAD_DISTANCE) && isAdjacentToSoup()){
            for(Direction dir : Constants.DIRECTIONS){
                MapLocation loc = rc.getLocation().add(dir);
                if(rc.canBuildRobot(RobotType.REFINERY, dir) && !loc.isAdjacentTo(hqLocation) && (loc.x - gridOffsetX) % 2 == (loc.y - gridOffsetY) % 2){
                    rc.buildRobot(RobotType.REFINERY, dir);
                    int[] message = new int[7];
                    initialRefinery = true;
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 8;
                    message[2] = CommunicationHelper.convertLocationToMessage(loc);
                    message[3] = rc.senseElevation(loc);
                    messageQueue.add(message);

                    if(productionLocked) {
                        lockCounter = 0;
                        productionLocked = false;
//                        int[] message2 = new int[7];
//                        message2[0] = Constants.MESSAGE_KEY;
//                        message2[1] = 11;
//                        message2[2] = 0;
//                        messageQueue.add(message2);
                    }

                    return true;
                }
            }

            if(rc.getTeamSoup() > RobotType.REFINERY.cost) {
                for (Direction dir : Constants.DIRECTIONS) {
                    if (Simple.tryMove(dir, rc)) {
                        // Didn't build but still return true so we dont have to worry
                        // about trying to run things twice.
                        return true;
                    }
                }
            }

        } else if(!initialRefinery){
            for(Direction dir : Constants.DIRECTIONS){
                MapLocation loc = rc.getLocation().add(dir);
                if(rc.canBuildRobot(RobotType.REFINERY, dir) && loc.distanceSquaredTo(hqLocation) > 8 && (loc.x - gridOffsetX) % 2 == (loc.y - gridOffsetY) % 2){
                    rc.buildRobot(RobotType.REFINERY, dir);
                    initialRefinery = true;
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 8;
                    message[2] = CommunicationHelper.convertLocationToMessage(loc);
                    message[3] = rc.senseElevation(loc);
                    messageQueue.add(message);

                    if(productionLocked) {
                        lockCounter = 0;
                        productionLocked = false;
//                        int[] message2 = new int[7];
//                        message2[0] = Constants.MESSAGE_KEY;
//                        message2[1] = 11;
//                        message2[2] = 0;
//                        messageQueue.add(message2);
                    }
                    return true;
                }
            }
            return false;
        }

        if(closestDeposit == null || (!productionLocked && isAdjacentToSoup() && rc.getLocation().distanceSquaredTo(closestDeposit) > Constants.MIN_REFINERY_SPREAD_DISTANCE)){
            productionLocked = true;
//            int[] message = new int[7];
//            message[0] = Constants.MESSAGE_KEY;
//            message[1] = 11;
//            message[2] = 1;
//            messageQueue.add(message);
        }

        if(lockCounter > 100){
            badLock = true;
        }

        if(productionLocked && !badLock){
            lockCounter++;
            return false;
        }

        if(!initialDesignSchool && !economyBuilding){
            for(Direction dir : Constants.DIRECTIONS){
                MapLocation loc = rc.getLocation().add(dir);
                if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, dir) && loc.distanceSquaredTo(hqLocation) > 8 && (loc.x - gridOffsetX) % 2 == 0 && (loc.y - gridOffsetY) % 2 == 0){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, dir);
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 9;
                    message[2] = CommunicationHelper.convertLocationToMessage(loc);
                    message[3] = rc.senseElevation(loc);
                    messageQueue.add(message);
                    return true;
                }
            }
        }

        if(!initialFulfillmentCenter && !economyBuilding){
            for(Direction dir : Constants.DIRECTIONS){
                MapLocation loc = rc.getLocation().add(dir);
                if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, dir) && loc.distanceSquaredTo(hqLocation) > 8 && (loc.x - gridOffsetX) % 2 == 0 && (loc.y - gridOffsetY) % 2 == 0){
                    rc.buildRobot(RobotType.FULFILLMENT_CENTER, dir);
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 10;
                    message[2] = CommunicationHelper.convertLocationToMessage(loc);
                    message[3] = rc.senseElevation(loc);
                    messageQueue.add(message);
                    return true;
                }
            }
        }

        if(economyBuilding) {
            for (Direction dir : Constants.DIRECTIONS) {
                MapLocation loc = rc.getLocation().add(dir);
                if(rc.canSenseLocation(loc)) {
                    int elevation = rc.senseElevation(loc);
                    if (elevation > GameConstants.getWaterLevel(rc.getRoundNum()) + 4 && (loc.x - gridOffsetX) % 2 == 0 && (loc.y - gridOffsetY) % 2 == 0) {
                        if (rc.canBuildRobot(RobotType.VAPORATOR, dir) && loc.distanceSquaredTo(hqLocation) > 8) {
                            rc.buildRobot(RobotType.VAPORATOR, dir);
                            vaporCount++;
                            if(buildRatioCounter <= vaporCount) {
//                            if (vaporCount >= Constants.VAPORATOR_COUNT) {
                                economyBuilding = false;
                                vaporCount = 0;
                                buildRatioCounter++;
                                buildRatioCounter++;
                            }
                            return true;
                        }
                    } else if ((loc.x - gridOffsetX) % 2 == 1 && (loc.y - gridOffsetY) % 2 == 1 && rc.senseNearbyRobots(1, myTeam).length < 1) {
                        if (rc.canBuildRobot(RobotType.VAPORATOR, dir) && loc.distanceSquaredTo(hqLocation) > 8) {
                            rc.buildRobot(RobotType.VAPORATOR, dir);
                            vaporCount++;
                            if (vaporCount >= Constants.VAPORATOR_COUNT) {
                                economyBuilding = false;
                                vaporCount = 0;
                            }
                            return true;
                        }
                    }
                }
            }
        } else {
            if(buildingType == Integer.MIN_VALUE){
                buildingType = rand.nextInt(3);
            }
            switch (buildingType){
                case 0:
                    for (Direction dir : Constants.DIRECTIONS) {
                        MapLocation loc = rc.getLocation().add(dir);
                        if (rc.canBuildRobot(RobotType.NET_GUN, dir) && loc.distanceSquaredTo(hqLocation) > 8 && (loc.x - gridOffsetX) % 2 == 0 && (loc.y - gridOffsetY) % 2 == 0) {
                            rc.buildRobot(RobotType.NET_GUN, dir);
                            economyBuilding = true;
                            buildingType = Integer.MIN_VALUE;
                            return true;
                        }
                    }
                    break;
                case 1:
                    for (Direction dir : Constants.DIRECTIONS) {
                        MapLocation loc = rc.getLocation().add(dir);
                        if (rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, dir) && loc.distanceSquaredTo(hqLocation) > 8 && (loc.x - gridOffsetX) % 2 == 0 && (loc.y - gridOffsetY) % 2 == 0) {
                            rc.buildRobot(RobotType.FULFILLMENT_CENTER, dir);
                            economyBuilding = true;
                            buildingType = Integer.MIN_VALUE;
                            return true;
                        }
                    }
                    break;
                case 2:
                    for (Direction dir : Constants.DIRECTIONS) {
                        MapLocation loc = rc.getLocation().add(dir);
                        if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, dir) && loc.distanceSquaredTo(hqLocation) > 8 && (loc.x - gridOffsetX) % 2 == 0 && (loc.y - gridOffsetY) % 2 == 0) {
                            rc.buildRobot(RobotType.DESIGN_SCHOOL, dir);
                            economyBuilding = true;
                            buildingType = Integer.MIN_VALUE;
                            return true;
                        }
                    }
                    break;
            }
        }

        return false;
    }

    public void searching() throws GameActionException{
        if(knownSoupLocations.size() > 0){
            state = previousState;
            previousState = SmugglerState.SEARCHING;
            targetLocation = null;
            path = null;
            mining();
            return;
        }

        // TODO: stop things here from not being able to path us to things
        while(targetLocation == null || rc.getLocation().equals(targetLocation) || waitCounter > 20){
            targetLocation = new MapLocation(rand.nextInt(rc.getMapWidth()), rand.nextInt(rc.getMapHeight()));
            path = new Bug(rc.getLocation(), targetLocation, rc);
            waitCounter = 0;
        }

        if(rc.isReady() && !path.run()){
            targetLocation = new MapLocation(rand.nextInt(rc.getMapWidth()), rand.nextInt(rc.getMapHeight()));
            path = new Bug(rc.getLocation(), targetLocation, rc);
            path.run();
            return;
        }

        waitCounter++;
    }

    public void depositing() throws GameActionException {
        MapLocation closest = getClosestMapLocation(depositLocations, rc);
        if(closest == null){
            System.out.println("No Available Deposit Location");
            building();
            return;
        } else if(targetLocation == null || !targetLocation.equals(closest)){
            targetLocation = closest;
            path = new Bug(rc.getLocation(), targetLocation, rc);
        }

        if(rc.getSoupCarrying() <= 0){
            state = SmugglerState.MINING;
            previousState = SmugglerState.DEPOSITING;
            targetLocation = null;
            path = null;
            mining();
            return;
        } else {
            if(rc.getLocation().isAdjacentTo(targetLocation)){
                if(ActionHelper.tryDeposit(rc.getLocation().directionTo(targetLocation), rc)){
                    return;
                }
            } else {
                path.run();
                return;
            }
        }
    }

    public void mining() throws GameActionException {
        MapLocation closest = Unsorted.getClosestMapLocation(knownSoupLocations, rc);
        if(closest == null){
            state = SmugglerState.SEARCHING;
            previousState = SmugglerState.MINING;
            targetLocation = null;
            path = null;
            searching();
            return;
        } else if(targetLocation == null || !targetLocation.equals(closest)){
            targetLocation = closest;
            path = new Bug(rc.getLocation(), targetLocation, rc);
        }

        if(rc.getSoupCarrying() >= Constants.MAX_SOUP_TO_CARRY){
            state = SmugglerState.DEPOSITING;
            previousState = SmugglerState.MINING;
            targetLocation = null;
            path = null;
            depositing();
            return;
        } else if(rc.getSoupCarrying() > 0 && rc.getLocation().distanceSquaredTo(targetLocation) > 2){
            state = SmugglerState.DEPOSITING;
            previousState = SmugglerState.MINING;
            targetLocation = null;
            path = null;
            depositing();
            return;
        } else {
            if(rc.getLocation().isAdjacentTo(targetLocation)){
                if(ActionHelper.tryMine(rc.getLocation().directionTo(targetLocation), rc)){
                    return;
                }
            } else {
                path.run();
                return;
            }
        }
    }

    private boolean isAdjacentToSoup() throws GameActionException{
        MapLocation[] soups = null;

        if(rc.canSenseRadiusSquared(2)){
            soups = rc.senseNearbySoup(2);
        }

        if(soups == null || soups.length == 0){
            return false;
        }

        return true;
    }

    private void updateSoupLocations() throws GameActionException {
        MapLocation[] soups = rc.senseNearbySoup();
        for(int i = 0; i < soups.length; i++){
            if(!knownSoupLocations.contains(soups[i])){
                knownSoupLocations.add(soups[i]);
                int[] message = new int[7];
                message[0] = Constants.MESSAGE_KEY;
                message[1] = 3;
                message[2] = CommunicationHelper.convertLocationToMessage(soups[i]);
                messageQueue.add(message);
            }
            if(Clock.getBytecodesLeft() < 6000){
                break;
            }
        }

        for(MapLocation loc : knownSoupLocations){
            if(rc.canSenseLocation(loc) && rc.senseSoup(loc) <= 0){
                if(state == SmugglerState.MINING && targetLocation != null && targetLocation.equals(loc)){
                    targetLocation = null;
                    path = null;
                }

                int[] message = new int[7];
                message[0] = Constants.MESSAGE_KEY;
                message[1] = 7;
                message[2] = CommunicationHelper.convertLocationToMessage(loc);
                messageQueue.add(message);
            }

            if(Clock.getBytecodesLeft() < 3000){
                break;
            }
        }
    }

    private void updateDepositLocations() {
        DroidList<DropOffLocation> toRemove = new DroidList<>();
        for(DropOffLocation loc : depositLocations){
            if(loc.elevation <= GameConstants.getWaterLevel(rc.getRoundNum()) && !loc.loc.equals(hqLocation)){
                toRemove.add(loc);
            }
        }
        depositLocations.removeAll(toRemove);
    }

    public static MapLocation getClosestMapLocation(DroidList<DropOffLocation> locs, RobotController rc){
        MapLocation best = null;
        int closest = Integer.MAX_VALUE;

        for(DropOffLocation dropLoc : locs){
            MapLocation loc = dropLoc.loc;
            if(rc.getLocation().distanceSquaredTo(loc) < closest){
                closest = rc.getLocation().distanceSquaredTo(loc);
                best = loc;
            }
        }

        return best;

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

    public void checkMessages(int roundNum) throws GameActionException{
        Transaction[] transactions = rc.getBlock(roundNum);

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
                depositLocations.add(new DropOffLocation(hqLocation, hqElevation));
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                MapLocation loc = CommunicationHelper.convertMessageToLocation(message[2]);
                if(!knownSoupLocations.contains(loc)){
                    knownSoupLocations.add(loc);
                }
                break;
            case 4:
                break;
            case 5:
                enemyHQ = CommunicationHelper.convertMessageToLocation(message[2]);
                targetLocation = null;
                break;
            case 6:
                if(message[2] == 1) {
                    depositLocations.remove(new DropOffLocation(hqLocation, hqElevation));
                    walling = true;
                }
                if(targetLocation != null && targetLocation.equals(hqLocation)){
                    targetLocation = null;
                    path = null;
                }
                break;
            case 7:
                MapLocation badSouploc = CommunicationHelper.convertMessageToLocation(message[2]);
                knownSoupLocations.remove(badSouploc);
                if(targetLocation != null && targetLocation.equals(badSouploc)){
                    targetLocation = null;
                    path = null;
                }
                break;
            case 8:
                DropOffLocation location = new DropOffLocation(CommunicationHelper.convertMessageToLocation(message[2]), message[3]);
                if(!depositLocations.contains(location)){
                    depositLocations.add(location);
                }
                initialRefinery = true;
                break;
            case 9:
                initialDesignSchool = true;
                break;
            case 10:
                initialFulfillmentCenter = true;
                break;
            case 11:
                if(message[2] == 0){
                    productionLocked = false;
                } else if(message[2] == 1) {
                    productionLocked = true;
                }
                break;
            case 15:
                MapLocation baseLocation = CommunicationHelper.convertMessageToLocation(message[2]);
                if(message[3] == 0 && !bases.contains(baseLocation) && Constants.SECONDARY_BASE_ENABLE){
                    bases.add(baseLocation);
                    targetLocation = null;
                    path = null;
                    previousState = state;
                    state = SmugglerState.BASE_BUILDING;
                } else if(rc.getID() != message[3]){
                    bases.remove(baseLocation);
                    targetLocation = null;
                    path = null;
                    state = previousState;
                    baseBuildingLocation = null;
                    previousState = SmugglerState.BASE_BUILDING;
                } else if (rc.getID() == message[3] && !bases.contains(baseLocation)){
//                    bases.add(baseLocation);
//                    targetLocation = null;
//                    path = null;
//                    previousState = state;
//                    state = SmugglerState.BASE_BUILDING;
                }


        }

    }

    public void setup () throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        enemyHQ = null;

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ){
                enemyHQ = robot.getLocation();
                break;
            }
        }

        if(enemyHQ == null){
            MapLocation closest = Unsorted.getClosestMapLocation(enemyHQLocations, rc);
            if(targetLocation == null || !targetLocation.equals(closest)) {
                targetLocation = closest;
                path = new Bug(rc.getLocation(), targetLocation, rc);
            }
            enemyHQ = targetLocation;
            path.run();
        }

        int dist = rc.getLocation().distanceSquaredTo(enemyHQ);
        if (dist <= 20){

            if (rc.getTeamSoup() >= 250 && !netGunBuilt) {
                if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateLeft())) {
                    rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    netGunBuilt = true;
                } else if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateRight())) {
                    rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    netGunBuilt = true;
                } else if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateRight().rotateRight())) {
                    rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    netGunBuilt = true;
                } else if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateLeft().rotateLeft())) {
                    rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    netGunBuilt = true;
                }

            }
            if (rc.getTeamSoup() >= 150 && !designSchoolBuilt) {
                if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ)) && rc.getLocation().add(rc.getLocation().directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 20){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ));
                    designSchoolBuilt = true;
                }
                else if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateRight()) && rc.getLocation().add(rc.getLocation().directionTo(enemyHQ).rotateRight()).distanceSquaredTo(enemyHQ) <= 20){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateRight());
                    designSchoolBuilt = true;
                }
                else if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateLeft()) && rc.getLocation().add(rc.getLocation().directionTo(enemyHQ).rotateLeft()).distanceSquaredTo(enemyHQ) <= 20){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    designSchoolBuilt = true;
                }
                else if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateLeft().rotateLeft()) && rc.getLocation().add(rc.getLocation().directionTo(enemyHQ).rotateLeft().rotateLeft()).distanceSquaredTo(enemyHQ) <= 20){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    designSchoolBuilt = true;
                }
                else if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateRight().rotateRight()) && rc.getLocation().add(rc.getLocation().directionTo(enemyHQ).rotateRight().rotateRight()).distanceSquaredTo(enemyHQ) <= 20){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    designSchoolBuilt = true;
                }
            }


        }

    }
}
