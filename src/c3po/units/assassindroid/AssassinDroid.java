package c3po.units.assassindroid;

import battlecode.common.*;
import c3po.base.Building;


public class AssassinDroid extends Building {
    public AssassinDroid(RobotController rc){

        super(rc);
    }

    public void turn() throws GameActionException {
        RobotInfo[] targets = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        int nearestDist = 9999;
        RobotInfo nearestDrone = null;
        if (!(targets[0] == null)){
            for (int i = 0; i < targets.length; i++){
                if (targets[i].location.distanceSquaredTo(rc.getLocation()) < nearestDist){
                    nearestDist = targets[i].location.distanceSquaredTo(rc.getLocation());
                    nearestDrone = targets[i];
                }
            }
            rc.shootUnit(nearestDrone.ID);
        }
    }
}
