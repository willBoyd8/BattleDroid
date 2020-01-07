package droideka.units.landscaper;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import droideka.units.MobileUnit;

public class Landscaper extends MobileUnit {
    public Landscaper(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException{
        rush();
    }

    public void rush() throws GameActionException{
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        if(robots.length > 0){
            if(rc.canPickUpUnit(robots[0].ID)){
                rc.pickUpUnit(robots[0].ID);
            }
        }
    }
}

//spawn - initial location data
//myteam - team variable
