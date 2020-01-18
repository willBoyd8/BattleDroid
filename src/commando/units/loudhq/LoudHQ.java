package commando.units.loudhq;

import battlecode.common.*;
import commando.base.Building;
import commando.communication.CommunicationHelper;
import commando.utility.ActionHelper;
import commando.utility.Constants;
import commando.utility.DroidList;

import java.util.Map;

public class LoudHQ extends Building {
    DroidList<MapLocation> desiredWallLocations;
    DroidList<MapLocation> occupiedWallLocations;
    int minerCounter;
    boolean hasAnnouncedBlocked;

    public LoudHQ(RobotController rc) throws GameActionException{
        super(rc);
        occupiedWallLocations = ActionHelper.generateAdjacentTiles(rc.getLocation(), rc);
        desiredWallLocations = new DroidList<>();
        minerCounter = 0;
        hqLocation = rc.getLocation();
        hasAnnouncedBlocked = false;
    }

    @Override
    public void onInitialization() throws GameActionException {
        int[] message = new int[7];
        message[0] = Constants.MESSAGE_KEY;
        message[1] = 0;
        message[2] = CommunicationHelper.convertLocationToMessage(rc.getLocation());
        hqElevation = rc.senseElevation(rc.getLocation());
        message[3] = hqElevation;
        rc.submitTransaction(message, 3);
    }

    public void turn() throws GameActionException {
        checkWall();
        ActionHelper.tryShoot(rc);
        checkBlocked();

        // TODO: implement better logic for building
        for(Direction dir : Constants.DIRECTIONS) {
            if(minerCounter < 5 && ActionHelper.tryBuild(RobotType.MINER, dir, rc)){
                minerCounter++;
                break;
            }
        }

    }

    public void checkWall() throws GameActionException {
        DroidList<MapLocation> toRemove = new DroidList<>();
        for(MapLocation loc : desiredWallLocations){
            if(rc.canSenseLocation(loc)){
                RobotInfo robot = rc.senseRobotAtLocation(loc);
                if(robot != null && robot.getTeam() == myTeam &&  robot.getType() == RobotType.LANDSCAPER){
                    toRemove.add(loc);
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 1;
                    message[2] = CommunicationHelper.convertLocationToMessage(loc);
                    messageQueue.add(message);
                }
            }
        }
        desiredWallLocations.removeAll(toRemove);
        occupiedWallLocations.addAll(toRemove);
        toRemove.clear();

        for(MapLocation loc : occupiedWallLocations){
            if(rc.canSenseLocation(loc)){
                RobotInfo robot = rc.senseRobotAtLocation(loc);
                if(robot == null){
                    toRemove.add(loc);
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 2;
                    message[2] = CommunicationHelper.convertLocationToMessage(loc);
                    messageQueue.add(message);
                }
            }
        }
        occupiedWallLocations.removeAll(toRemove);
        desiredWallLocations.addAll(toRemove);
        toRemove.clear();

    }

    public void checkBlocked() throws GameActionException{
        if(!hasAnnouncedBlocked){
            for(Direction dir : Constants.DIRECTIONS){
                MapLocation loc = rc.getLocation().add(dir);
                if(rc.canSenseLocation(loc) && Math.abs(rc.senseElevation(loc) - hqElevation) <= GameConstants.MAX_DIRT_DIFFERENCE){
                    return;
                }
            }
        } else {
            return;
        }

        hasAnnouncedBlocked = true;
        int[] message = new int[7];
        message[0] = Constants.MESSAGE_KEY;
        message[1] = 6;
        message[2] = 1;
        messageQueue.add(message);
    }
}
