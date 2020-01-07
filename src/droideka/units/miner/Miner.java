package droideka.units.miner;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import droideka.tools.Constants;
import droideka.units.MobileUnit;

import static bb8.tools.ActionHelper.*;
import static bb8.tools.Unsorted.randomDirection;

public class Miner extends MobileUnit {
    public Miner(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        tryMove(randomDirection(), rc);
        if (tryMove(randomDirection(), rc))
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
