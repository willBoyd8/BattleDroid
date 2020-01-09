package mouse.units.mouseheadquarters;

import battlecode.common.*;
import mouse.base.Building;

import static mouse.utility.ActionHelper.*;

public class MouseHeadquarters extends Building {
    boolean horizontal;
    boolean vertical;
    public MouseHeadquarters(RobotController rc){
        super(rc);
        this.horizontal = false;
        this.vertical = false;
    }

    public void turn() throws GameActionException{
        tryBuild(RobotType.MINER, Direction.NORTH, rc);
        //tryBuild(RobotType.MINER, Direction.SOUTH, rc);
    }
}
