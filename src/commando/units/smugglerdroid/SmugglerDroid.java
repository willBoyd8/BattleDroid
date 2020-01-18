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

    public SmugglerDroid(RobotController rc){
        super(rc);
        knownSoupLocations = new DroidList<>();
        state = SmugglerState.MINING;
        previousState = null;
    }

    enum SmugglerState {
        BUILDING,
        MINING,
        DEPOSITING,
        SEARCHING
    }

    public void turn() throws GameActionException {
        checkMessages();
        updateDepositLocations();
        updateSoupLocations();

        switch (state){
            case MINING: mining(); break;
            case BUILDING: building(); break;
            case DEPOSITING: depositing(); break;
            case SEARCHING: searching(); break;
        }
    }

    public void building() throws GameActionException{

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

        Simple.tryMove(rc);
    }

    public void depositing() throws GameActionException {
        MapLocation closest = getClosestMapLocation(depositLocations, rc);
        if(closest == null){

        } else if(!targetLocation.equals(closest)){
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
            path.run();
        }
    }

    public void mining() throws GameActionException {
        MapLocation closest = Unsorted.getClosestMapLocation(knownSoupLocations, rc);
        if(closest == null){
            state = SmugglerState.SEARCHING;
            previousState = SmugglerState.MINING;
            targetLocation = null;
            searching();
            return;
        } else if(!targetLocation.equals(closest)){
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
        }

        for(MapLocation loc :knownSoupLocations){
            if(rc.canSenseLocation(loc) && rc.senseSoup(loc) <= 0){
                if(targetLocation.equals(loc)){
                    targetLocation = null;
                }

                int[] message = new int[7];
                message[0] = Constants.MESSAGE_KEY;
                message[1] = 7;
                message[2] = CommunicationHelper.convertLocationToMessage(loc);
                messageQueue.add(message);
            }
        }
    }

    private void updateDepositLocations() {
        DroidList<DropOffLocation> toRemove = new DroidList<>();
        for(DropOffLocation loc : depositLocations){
            if(loc.elevation >= GameConstants.getWaterLevel(rc.getRoundNum())){
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
                MapLocation loc = new MapLocation(message[1], message[2]);
                if(!knownSoupLocations.contains(loc)){
                    knownSoupLocations.add(loc);
                }
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
                knownSoupLocations.remove(CommunicationHelper.convertMessageToLocation(message[2]));
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
