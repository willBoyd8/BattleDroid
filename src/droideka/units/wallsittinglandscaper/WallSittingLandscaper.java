package droideka.units.wallsittinglandscaper;

import battlecode.common.*;
import droideka.base.KillMeNowException;
import droideka.base.MobileUnit;
import droideka.pathing.Simple;
import droideka.utility.ActionHelper;
import droideka.utility.Constants;
import droideka.utility.Unsorted;
import org.mockito.internal.matchers.Null;

import java.util.ArrayList;

public class WallSittingLandscaper extends MobileUnit {
    ArrayList<Direction> path;
    boolean moving;
    boolean hasResetPath;
    int totalMoves;
    public static int maxMoves = Integer.MAX_VALUE;
    ArrayList<MapLocation> hqTiles;
    LandscaperState state;

    enum LandscaperState {
        WALLING,
        RAIDING
    }

    public WallSittingLandscaper(RobotController rc){
        super(rc);
        path = new ArrayList<Direction>();
        path.add(Direction.SOUTH);
        path.add(Direction.SOUTH);
        path.add(Direction.WEST);
        path.add(Direction.WEST);
        path.add(Direction.WEST);
        path.add(Direction.WEST);
        path.add(Direction.NORTH);
        path.add(Direction.NORTH);
        path.add(Direction.NORTH);
        path.add(Direction.NORTH);
        path.add(Direction.EAST);
        path.add(Direction.EAST);
        path.add(Direction.EAST);
        path.add(Direction.EAST);
        path.add(Direction.SOUTH);
        path.add(Direction.SOUTH);
        moving = false;
        hasResetPath = false;
        totalMoves = 0;
        hqTiles = new ArrayList<MapLocation>();
        state = LandscaperState.WALLING;

        generateHQTiles();

    }

    public void turn() throws GameActionException, KillMeNowException {

        if(rc.getLocation().distanceSquaredTo(hqLocation) > 8){
            state = LandscaperState.RAIDING;
        }

        if(state == LandscaperState.RAIDING){
            raid();
            return;
        }

        if(!hasResetPath && !(spawn.directionTo(hqLocation) == Direction.WEST)) {
        //if(rc.getLocation().add(Direction.NORTHWEST).equals(hqLocation) || rc.getLocation().add(Direction.NORTH).equals(hqLocation)){
            path.clear();
            path.add(Direction.WEST);
            path.add(Direction.WEST);
            path.add(Direction.NORTH);
            path.add(Direction.NORTH);
            path.add(Direction.NORTH);
            path.add(Direction.NORTH);
            path.add(Direction.EAST);
            path.add(Direction.EAST);
            path.add(Direction.EAST);
            path.add(Direction.EAST);
            path.add(Direction.SOUTH);
            path.add(Direction.SOUTH);
            path.add(Direction.SOUTH);
            path.add(Direction.SOUTH);
            path.add(Direction.WEST);
            path.add(Direction.WEST);
            hasResetPath = true;

        }

        if(hqLocation == null){
            throw new KillMeNowException();
        }

        if (Math.abs((rc.getLocation().x)-hqLocation.x) == 2 || Math.abs((rc.getLocation().y)-hqLocation.y) == 2){

        } else {
            return;
        }

        if(Unsorted.getNumberOfNearbyFriendlyUnitType(RobotType.LANDSCAPER, rc) > Constants.LANDSCAPERS_ON_WALL - 3){
            averageDig();
            return;
        }

        for(MapLocation loc : hqTiles){
            if(loc.isAdjacentTo(rc.getLocation())){
                if(rc.senseElevation(loc) < hqElevation && rc.senseFlooding(loc)){
                    if(rc.getDirtCarrying() > 0){
                        tryDeposit(rc.getLocation().directionTo(loc));
                        return;
                    } else {
                        tryDig(path.get(0).rotateLeft());
                        return;
                    }
                }
            }
        }





        if(moving && totalMoves < maxMoves){
            if(Unsorted.getNumberOfNearbyFriendlyUnitType(RobotType.LANDSCAPER, rc) < Constants.LANDSCAPERS_ON_WALL - 4) {
                handleRotate();
            } else if(rc.getLocation().add(Direction.NORTH).add(Direction.NORTHWEST) != hqLocation){
                handleRotate();
            } else {

            }
        }

        if(rc.getDirtCarrying() > 0){
            if(tryDeposit(Direction.CENTER)){
                moving = true;
            }
        } else {
            tryDig(path.get(0).rotateLeft());
        }
    }

    public boolean tryDig(Direction dir) throws GameActionException {
        if(rc.isReady() && rc.canDigDirt(dir)){
            rc.digDirt(dir);
            return true;
        }
        return false;
    }

    public boolean tryDeposit(Direction dir) throws GameActionException {
        if(rc.isReady() && rc.canDepositDirt(dir)){
            rc.depositDirt(dir);
            return true;
        }
        return false;
    }

    public void generateHQTiles(){
        try {
            for (Direction dir : Constants.DIRECTIONS) {
                hqTiles.add(hqLocation.add(dir));
            }
        } catch (NullPointerException e) {

        }
    }

    public void handleRotate() throws GameActionException{
        if (Simple.tryMove(path.get(0), rc)) {
            moving = false;
            path.add(path.get(0));
            path.remove(0);
            totalMoves++;

        } else {
            if ((rc.senseElevation(rc.getLocation().add(path.get(0))) < rc.senseElevation(rc.getLocation())) && rc.senseRobotAtLocation(rc.getLocation().add(path.get(0))) == null) {
                if (rc.getDirtCarrying() > 0) {
                    tryDeposit(path.get(0));
                } else {
                    tryDig(path.get(0).rotateLeft());
                }
            } else {
                if (rc.getDirtCarrying() > 0) {
                    tryDeposit(Direction.CENTER);
                } else {
                    tryDig(path.get(0).rotateLeft());
                }
            }
        }
    }

    public void averageDig() throws GameActionException {

        if (!rc.getLocation().add(Direction.NORTH).add(Direction.NORTHWEST).equals(hqLocation) && Simple.tryMove(path.get(0), rc)) {
            moving = false;
            path.add(path.get(0));
            path.remove(0);
            totalMoves++;

        }


        if(rc.getDirtCarrying() > 0) {

            int lowest = Integer.MAX_VALUE;
            MapLocation place = null;

            for (Direction dir : Direction.allDirections()) {
                MapLocation tile = rc.getLocation().add(dir);
                // TODO: remove this when we handle square more programatically or make it larger
                if (tile.distanceSquaredTo(hqLocation) <= 8 && tile.distanceSquaredTo(hqLocation) >= 4) {
                    if (rc.senseElevation(tile) < lowest) {
                        lowest = rc.senseElevation(tile);
                        place = tile;
                    }
                }
            }

            if(tryDeposit(rc.getLocation().directionTo(place))) {
                return;
            }


        }

        tryDig(path.get(0).rotateLeft());


    }

    public void raid() throws GameActionException {
        if(rc.getDirtCarrying() > 0){
            handleDirtDepositAttack();
        } else {
            handleDirtDigAttack();
        }
    }

    public void handleDirtDigAttack() throws GameActionException {
        Direction best = Direction.CENTER;
        int lowest = Integer.MAX_VALUE;

        for(Direction dir : Direction.allDirections()){
            int elev = rc.senseElevation(rc.getLocation().add(dir));
            RobotInfo possibleBot = rc.senseRobotAtLocation(rc.getLocation().add(dir));
            if(teamworkTile(dir) && !rc.senseFlooding(rc.getLocation().add(dir)) && isAdjacentToWater(rc.getLocation().add(dir))){
                best = dir;
                lowest = elev;
            }
        }

        tryDig(best);
    }

    public boolean digUnderEnemy(RobotInfo robot){
        if(robot == null){
            return true;
        }

        RobotType type = robot.getType();

        if(type == RobotType.MINER || type == RobotType.LANDSCAPER || type == RobotType.COW || type == RobotType.DELIVERY_DRONE){
            return true;
        }

        return false;
    }

    public boolean teamworkTile(Direction dir) throws GameActionException {
        RobotInfo robots[] = rc.senseNearbyRobots(8, myTeam);
        for (int i = 0; i < robots.length; i++){
            //Conditions for teamworkTile is it's adjacent to a friendly landscaper and adjacent to water
            if (((robots[i].type == RobotType.LANDSCAPER) && (rc.getLocation().add(dir).isAdjacentTo(robots[i].location)) && isAdjacentToWater(rc.getLocation().add(dir)))){
                return true;
            }
        }


        return false;
    }

    public boolean isAdjacentToWater(MapLocation loc) throws GameActionException{
        for(Direction dir : Direction.allDirections()){
            if(rc.senseFlooding(loc.add(dir))){
                return true;
            }
        }
        return false;
    }

    public void handleDirtDepositAttack() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(2, enemy);

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ){
                if(tryDeposit(rc.getLocation().directionTo(robot.getLocation()))){
                    return;
                }
            }
        }

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.VAPORATOR){
                if(tryDeposit(rc.getLocation().directionTo(robot.getLocation()))){
                    return;
                }
            }
        }

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.NET_GUN){
                if(tryDeposit(rc.getLocation().directionTo(robot.getLocation()))){
                    return;
                }
            }
        }

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.FULFILLMENT_CENTER){
                if(tryDeposit(rc.getLocation().directionTo(robot.getLocation()))){
                    return;
                }
            }
        }

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.DESIGN_SCHOOL){
                if(tryDeposit(rc.getLocation().directionTo(robot.getLocation()))){
                    return;
                }
            }
        }

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.REFINERY){
                if(tryDeposit(rc.getLocation().directionTo(robot.getLocation()))){
                    return;
                }
            }
        }

        if(rc.getDirtCarrying() >= RobotType.LANDSCAPER.dirtLimit){
            // TODO: Make this actually smart
            tryDeposit(Unsorted.randomDirection());
            return;
        }

        Direction best = Direction.CENTER;
        int highest = Integer.MIN_VALUE;

        for(Direction dir : Direction.allDirections()){
            int elev = rc.senseElevation(rc.getLocation().add(dir));
            if(elev < highest){
                best = dir;
                highest = elev;
            }
            if(rc.senseFlooding(rc.getLocation().add(dir))){
                best = dir;
                break;
            }
        }

        tryDeposit(best);

    }
}

