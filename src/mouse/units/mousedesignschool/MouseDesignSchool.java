package mouse.units.mousedesignschool;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import mouse.base.Building;

import static mouse.utility.ActionHelper.tryBuild;

public class MouseDesignSchool extends Building {
    public MouseDesignSchool(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException{
        tryBuild(RobotType.LANDSCAPER, Direction.NORTH, rc);
        tryBuild(RobotType.LANDSCAPER, Direction.SOUTH, rc);
    }
}
