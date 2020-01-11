package c3po.units.mouseheadquarters;

import battlecode.common.*;
import c3po.base.Building;
import c3po.utility.Constants;

import static c3po.utility.ActionHelper.*;

public class MouseHeadquarters extends Building {
    boolean horizontal;
    boolean vertical;
    boolean hasBuilt;
    public int minerCounter;
    public static int maxMiners = 10;

    public MouseHeadquarters(RobotController rc){
        super(rc);
        this.horizontal = false;
        this.vertical = false;
        this.hasBuilt = false;
    }

    public void turn() throws GameActionException{

        RobotInfo[] targets = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        int nearestDist = Integer.MAX_VALUE;
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

        if(rc.getRoundNum() >= Constants.WALL_START_ROUND + 2){
            if(!hasBuilt) {
                hasBuilt = tryBuild(RobotType.MINER, Direction.SOUTH, rc);
            }
        } else if (rc.getRoundNum() < Constants.WALL_START_ROUND - 50) {
            if(minerCounter < maxMiners) {
                if(tryBuild(RobotType.MINER, Direction.NORTH, rc)){
                    minerCounter++;
                }
            }
        }

    }
}
