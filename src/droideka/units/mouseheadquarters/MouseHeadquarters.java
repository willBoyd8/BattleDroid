package droideka.units.mouseheadquarters;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import droideka.base.Building;
import droideka.utility.ActionHelper;
import droideka.utility.Constants;

import static c3po.utility.ActionHelper.tryBuild;

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
        ActionHelper.tryShoot(rc);

        if(rc.getRoundNum() >= Constants.WALL_START_ROUND + 2){
            if(!hasBuilt) {
                hasBuilt = tryBuild(RobotType.MINER, Direction.SOUTH, rc);
            }
        } else if (rc.getRoundNum() < Constants.WALL_START_ROUND - 50) {
            if(minerCounter < maxMiners) {
                for (int i = 0; i < Constants.DIRECTIONS.length; i++){
                    if(tryBuild(RobotType.MINER, Constants.DIRECTIONS[i], rc)){
                        minerCounter++;
                        break;
                    }
                }
                }
            }
        }
    }


