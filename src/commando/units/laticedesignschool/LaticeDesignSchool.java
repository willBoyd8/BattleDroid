package commando.units.laticedesignschool;

import battlecode.common.*;
import commando.base.Building;
import commando.communication.CommunicationHelper;
import commando.units.smugglerdroid.DropOffLocation;
import commando.utility.Constants;
import commando.utility.DroidList;

public class LaticeDesignSchool extends Building {
    DroidList<DropOffLocation> depositLocations;
    DroidList<MapLocation> knownSoupLocations;
    int gridOffsetX, gridOffsetY;
    int waitTime;
    boolean hasWaited;


    public LaticeDesignSchool(RobotController rc){
        super(rc);
        depositLocations = new DroidList<>();
        knownSoupLocations = new DroidList<>();
        gridOffsetX = 0;
        gridOffsetY = 0;
        waitTime = 0;
        hasWaited = false;
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
        updateDepositLocations();

//        if(waitTime > 300){
//            hasWaited = true;
//        }
//
//        if(depositLocations.size() <= 0 && knownSoupLocations.size() > 0 && !hasWaited){
//            waitTime++;
//            return;
//        } else {
//            waitTime = 0;
            for(Direction dir : Constants.DIRECTIONS){
                if(isOnLatice(rc.getLocation().add(dir)) && rc.canBuildRobot(RobotType.LANDSCAPER, dir)){
                    rc.buildRobot(RobotType.LANDSCAPER, dir);
                }
            }
//        }
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

    public boolean isOnLatice(MapLocation loc){
        return (loc.x - gridOffsetX) % 2 == 0 || (loc.y - gridOffsetY) % 2 == 0;
    }

    public void catchup() throws GameActionException {
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
                break;
            case 6:
                if(message[2] == 1) {
                    depositLocations.remove(new DropOffLocation(hqLocation, hqElevation));
                }
                break;
            case 7:
                MapLocation badSouploc = CommunicationHelper.convertMessageToLocation(message[2]);
                knownSoupLocations.remove(badSouploc);
                break;
            case 8:
                DropOffLocation location = new DropOffLocation(CommunicationHelper.convertMessageToLocation(message[2]), message[3]);
                if(!depositLocations.contains(location)){
                    depositLocations.add(location);
                }
                break;
            case 9:
                break;
            case 10:
                break;

        }

    }
}
