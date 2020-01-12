package droideka.units.genericminer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import droideka.base.MobileUnit;
import droideka.pathing.Simple;
import droideka.utility.Constants;

import static droideka.utility.ActionHelper.*;
import static droideka.utility.Unsorted.randomDirection;

public class GenericMiner extends MobileUnit {
    public GenericMiner(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        Simple.tryMove(randomDirection(), rc);
        if (Simple.tryMove(randomDirection(), rc))
            System.out.println("I moved!");
        // tryBuild(randomSpawnedByMiner(), randomDirection());
        for (Direction dir : Constants.DIRECTIONS)
            tryBuild(RobotType.FULFILLMENT_CENTER, dir, rc);
        for (Direction dir : Constants.DIRECTIONS)
            if (tryRefine(dir, rc))
                System.out.println("I refined soup! " + rc.getTeamSoup());
        for (Direction dir : Constants.DIRECTIONS)
            if (tryMine(dir, rc))
                System.out.println("I mined soup! " + rc.getSoupCarrying());
    }
}
