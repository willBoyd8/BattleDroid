package burial.units.miner;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import burial.tools.Constants;
import burial.units.MobileUnit;
import burial.tools.ActionHelper;

import static bb8.tools.ActionHelper.*;
import static bb8.tools.Unsorted.randomDirection;

public class Miner extends MobileUnit {
    int state = 0;
    public Miner(RobotController rc){
        super(rc);
        int state = 1;
        if (rc.getRoundNum() <= 1){ //Builder Mode
            System.out.println("Entering Builder Mode");
            state = 0;
        }
    }

    public void turn() throws GameActionException {
       if (rc.isReady()){

           switch (state)
           {


           }

           tryMove(randomDirection(), rc);



       }



        else {
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
}
