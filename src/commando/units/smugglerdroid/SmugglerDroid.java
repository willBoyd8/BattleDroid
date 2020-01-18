package commando.units.smugglerdroid;

import battlecode.common.*;
import c3po.units.miningminer.MiningMiner;
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

    public SmugglerDroid(RobotController rc){
        super(rc);
        knownSoupLocations = new DroidList<>();
        depositLocations = new DroidList<>();
        state = SmugglerState.MINING;
        previousState = null;
        gridOffsetX = 0;
        gridOffsetY = 0;
    }

    enum SmugglerState {
        MINING,
        DEPOSITING,
        SEARCHING
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

    }

    public void turn() throws GameActionException {
        checkMessages();
        updateDepositLocations();
        updateSoupLocations();
        if(building()){
            return;
        }

        switch (state){
            case MINING: mining(); break;
            case DEPOSITING: depositing(); break;
            case SEARCHING: searching(); break;
        }
    }

    public boolean building() throws GameActionException{
        MapLocation closestDeposit = getClosestMapLocation(depositLocations, rc);
        if((closestDeposit == null || rc.getLocation().distanceSquaredTo(closestDeposit) > Constants.MIN_REFINERY_SPREAD_DISTANCE) && isAdjacentToSoup()){
            for(Direction dir : Constants.DIRECTIONS){
                MapLocation loc = rc.getLocation().add(dir);
                if(rc.canBuildRobot(RobotType.REFINERY, dir) && (loc.x - gridOffsetX) % 2 == 1 && (loc.y - gridOffsetY) % 2 == 1){
                    rc.buildRobot(RobotType.REFINERY, dir);
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 8;
                    message[2] = CommunicationHelper.convertLocationToMessage(loc);
                    message[3] = rc.senseElevation(loc);
                    messageQueue.add(message);
                    return true;
                }
            }
        }
        return false;
    }

    public void searching() throws GameActionException{
        if(knownSoupLocations.size() > 0){
            state = previousState;
            previousState = SmugglerState.SEARCHING;
            // TODO: This is broken because I'm only halfway done
            targetLocation = null;
            path = null;
            mining();
            return;
        }

        while(targetLocation == null || rc.getLocation().equals(targetLocation)){
            targetLocation = new MapLocation(rand.nextInt(rc.getMapWidth()), rand.nextInt(rc.getMapHeight()));
            path = new Bug(rc.getLocation(), targetLocation, rc);
        }

        if(rc.isReady() && !path.run()){
            targetLocation = new MapLocation(rand.nextInt(rc.getMapWidth()), rand.nextInt(rc.getMapHeight()));
            path = new Bug(rc.getLocation(), targetLocation, rc);
            path.run();
            return;
        }
    }

    public void depositing() throws GameActionException {
        MapLocation closest = getClosestMapLocation(depositLocations, rc);
        if(closest == null){

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
        for(MapLocation loc : soups){
            if(!knownSoupLocations.contains(loc)){
                knownSoupLocations.add(loc);
                int[] message = new int[7];
                message[0] = Constants.MESSAGE_KEY;
                message[1] = 3;
                message[2] = CommunicationHelper.convertLocationToMessage(loc);
                messageQueue.add(message);
            }
            if(Clock.getBytecodesLeft() < 6000){
                break;
            }
        }

        for(MapLocation loc :knownSoupLocations){
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
            if(loc.elevation <= GameConstants.getWaterLevel(rc.getRoundNum()) && !loc.equals(hqLocation)){
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
                }
                if(targetLocation.equals(hqLocation)){
                    targetLocation = null;
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
                break;

        }

    }
}
