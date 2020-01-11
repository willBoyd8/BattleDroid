package c3po.units.mouselandscaper;

import battlecode.common.*;
import c3po.base.MobileUnit;
import c3po.utility.Constants;

import java.util.ArrayList;

import static c3po.utility.ActionHelper.tryMove;

public class MouseLandscaper extends MobileUnit {
    ArrayList<MapLocation> dirtLocations;
    ArrayList<MapLocation> digLocations;
    MapLocation hqLocation;
    boolean hasMoved;
    boolean moving;
    MapLocation partnerLocation;
    ArrayList<Direction> path;

    public MouseLandscaper(RobotController rc){
        super(rc);
        dirtLocations = new ArrayList<MapLocation>();
        digLocations = new ArrayList<MapLocation>();
        path = new ArrayList<Direction>();
        moving = false;
        hasMoved = false;
        partnerLocation = rc.getLocation().add(Direction.WEST).add(Direction.WEST);
        decideLocations();

    }

    public void decideLocations(){
        dirtLocations.clear();
        digLocations.clear();

        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

        RobotInfo hq = null;

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ){
                hq = robot;
                hqLocation = robot.getLocation();
                break;
            }
            if(robot.getType() == RobotType.DESIGN_SCHOOL && (rc.getLocation().directionTo(robot.getLocation()) == Direction.NORTH || rc.getLocation().directionTo(robot.getLocation()) == Direction.SOUTH)){
                digLocations.add(robot.getLocation());

            }
        }

        if(rc.getLocation().directionTo(hq.getLocation()) == Direction.NORTHWEST){
            path.add(Direction.SOUTH);
            path.add(Direction.WEST);
            path.add(Direction.WEST);
            path.add(Direction.NORTH);
        } else {
            path.add(Direction.NORTH);
            path.add(Direction.WEST);
            path.add(Direction.WEST);
            path.add(Direction.SOUTH);
        }


        for(Direction dir : Constants.DIRECTIONS){
            if(rc.getLocation().add(dir) == hq.getLocation() || rc.getLocation().add(dir).isAdjacentTo(hq.getLocation())){
                if(rc.getLocation().add(dir) == hq.getLocation()){
                    digLocations.add(0, rc.getLocation().add(dir));
                } else {
                    digLocations.add(rc.getLocation().add(dir));
                }
            } else {
                dirtLocations.add(rc.getLocation().add(dir));
            }


        }
    }

    public void turn() throws GameActionException {

        if(!hasMoved) {
            boolean shortWall = true;
            for (MapLocation loc : dirtLocations) {
                if (rc.senseElevation(loc) - rc.senseElevation(hqLocation) <= GameConstants.MAX_DIRT_DIFFERENCE - 1) {
                    shortWall = false;
                    break;
                }
            }

            if (shortWall && !hasMoved && !moving) {
                if (rc.senseRobotAtLocation(rc.getLocation().add(Direction.WEST).add(Direction.WEST)) == null) {
                    moving = true;
                } else {
                    shortWall = false;
                }
            }

            if (moving) {
                boolean success = tryMove(path.get(0), rc);
                if (success) {
                    path.remove(0);
                } else {
                    if(rc.senseElevation(rc.getLocation().add(path.get(0))) < rc.senseElevation(rc.getLocation())){
                        if(rc.getDirtCarrying() > 0) {
                            if (rc.isReady() && rc.canDepositDirt(path.get(0))) {
                                rc.depositDirt(path.get(0));
                            }
                        } else {
                            if(rc.isReady() && rc.canDigDirt(path.get(0).opposite())){
                                rc.digDirt(path.get(0).opposite());
                            }
                        }
                    } else {
                        if(rc.isReady() && rc.canDigDirt(path.get(0))){
                            rc.digDirt(path.get(0));
                        }
                    }
                }
            }

            if (moving && partnerLocation.equals(rc.getLocation())) {
                moving = false;
                hasMoved = true;
                decideLocations();
                return;
            }

            if (moving && path.size() == 0) {
                moving = false;
                hasMoved = true;
                decideLocations();
                return;
            } else if (moving) {
                return;
            }
        }

        standardDirtWall();

    }

    public void standardDirtWall() throws GameActionException{
        if(rc.getDirtCarrying() > 0){
            int lowest = Integer.MAX_VALUE;
            MapLocation placeLocation = null;

            for(MapLocation loc : dirtLocations){
                if(rc.canSenseLocation(loc)){
                    int elevation = rc.senseElevation(loc);
                    if(elevation < lowest) {
                        lowest = elevation;
                        placeLocation = loc;
                    }
                }
            }

            if(rc.isReady() && rc.canDepositDirt(rc.getLocation().directionTo(placeLocation))){
                rc.depositDirt(rc.getLocation().directionTo(placeLocation));
            }
        } else {
            for(MapLocation loc : digLocations){
                if(rc.isReady() && rc.canDigDirt(rc.getLocation().directionTo(loc)) && rc.getLocation().directionTo(loc) != Direction.NORTH && rc.getLocation().directionTo(loc) != Direction.SOUTH){
                    rc.digDirt(rc.getLocation().directionTo(loc));
                }
            }
        }
    }
}
