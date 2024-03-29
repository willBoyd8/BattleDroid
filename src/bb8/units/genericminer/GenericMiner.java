package bb8.units.genericminer;

import bb8.base.MobileUnit;
import battlecode.common.*;

import static bb8.utility.ActionHelper.*;
import static bb8.utility.Unsorted.*;

import bb8.utility.Constants;

public class GenericMiner extends MobileUnit {
    public GenericMiner(RobotController rc){
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
