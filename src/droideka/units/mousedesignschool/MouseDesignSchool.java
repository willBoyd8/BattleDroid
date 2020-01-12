package droideka.units.mousedesignschool;

import battlecode.common.*;
import droideka.base.Building;

import static c3po.utility.ActionHelper.tryBuild;

public class MouseDesignSchool extends Building {
    boolean hasBuilt;
    int numberBuilt;

    public MouseDesignSchool(RobotController rc){
        super(rc);
        hasBuilt = false;
        numberBuilt = 0;
    }

    public void turn() throws GameActionException {
        if (rc.senseElevation(rc.getLocation()) + GameConstants.MAX_DIRT_DIFFERENCE > rc.senseElevation(rc.getLocation().add(Direction.EAST))) {
            if(tryBuild(RobotType.LANDSCAPER, Direction.EAST, rc)){
                numberBuilt++;
            }
            return;
        } else {
            if(numberBuilt < Integer.MAX_VALUE) {
                if (tryBuild(RobotType.LANDSCAPER, Direction.SOUTH, rc)) {
                    numberBuilt++;
                }
            }
            return;
        }

    }
}
