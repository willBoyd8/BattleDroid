package mouse.units.mouseheadquarters;

import battlecode.common.*;
import mouse.base.Building;

import static mouse.utility.ActionHelper.*;

public class MouseHeadquarters extends Building {
    boolean horizontal;
    boolean vertical;
    boolean hasBuilt;
    public MouseHeadquarters(RobotController rc){
        super(rc);
        this.horizontal = false;
        this.vertical = false;
        this.hasBuilt = false;
    }

    public void turn() throws GameActionException{
        //tryBuild(RobotType.MINER, Direction.NORTH, rc);
        if(!hasBuilt) {
            hasBuilt = tryBuild(RobotType.MINER, Direction.SOUTH, rc);
        }
    }
}
