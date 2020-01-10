package burial.units.miner;

import battlecode.common.*;
import battlecode.world.MapBuilder;
import burial.tools.Constants;
import burial.units.MobileUnit;
import burial.tools.ActionHelper;
import burial.units.Unit;


import java.util.Random;

import static burial.tools.ActionHelper.*;
import static burial.tools.Unsorted.randomDirection;

public class Miner extends MobileUnit {
    int state = 0;
    boolean DS = false;
    boolean FC = false;
    boolean REFINERY = false;
    boolean taskSet = false;
    MapLocation friendlyHQ = rc.getLocation();
    MapLocation destination = rc.getLocation();
    public Miner(RobotController rc){
        super(rc);
        if (rc.getRoundNum() <= 0){ //Builder Mode but only for the first unit spawned
            System.out.println("Entering Builder Mode");
            state = 1;
        }
    }

    public void turn() throws GameActionException {
       if (rc.isReady()){
           if (this.age < 2){
               RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
               for (int i = 0; i < nearbyRobots.length; i++){
                   if ((nearbyRobots[i].type == RobotType.HQ)&&(nearbyRobots[i].team == this.myTeam)){
                       friendlyHQ = nearbyRobots[i].location;
                   }
               }
           }

           switch (state)
           {
               case 0: //Mine Mode
                   if (rc.getSoupCarrying() < 98) { //If not full then go mine
                       MapLocation[] visibleTiles = ActionHelper.getVisibleTiles(RobotType.MINER, rc);
                       MapLocation[] soupTiles = ActionHelper.getSoupTiles(visibleTiles, rc);

                       if (soupTiles[0] == null){
                           Direction toHQ = rc.getLocation().directionTo(friendlyHQ);
                           Direction awayFromHQ = toHQ.opposite();
                           Random r = new Random();
                           Direction travelDir = awayFromHQ;
                           while(true) {
                               int travelDescision = r.nextInt(5);
                               switch (travelDescision) {
                                   case 0:
                                       travelDir = awayFromHQ.rotateLeft().rotateLeft();
                                       break;

                                   case 1:
                                       travelDir = awayFromHQ.rotateLeft();
                                       break;

                                   case 2:
                                       //Go straight
                                       break;

                                   case 3:
                                       travelDir = awayFromHQ.rotateRight();
                                       break;
                                   case 4:
                                       travelDir = awayFromHQ.rotateRight().rotateRight();
                                       break;
                               }
                               if (tryMove(travelDir, rc)){
                                   break;
                               }
                           }

                           break;
                       }

                       MapLocation closestPoint = ActionHelper.getClosestTile(soupTiles, rc.getLocation(), rc);
                       destination = closestPoint;
                       if (destination.isAdjacentTo(rc.getLocation())){
                           if (tryMine(rc.getLocation().directionTo(destination), rc)){
                               System.out.println("Soup Mined");
                           }
                       }
                       else{
                           Direction travel = rc.getLocation().directionTo(destination);
                       }
                   }
                   else{ //If full go deposit soup
                       RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
                       if (ActionHelper.checkForUnitsofTypeandTeam(nearbyRobots, RobotType.REFINERY, this.myTeam, rc)){
                           RobotInfo closestUnit = getClosestUnitofTypeandTeam(nearbyRobots, RobotType.REFINERY, this.myTeam, rc.getLocation(), rc);
                           if (closestUnit.location.isAdjacentTo(rc.getLocation())){
                               Direction depositDirection = rc.getLocation().directionTo(closestUnit.location);
                               rc.depositSoup(depositDirection, 100);
                           }
                           else{
                               Direction travelDir = rc.getLocation().directionTo(closestUnit.location);
                               tryMove(travelDir, rc);
                           }

                       }
                       else if (ActionHelper.checkForUnitsofTypeandTeam(nearbyRobots, RobotType.HQ, this.myTeam, rc)){
                           RobotInfo closestUnit = getClosestUnitofTypeandTeam(nearbyRobots, RobotType.HQ, this.myTeam, rc.getLocation(), rc);
                           Direction travel = rc.getLocation().directionTo(closestUnit.location);
                           if (closestUnit.location.isAdjacentTo(rc.getLocation())){
                               Direction depositDirection = rc.getLocation().directionTo(closestUnit.location);
                               rc.depositSoup(depositDirection, 100);
                           }
                           else{
                               Direction travelDir = rc.getLocation().directionTo(closestUnit.location);
                               tryMove(travelDir, rc);
                           }
                       }
                       else{
                           for (Direction dir : Constants.DIRECTIONS){
                               if (ActionHelper.tryBuild(RobotType.REFINERY, randomDirection(), rc)){
                                   System.out.println("Refinery Built");
                                   REFINERY = true;
                               }
                           }
                       }
                   }


                   break;

               case 1: //Builder Mode
                   if (REFINERY == false){ //Using boolean variable as a flag for whether or not Design school has already been built
                       tryMove(randomDirection()/*MapLocation.directionTo()*/, rc);
                       for (Direction dir : Constants.DIRECTIONS){
                           if (ActionHelper.tryBuild(RobotType.REFINERY, randomDirection(), rc)){
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
