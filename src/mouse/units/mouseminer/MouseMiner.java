package mouse.units.mouseminer;

import battlecode.common.*;
import mouse.base.MobileUnit;

import static mouse.utility.ActionHelper.tryBuild;

public class MouseMiner extends MobileUnit {
    public MouseMiner(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException{
        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

        RobotInfo hq = null;

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ){
                hq = robot;
                break;
            }
        }

        if(hq != null && rc.getLocation().directionTo(hq.getLocation()) == Direction.SOUTH) {
            tryBuild(RobotType.DESIGN_SCHOOL, Direction.SOUTHEAST, rc);
            tryBuild(RobotType.DESIGN_SCHOOL, Direction.SOUTHWEST, rc);
        } else {
            tryBuild(RobotType.DESIGN_SCHOOL, Direction.NORTHEAST, rc);
            tryBuild(RobotType.DESIGN_SCHOOL, Direction.NORTHWEST, rc);
        }

    }
}
