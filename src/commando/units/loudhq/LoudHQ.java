package commando.units.loudhq;

import battlecode.common.*;
import commando.base.Building;
import commando.communication.CommunicationHelper;
import commando.utility.*;

import java.util.Map;

public class LoudHQ extends Building {
    DroidList<MapLocation> desiredWallLocations;
    DroidList<MapLocation> occupiedWallLocations;
    DroidList<MapLocation> desiredDroneDefenseLocations;
    DroidList<MapLocation> occupiedDroneDefenseLocations;
    DroidList<MapLocation> secondaryWallLocations;
    DroidList<MapLocation> occupiedSecondaryWallLocations;
    int minerCounter;
    boolean hasAnnouncedBlocked, hasAnnouncedFlooded, hasAnnouncedSecondary;

    public LoudHQ(RobotController rc) throws GameActionException{
        super(rc);
        occupiedWallLocations = ActionHelper.generateAdjacentTiles(rc.getLocation(), rc);
        desiredWallLocations = new DroidList<>();
        occupiedDroneDefenseLocations = Unsorted.getTilesAtSquareRadius(rc.getLocation(), 2, rc);
        desiredDroneDefenseLocations = new DroidList<>();
        occupiedSecondaryWallLocations = Unsorted.getTilesAtSquareRadius(rc.getLocation(), 2, rc);
        secondaryWallLocations = new DroidList<>();
        minerCounter = 0;
        hqLocation = rc.getLocation();
        for(Direction dir : Direction.cardinalDirections()){
            occupiedSecondaryWallLocations.remove(hqLocation.add(dir).add(dir));
        }
        hasAnnouncedBlocked = false;
        hasAnnouncedFlooded = false;
        hasAnnouncedSecondary = false;
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
        checkDroneGrid();
    }

    public void turn() throws GameActionException {
        checkWall();
        checkFlooded();
        if(hasAnnouncedFlooded) {
            checkDroneGrid();
        }
        ActionHelper.tryShoot(rc);
        checkBlocked();

        // TODO: implement better logic for building
        for(Direction dir : Constants.DIRECTIONS) {
            if(minerCounter < Constants.NUMBER_OF_MINERS_TO_BUILD && ActionHelper.tryBuild(RobotType.MINER, dir, rc)){
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

        if(!hasAnnouncedSecondary && desiredWallLocations.size() <= 0 && rc.getRoundNum() > Constants.SECONDARY_WALL_START_ROUND){
            for(MapLocation loc : occupiedSecondaryWallLocations){
                int[] secondaryWallMessage = new int[7];
                secondaryWallMessage[0] = Constants.MESSAGE_KEY;
                secondaryWallMessage[1] = 2;
                secondaryWallMessage[2] = CommunicationHelper.convertLocationToMessage(loc);
                messageQueue.add(secondaryWallMessage);
                hasAnnouncedSecondary = true;
            }
        }

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

    public void checkFlooded() throws GameActionException {
        DroidList<MapLocation> tiles = Unsorted.getTilesAtSquareRadius(rc.getLocation(), 2, rc);

        boolean flooded = true;

        for(MapLocation loc : tiles){
            DebugHelper.setIndicatorDot(loc, 255, 255, 255, rc);
            if(rc.canSenseLocation(loc)){
                if(!rc.senseFlooding(loc)){
                    flooded = false;
                    break;
                }
            } else {
                flooded = false;
                break;
            }
        }

        if(flooded && !hasAnnouncedFlooded && rc.getRoundNum() > Constants.SECONDARY_WALL_START_ROUND + 100){
            int[] message = new int[7];
            message[0] = Constants.MESSAGE_KEY;
            message[1] = 6;
            message[2] = 2;
            if(rc.canSubmitTransaction(message, rc.getTeamSoup())){
                hasAnnouncedFlooded = true;
                rc.submitTransaction(message, rc.getTeamSoup());
            }
        }
    }

    public void checkDroneGrid() throws GameActionException {
        DroidList<MapLocation> toRemove = new DroidList<>();
        for(MapLocation loc : desiredDroneDefenseLocations){
            if(rc.canSenseLocation(loc)){
                RobotInfo robot = rc.senseRobotAtLocation(loc);
                if(robot != null && robot.getTeam() == myTeam &&  robot.getType() == RobotType.DELIVERY_DRONE){
                    toRemove.add(loc);
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 12;
                    message[2] = CommunicationHelper.convertLocationToMessage(loc);
                    messageQueue.add(message);
                }
            }
        }
        desiredDroneDefenseLocations.removeAll(toRemove);
        occupiedDroneDefenseLocations.addAll(toRemove);
        toRemove.clear();

        for(MapLocation loc : occupiedDroneDefenseLocations){
            if(rc.canSenseLocation(loc)){
                RobotInfo robot = rc.senseRobotAtLocation(loc);
                if(robot == null || robot.getType() != RobotType.DELIVERY_DRONE){
                    toRemove.add(loc);
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 13;
                    message[2] = CommunicationHelper.convertLocationToMessage(loc);
                    messageQueue.add(message);
                }
            }
        }
        occupiedDroneDefenseLocations.removeAll(toRemove);
        desiredDroneDefenseLocations.addAll(toRemove);
        toRemove.clear();
    }
}
