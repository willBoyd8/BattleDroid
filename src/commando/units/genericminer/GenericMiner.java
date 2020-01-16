package commando.units.genericminer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import commando.base.MobileUnit;
import commando.pathing.Simple;
import commando.utility.Constants;

import static commando.utility.ActionHelper.*;
import static commando.utility.Unsorted.randomDirection;

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
