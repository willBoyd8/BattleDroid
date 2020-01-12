package droideka.units.buildingminer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import droideka.base.MobileUnit;
import droideka.pathing.Simple;

import static droideka.utility.ActionHelper.tryBuild;

public class BuildingMiner extends MobileUnit {
    boolean designSchool;
    boolean hasMoved;

    public BuildingMiner(RobotController rc){
        super(rc);
        designSchool = false;
        hasMoved = false;
    }

    public void turn() throws GameActionException{
        if(!designSchool){
            designSchool = tryBuild(RobotType.DESIGN_SCHOOL, Direction.NORTHEAST, rc);
            return;
        } else if (!hasMoved){
            hasMoved = Simple.tryMove(Direction.NORTHWEST, rc);
            return;
        } else{
            tryBuild(RobotType.VAPORATOR, Direction.NORTH, rc);
            tryBuild(RobotType.FULFILLMENT_CENTER, Direction.SOUTH, rc);
            tryBuild(RobotType.NET_GUN, Direction.NORTHEAST, rc);
        }



    }
}
