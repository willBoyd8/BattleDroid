package commando.units.buildingminer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import commando.base.MobileUnit;
import commando.pathing.Simple;

import static droideka.utility.ActionHelper.tryBuild;

public class BuildingMiner extends MobileUnit {
    boolean designSchool;
    boolean hasMoved1;
    boolean fullfillment;
    boolean hasMoved2;
    boolean vaporator;

    public BuildingMiner(RobotController rc){
        super(rc);
        designSchool = false;
        hasMoved1 = false;
        fullfillment = false;
        hasMoved2 = false;
        vaporator = false;
    }

    public void turn() throws GameActionException{
//        if(!designSchool){
//            designSchool = tryBuild(RobotType.DESIGN_SCHOOL, Direction.NORTHEAST, rc);
//            return;
//        } else if (!hasMoved1){
//            hasMoved1 = Simple.tryMove(Direction.NORTHWEST, rc);
//            return;
//        } else if(!fullfillment){
//            fulfillment = tryBuild(RobotType.FULFILLMENT_CENTER, Direction.SOUTH, rc);
//        } else if(!hasMoved2){
//            tryBuild(RobotType.VAPORATOR, Direction.NORTH, rc);
//
//            tryBuild(RobotType.NET_GUN, Direction.NORTHEAST, rc);
//        }

        if(!fullfillment){
            fullfillment = tryBuild(RobotType.FULFILLMENT_CENTER, Direction.WEST, rc);
        } else if (!hasMoved1) {
            hasMoved1 = Simple.tryMove(Direction.NORTHWEST, rc);
        } else if (!hasMoved2) {

            hasMoved2 = Simple.tryMove(Direction.NORTHEAST, rc);
        } else if (!vaporator){
            vaporator = tryBuild(RobotType.VAPORATOR, Direction.WEST, rc);
        } else if (!designSchool) {
            designSchool = tryBuild(RobotType.DESIGN_SCHOOL, Direction.SOUTHEAST, rc);
        } else {

        }

    }
}
