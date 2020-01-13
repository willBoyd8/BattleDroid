package droideka.units.buildingminer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import droideka.base.KillMeNowException;
import droideka.base.MobileUnit;
import droideka.pathing.Simple;

import static droideka.utility.ActionHelper.tryBuild;

public class BuildingMiner extends MobileUnit {
    boolean designSchool;
    boolean hasMoved1;
    boolean hasMoved2;
    boolean hasMoved3;
    boolean hasMoved4;
    boolean vaporator1;
    boolean vaporator2;
    boolean fulfillment;
    boolean netgun1;
    boolean netgun2;

    public BuildingMiner(RobotController rc){
        super(rc);
        designSchool = false;
        hasMoved1 = false;
        hasMoved2 = false;
        hasMoved3 = false;
        hasMoved4 = false;
        vaporator1 = false;
        vaporator2 = false;
        fulfillment = false;
        netgun1 = false;
        netgun2 = false;

    }

    public void turn() throws GameActionException, KillMeNowException{
        if(!designSchool){
            designSchool = tryBuild(RobotType.DESIGN_SCHOOL, Direction.NORTHEAST, rc);
            return;
        } else if (!hasMoved1){
            hasMoved1 = Simple.tryMove(Direction.NORTHWEST, rc);
            return;
        } else if (!vaporator1){
            vaporator1 = tryBuild(RobotType.VAPORATOR, Direction.NORTH, rc);
            return;
        } else if (!hasMoved2){
            hasMoved2 = Simple.tryMove(Direction.NORTHEAST, rc);
            return;
        } else if(!netgun1){
            netgun1 = tryBuild(RobotType.NET_GUN, Direction.EAST, rc);
            return;
        } else if(!hasMoved3){
            hasMoved3 = Simple.tryMove(Direction.SOUTHWEST, rc);
            return;
        } else if(!vaporator2){
            vaporator2 = tryBuild(RobotType.VAPORATOR, Direction.NORTHEAST, rc);
            return;
        } else if(!fulfillment){
            fulfillment = tryBuild(RobotType.FULFILLMENT_CENTER, Direction.SOUTH, rc);
            return;
        } else if(!hasMoved4){
            hasMoved4 = Simple.tryMove(Direction.SOUTHEAST, rc);
            return;
        } else if(!netgun2){
            netgun2 = tryBuild(RobotType.NET_GUN, Direction.NORTHWEST, rc);
            return;
        } else {
            throw new KillMeNowException();
        }


    }
}
