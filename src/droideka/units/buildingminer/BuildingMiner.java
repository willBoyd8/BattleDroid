package droideka.units.buildingminer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import droideka.base.MobileUnit;

import static c3po.utility.ActionHelper.tryBuild;
import static c3po.utility.ActionHelper.tryMove;

public class BuildingMiner extends MobileUnit {
    boolean designSchool;
    boolean hasMoved;
    boolean fulfillment;

    public BuildingMiner(RobotController rc){
        super(rc);
        designSchool = false;
        hasMoved = false;
        fulfillment = false;
    }

    public void turn() throws GameActionException{
        if(!designSchool){
            designSchool = tryBuild(RobotType.DESIGN_SCHOOL, Direction.NORTHEAST, rc);
            return;
        } else if (!hasMoved){
            hasMoved = tryMove(Direction.NORTHWEST, rc);
            return;
        } else if (!fulfillment){
            fulfillment = tryBuild(RobotType.FULFILLMENT_CENTER, Direction.SOUTH, rc);
            return;
        } else{
            tryBuild(RobotType.NET_GUN, Direction.NORTHEAST, rc);
            tryBuild(RobotType.FULFILLMENT_CENTER, Direction.SOUTH, rc);
            tryBuild(RobotType.VAPORATOR, Direction.NORTH, rc);
        }



    }
}
