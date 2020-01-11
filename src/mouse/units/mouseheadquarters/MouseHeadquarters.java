package mouse.units.mouseheadquarters;

import battlecode.common.*;
import mouse.base.Building;

import static mouse.utility.ActionHelper.*;

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
        if(rc.getRoundNum() >= 500){
            if(!hasBuilt) {
                hasBuilt = tryBuild(RobotType.MINER, Direction.SOUTH, rc);
            }
        } else if (rc.getRoundNum() < 490) {
            if(minerCounter < maxMiners) {
                if(tryBuild(RobotType.MINER, Direction.NORTH, rc)){
                    minerCounter++;
                }
            }
        }

    }
}
