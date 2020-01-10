package mouse.units.mousedesignschool;

import battlecode.common.*;
import mouse.base.Building;

import static mouse.utility.ActionHelper.tryBuild;

public class MouseDesignSchool extends Building {
    boolean hasBuilt;

    public MouseDesignSchool(RobotController rc){
        super(rc);
        hasBuilt = false;
    }

    public void turn() throws GameActionException {
        if (rc.senseElevation(rc.getLocation()) + GameConstants.MAX_DIRT_DIFFERENCE > rc.senseElevation(rc.getLocation().add(Direction.EAST))) {
            tryBuild(RobotType.LANDSCAPER, Direction.EAST, rc);
            return;
        } else {
            tryBuild(RobotType.LANDSCAPER, Direction.SOUTH, rc);
            return;
        }

    }
}
