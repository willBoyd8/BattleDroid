package mouse.units.mouselandscaper;

import battlecode.common.*;
import mouse.base.MobileUnit;
import mouse.utility.Constants;

import java.util.ArrayList;

public class MouseLandscaper extends MobileUnit {
    ArrayList<MapLocation> dirtLocations;
    ArrayList<MapLocation> digLocations;

    public MouseLandscaper(RobotController rc){
        super(rc);
        dirtLocations = new ArrayList<MapLocation>();
        digLocations = new ArrayList<MapLocation>();

        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

        RobotInfo hq = null;

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ){
                hq = robot;
                break;
            }
            if(robot.getType() == RobotType.DESIGN_SCHOOL && robot.getLocation().isAdjacentTo(rc.getLocation())){
                digLocations.add(robot.getLocation());
            }
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
                if(rc.isReady() && rc.canDigDirt(rc.getLocation().directionTo(loc))){
                    rc.digDirt(rc.getLocation().directionTo(loc));
                }
            }
        }

    }
}
