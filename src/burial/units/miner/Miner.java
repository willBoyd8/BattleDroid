package burial.units.miner;

import battlecode.common.*;
import battlecode.world.MapBuilder;
import burial.tools.Constants;
import burial.units.MobileUnit;
import burial.tools.ActionHelper;
import burial.units.Unit;

import static bb8.tools.ActionHelper.*;
import static bb8.tools.Unsorted.randomDirection;

public class Miner extends MobileUnit {
    int state = 0;
    boolean DS = false;
    boolean FC = false;
    boolean REFINERY = false;
    public Miner(RobotController rc){
        super(rc);
        if (rc.getRoundNum() <= 0){ //Builder Mode but only for the first unit spawned
            System.out.println("Entering Builder Mode");
            state = 1;
        }
    }

    public void turn() throws GameActionException {
       if (rc.isReady()){

           switch (state)
           {
               case 0: //Mine Mode

                   break;

               case 1: //Builder Mode
                   if (REFINERY == false){ //Using boolean variable as a flag for whether or not Design school has already been built
                       tryMove(randomDirection()/*MapLocation.directionTo()*/, rc);
                       for (Direction dir : Constants.DIRECTIONS){
                           if (ActionHelper.tryBuild(RobotType.FULFILLMENT_CENTER, randomDirection(), rc)){
                               System.out.println("Refinery Built");
                               REFINERY = true;
                           }
                       }

                   }
                   else if (FC == false){ //Using boolean variable as a flag for whether or not Design school has already been built
                       tryMove(randomDirection()/*MapLocation.directionTo()*/, rc);
                       for (Direction dir : Constants.DIRECTIONS){
                           if (ActionHelper.tryBuild(RobotType.FULFILLMENT_CENTER, randomDirection(), rc)){
                               System.out.println("Fulfillment Center Built");
                               FC = true;
                           }
                       }

                   }
                   else if (DS == false){ //Using boolean variable as a flag for whether or not Design school has already been built
                       tryMove(randomDirection()/*MapLocation.directionTo()*/, rc);
                       for (Direction dir : Constants.DIRECTIONS){
                           if (ActionHelper.tryBuild(RobotType.DESIGN_SCHOOL, randomDirection(), rc)){
                               System.out.println("Design School Built");
                               DS = true;
                           }
                       }

                   }
                   if ((REFINERY==true)&&(FC==true)&&(DS==true)){
                       System.out.println("Finished building Main buildings! Switching to Defense Construction");
                       state = 2;
                   }

                   break;

               case 2:

                   break;
           }

           //tryMove(randomDirection(), rc);

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
