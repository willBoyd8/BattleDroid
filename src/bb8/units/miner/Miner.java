package bb8.units.miner;

import battlecode.common.*;
import bb8.units.*;
import static bb8.tools.ActionHelper.*;
import static bb8.tools.Unsorted.*;
import bb8.tools.*;

public class Miner extends MobileUnit{
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
