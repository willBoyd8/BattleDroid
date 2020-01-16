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

    public LoudHQ(RobotController rc) throws GameActionException{
        super(rc);
        desiredWallLocations = ActionHelper.generateAdjacentTiles(rc.getLocation(), rc);
        occupiedWallLocations = new DroidList<>();
        minerCounter = 0;
        hqLocation = rc.getLocation();
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
                    rc.submitTransaction(message, 3);
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
                    rc.submitTransaction(message, 3);
                }
            }
        }
        occupiedWallLocations.removeAll(toRemove);
        desiredWallLocations.addAll(toRemove);
        toRemove.clear();
    }
}
