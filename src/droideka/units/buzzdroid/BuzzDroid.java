package droideka.units.buzzdroid;

import battlecode.common.*;
import droideka.base.MobileUnit;
import droideka.pathing.Simple;
import droideka.utility.ActionHelper;
import droideka.utility.Constants;
import droideka.utility.DebugHelper;

import java.util.ArrayList;

public class BuzzDroid extends MobileUnit {
    public DroneState state;
    public ArrayList<MapLocation> enemyHQLocations;
    public int homeQuad;
    public RaidState raidState;
    public DefendState defendState;
    public RobotInfo holding;
    public MapLocation nearestWater;
    public MapLocation enemyHQ;
    public ArrayList<MapLocation> patrolPath;
    public MapLocation patrolPoint;

    public BuzzDroid (RobotController rc) {
        super(rc);
        state = DroneState.GENERATE;
        targetLocation = null;
        enemyHQLocations = new ArrayList<MapLocation>();
        homeQuad = 0;
        raidState = null;
        defendState = null;
        holding = null;
        nearestWater = null;
        enemyHQ = null;
        patrolPoint = null;
        patrolPath = new ArrayList<MapLocation>();
        patrolPath.add(hqLocation.translate(-5,-5));
        patrolPath.add(hqLocation.translate(-5,5));
        patrolPath.add(hqLocation.translate(5,5));
        patrolPath.add(hqLocation.translate(5,-5));

    }

    // TODO: This isn't 100% safe because it can take any mobile unit
    public BuzzDroid(MobileUnit unit){
        super(unit);
        state = DroneState.GENERATE;
        targetLocation = null;
        enemyHQLocations = new ArrayList<MapLocation>();
        homeQuad = 0;
        raidState = null;
        holding = null;
        nearestWater = null;
        enemyHQ = null;
    }

    enum DroneState {
        GENERATE,
        LEAVEBASE,
        DEFEND,
        LOOK,
        MOVE_TO_POINT,
        CAN_SENSE,
        FOUND,
        REMOVE,
        WAIT_TO_RAID,
        RAID
    }

    enum RaidState {
        BUZZ,
        KIDNAP,
        DROPPING,
        MOVING,
    }

    enum DefendState {
        PATROL,
        INTERCEPT,
        //HOLDING,
        DISPOSINGDRONE,
        DISPOSINGLANDUNIT,
        RESUMINGPATROL,
    }

    public void turn() throws GameActionException{
//        RobotInfo robots[] = rc.senseNearbyRobots(-1, myTeam);
//        if(rc.isReady() && rc.isCurrentlyHoldingUnit() && (rc.getRoundNum()> 700) && (robots.length < 5)) {
//            for (int i = 0; i < Constants.DIRECTIONS.length; i++) {
//                if (ActionHelper.tryDrop(Constants.DIRECTIONS[i], rc)) {
//                    holding = null;
//                    //raidState = RaidState.KIDNAP;
//                    //System.out.println("DROPPING: Switching to KIDNAP state");
//                    //kidnap();
//                    //return;
//                }
//            }
//        }


        // TODO: When we actually fix rotation, this will need to be removed
        if(rc.isReady() && !rc.isCurrentlyHoldingUnit()){
            if(rc.getLocation().isAdjacentTo(hqLocation)){
                RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(Direction.EAST));
                if(robot != null && robot.getType() == RobotType.LANDSCAPER){
                    if(ActionHelper.tryPickup(robot.ID, rc)){
                        holding = robot;
                    }
                }
            } else if (rc.getLocation().add(Direction.NORTH).isAdjacentTo(hqLocation)) {
                RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(Direction.NORTHEAST));
                if(robot != null && robot.getType() == RobotType.LANDSCAPER){
                    if(ActionHelper.tryPickup(robot.ID, rc)){
                        holding = robot;
                    }
                }
            }
        }




        switch(state){
            case GENERATE: generate(); break;

            case LEAVEBASE: leaveBase(); break;

            case DEFEND: defend(); break;

            case LOOK: look(); break;

            case MOVE_TO_POINT: moveToPoint(); break;

            case CAN_SENSE: canSense(); break;

            case FOUND: found(); break;

            case WAIT_TO_RAID: waitToRaid(); break;

            case REMOVE: removePoint(); break;

            case RAID: raid(); break;

        }

    }

    public  void preEnd() throws GameActionException {
        if(rc.senseFlooding(rc.getLocation())){
            nearestWater = rc.getLocation();
        }

        for(MapLocation loc : enemyHQLocations){
            DebugHelper.setIndicatorDot(loc, 255, 0, 0, rc);
        }
    }

    private void generate() throws GameActionException {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();

        //Figure out Home Quadrant
        if ((hqLocation.x > (mapWidth/2))&&(hqLocation.y < (mapHeight/2))){
            homeQuad = 4;
        }
        else if ((hqLocation.x < (mapWidth/2))&&(hqLocation.y > (mapHeight/2))){
            homeQuad = 2;
        }
        else if ((hqLocation.x < (mapWidth/2))&&(hqLocation.y < (mapHeight/2))){
            homeQuad = 3;
        }
        else if ((hqLocation.x > (mapWidth/2))&&(hqLocation.y > (mapHeight/2))){
            homeQuad = 1;
        }

        //Calculate Offsets and instantiate MapLocationObjects
        int xOffset = mapWidth-hqLocation.x-1;
        int yOffset = mapHeight-hqLocation.y-1;
        MapLocation loc1;
        MapLocation loc2;
        MapLocation loc3;
        MapLocation loc4;

        //Set Potential Locations based on Home Quadrant
        switch (homeQuad){
            case 1:
                loc2 = new MapLocation(xOffset, hqLocation.y);
                loc3 = new MapLocation(xOffset, yOffset);
                loc4 = new MapLocation(hqLocation.x, yOffset);
                enemyHQLocations.add(loc4);
                enemyHQLocations.add(loc3);
                enemyHQLocations.add(loc2);
                break;
            case 2:
                loc1 = new MapLocation(xOffset, hqLocation.y);
                loc3 = new MapLocation(hqLocation.x, yOffset);
                loc4 = new MapLocation(xOffset, yOffset);
                enemyHQLocations.add(loc3);
                enemyHQLocations.add(loc4);
                enemyHQLocations.add(loc1);
                break;
            case 3:
                loc1 = new MapLocation(xOffset, yOffset);
                loc2 = new MapLocation(hqLocation.x, yOffset);
                loc4 = new MapLocation(xOffset, hqLocation.y);

                enemyHQLocations.add(loc4);
                enemyHQLocations.add(loc1);
                enemyHQLocations.add(loc2);
                break;
            case 4:
                loc1 = new MapLocation(hqLocation.x, yOffset);
                loc2 = new MapLocation(xOffset, yOffset);
                loc3 = new MapLocation(xOffset, hqLocation.y);
                enemyHQLocations.add(loc3);
                enemyHQLocations.add(loc2);
                enemyHQLocations.add(loc1);
                break;
        }


        state = DroneState.LEAVEBASE;
        leaveBase();
        return;
    }

    private void leaveBase() throws GameActionException {
        if (rc.getLocation().distanceSquaredTo(hqLocation) >= 25){
            //state = DroneState.LOOK;
            state = DroneState.DEFEND;
            defend();
            return;
        }
        if (Simple.tryMove(Direction.SOUTH, rc)){
            return;
        } else if (Simple.tryMove(Direction.SOUTHWEST, rc)){
            return;
        }
    }

    private void defend() throws GameActionException {
        switch(defendState){
            case PATROL: patrol(); return;
            case INTERCEPT: intercept(); return;
            //case HOLDING: holdState(); return;
            case DISPOSINGDRONE: disposingDrone(); return;
            case DISPOSINGLANDUNIT: disposingLandUnit(); return;
            case RESUMINGPATROL: resumePatrol(); return;
        }
    }

    //Patrol around the base until enemy spotted then switch to Intercept
    private void patrol() throws GameActionException {
        if (rc.getRoundNum() > Constants.RAID_START_ROUND) {
            state = DroneState.LOOK;
        }
        patrolPoint = patrolPath.get(0);
        RobotInfo robots[] = rc.senseNearbyRobots(-1, enemy);
        if (robots.length > 0) {
            defendState = DefendState.INTERCEPT;
            intercept();
            return;
        }
        if (rc.getLocation().isAdjacentTo(patrolPoint)){
            patrolPath.add(patrolPath.get(0));
            patrolPath.remove(0);
        }
        if (Simple.moveToLocationFuzzy(patrolPoint, rc)){
            return;
        }
        if (Simple.tryMove(rc.getLocation().directionTo(patrolPoint).rotateLeft().rotateLeft(), rc)){
            return;
        }
        if (Simple.tryMove(rc.getLocation().directionTo(patrolPoint).rotateRight().rotateRight(), rc)){
            return;
        }
    }

    //Determine Closest enemy unit and move towards it, then pickup when adjacent and switch to Holding
    private void intercept() throws GameActionException {
        RobotInfo robots[] = rc.senseNearbyRobots(-1, enemy);
        int closestDist = 9999;
        RobotInfo target = null;
        for (RobotInfo robot : robots) {
            if (robot.location.distanceSquaredTo(rc.getLocation()) < closestDist) {
                closestDist = robot.location.distanceSquaredTo(rc.getLocation());
                target = robot;
            }
        }
        if (rc.canPickUpUnit(target.ID)) {
            rc.pickUpUnit(target.ID);
            holding = target;
            if (holding.type == RobotType.DELIVERY_DRONE) {
                defendState = DefendState.DISPOSINGDRONE;
                disposingDrone();
                return;
            } else {
                defendState = DefendState.DISPOSINGLANDUNIT;
                disposingLandUnit();
                return;
            }
        } else {
            if (Simple.moveToLocationFuzzy(target.location, rc)) {
                return;
            }
            if (Simple.tryMove(rc.getLocation().directionTo(patrolPoint).rotateLeft().rotateLeft(), rc)){
                return;
            }
            if (Simple.tryMove(rc.getLocation().directionTo(patrolPoint).rotateRight().rotateRight(), rc)){
                return;
            }
        }

    }

    /*private void holdState() throws GameActionException {

    }*/

    private void disposingDrone() throws GameActionException {
        RobotInfo enemyRobots[] = rc.senseNearbyRobots(-1, enemy);
        if (enemyRobots.length == 0) {
            int closestDist = 999;
            RobotInfo closestNetGun = null;
            RobotInfo friendlyRobots[] = rc.senseNearbyRobots(-1, myTeam);
            for (RobotInfo unit : friendlyRobots) {
                if ((unit.type == RobotType.NET_GUN) || (unit.type == RobotType.HQ)){
                    if (rc.getLocation().distanceSquaredTo(unit.location) < closestDist) {
                        closestDist = rc.getLocation().distanceSquaredTo(unit.location);
                        closestNetGun = unit;
                    }
                }
            }
            if (rc.getLocation().distanceSquaredTo(closestNetGun.location) <= GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED) {
                if (rc.canDropUnit(rc.getLocation().directionTo(closestNetGun.location))){
                    rc.dropUnit(rc.getLocation().directionTo(closestNetGun.location));
                    defendState = DefendState.RESUMINGPATROL;
                    resumePatrol();
                    return;
                }
                else{
                    for (Direction dir : Constants.DIRECTIONS){
                        if (rc.getLocation().add(dir).distanceSquaredTo(closestNetGun.location) <= GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED) {
                            if (rc.canDropUnit(dir)){
                                rc.dropUnit(dir);
                                defendState = DefendState.RESUMINGPATROL;
                                resumePatrol();
                                return;
                            }
                        }
                    }
                    if (Simple.moveToLocationFuzzy(closestNetGun.location, rc)) {
                        return;
                    }
                }
            }

        }
    }

    private void disposingLandUnit() throws GameActionException {
        ArrayList<MapLocation> floodTiles = ActionHelper.getFloodLocations(rc);
        if (floodTiles.size() > 0) {
            int closestDist = 999;
            MapLocation closestFlood = null;
            ArrayList<MapLocation> adjFloodTiles = new ArrayList<MapLocation>();
            for (MapLocation flooded : floodTiles) {
                if (flooded.isAdjacentTo(rc.getLocation())) {
                    adjFloodTiles.add(flooded);
                }
                if ((flooded.distanceSquaredTo(rc.getLocation()) < closestDist) && (flooded.distanceSquaredTo(rc.getLocation()) > 0)) {
                    closestDist = flooded.distanceSquaredTo(rc.getLocation());
                    closestFlood = flooded;
                }
            }
            if ((rc.getLocation().isAdjacentTo(closestFlood)) && (rc.canDropUnit(rc.getLocation().directionTo(closestFlood)))) {
                rc.dropUnit(rc.getLocation().directionTo(closestFlood));
                defendState = DefendState.RESUMINGPATROL;
                resumePatrol();
                return;
            }
            for (MapLocation drop : adjFloodTiles) {
                if (rc.canDropUnit(rc.getLocation().directionTo(drop))) {
                    rc.dropUnit(rc.getLocation().directionTo(drop));
                    defendState = DefendState.RESUMINGPATROL;
                    resumePatrol();
                    return;
                }
            }
            if (Simple.moveToLocationFuzzy(closestFlood, rc)) {
                return;
            }
        }
    }

    private void resumePatrol() throws GameActionException {
        if (rc.getLocation().isAdjacentTo(patrolPoint)) {
            defendState = DefendState.PATROL;
            patrol();
            return;
        }
        if (Simple.moveToLocationFuzzy(patrolPoint, rc)) {
            return;
        }
    }

    private void look() throws GameActionException {
        if ((rc.getLocation().directionTo(hqLocation) == Direction.SOUTH)&&(rc.getLocation().distanceSquaredTo(hqLocation) <= 16)){
            state = DroneState.LEAVEBASE;
            leaveBase();
            return;
        }
        if (rc.canSenseLocation(enemyHQLocations.get(0))){
            state = DroneState.CAN_SENSE;
            canSense();
            return;
        } else {
            if (rc.getLocation().distanceSquaredTo(hqLocation) <= 2)
            {
                //Simple.tryMove(Direction.SOUTH, rc);
            }
            state = DroneState.MOVE_TO_POINT;
            moveToPoint();
            return;
        }
    }

    private void moveToPoint() throws GameActionException {
        targetLocation = enemyHQLocations.get(0);
        if (Simple.moveToLocationFuzzyNoFlyZone(enemyHQLocations.get(0), GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc)){
            state = DroneState.LOOK;
            return;
        }
        else{
            if (rc.getLocation().distanceSquaredTo(hqLocation) < 10 && Simple.tryMove(Direction.SOUTH, rc)){
                state = DroneState.LOOK;
                return;
            }
            else if(rc.getLocation().distanceSquaredTo(hqLocation) < 10 && Simple.tryMove(Direction.EAST, rc)) {
                state = DroneState.LOOK;
                return;
            }
            else if(rc.getLocation().distanceSquaredTo(hqLocation) < 10 && Simple.tryMove(Direction.WEST, rc)){
                state = DroneState.LOOK;
                return;
            } else if(rc.getLocation().distanceSquaredTo(hqLocation) < 10 && Simple.tryMove(Direction.NORTH, rc)) {
                state = DroneState.LOOK;
                return;
            } else {
                Simple.tryMove(rc);
            }


        }
    }

    private void canSense() throws GameActionException {
        RobotInfo potHQ = null;
        potHQ = rc.senseRobotAtLocation(enemyHQLocations.get(0));
        if (potHQ == null || potHQ.getType() != RobotType.HQ) {
            state = DroneState.REMOVE;
            removePoint();
            return;
        } else {
            state = DroneState.FOUND;
            enemyHQLocations.clear();
            enemyHQLocations.add(potHQ.getLocation());
            enemyHQ = potHQ.getLocation();
            targetLocation = null;
            found();
            return;
        }
    }

    private void found() throws GameActionException {
        //TODO: Implement the broadcasting of enemy HQ location
        state = DroneState.WAIT_TO_RAID;
        waitToRaid();
        return;
    }

    private void waitToRaid() throws GameActionException {
        //System.out.println("I AM WAITING TO RAID!!!");
        if(rc.getRoundNum() > Constants.RAID_START_ROUND){
            System.out.println("ITS RAID TIME, SO I SHOULD BE RAIDING!!!");
            state = DroneState.RAID;
            targetLocation = null;
            raid();
            return;
        } else {
            System.out.println("ITS NOT RAID TIME YET, SO I AM WAITING TO RAID!!!");
            // TODO: Wasting time here?
            int friendlyDroneCount = 0;
            RobotInfo robots[] = rc.senseNearbyRobots(-1, myTeam);
            for(RobotInfo robot : robots){
                if(robot.getType() == RobotType.DELIVERY_DRONE){
                    friendlyDroneCount++;
                }
            }
            System.out.println("THERE ARE " + friendlyDroneCount + "FRIENDLY DRONES");
            if (friendlyDroneCount >= Integer.MAX_VALUE){
                System.out.println("AIGHT BOIS, THERE'S ENOUGH OF US.  ITS GAMER TIME!!!");
                state = DroneState.RAID;
                raid();

            }


            return;
        }
    }

    private void removePoint() throws  GameActionException {
        enemyHQLocations.remove(0);
        targetLocation = enemyHQLocations.get(0);
        state = DroneState.LOOK;
        look();
        return;
    }

    private void raid() throws GameActionException {
        if(rc.isCurrentlyHoldingUnit()){
            if(holding.getTeam() == myTeam){
                raidState = RaidState.BUZZ;
                System.out.println("RAID: Switching to BUZZ state");
            } else  {
            raidState = RaidState.DROPPING;
            System.out.println("RAID: Switching to DROPPING state");
            }

        } else {
            raidState = RaidState.KIDNAP;
            System.out.println("RAID Switching to KIDNAP state");
        }

        switch(raidState){
            case BUZZ: buzz(); return;
            case KIDNAP: kidnap(); return;
            case DROPPING: dropping(); return;
            case MOVING: moving(); return;
        }


    }

    private void buzz() throws GameActionException {
        ArrayList<Direction> notFlooded = new ArrayList<>();

        for(Direction dir : Direction.allDirections()){
            if(!rc.senseFlooding(rc.getLocation().add(dir))){
                notFlooded.add(dir);
            }
        }

        if(notFlooded.size() <= 0){
            targetLocation = enemyHQ;
            moving();
            return;
        } else {
            for(Direction dir : notFlooded){
                if(ActionHelper.tryDrop(dir, rc)){
                    raidState = RaidState.KIDNAP;
                    System.out.println("BUZZ: Switching to KIDNAP state");
                    return;
                }
            }

            if(rc.isReady()){
                for(Direction dir : Constants.DIRECTIONS) {
                    if (ActionHelper.tryDrop(dir, rc)) {
                        raidState = RaidState.KIDNAP;
                        System.out.println("BUZZ: switching to KIDNAP state");
                        return;
                    }
                }

            }
        }

    }

    private void kidnap() throws GameActionException {
        RobotInfo robots[] = rc.senseNearbyRobots(-1, enemy);

        int closest = Integer.MAX_VALUE;
        RobotInfo victim = null;

        if(robots.length <= 0){
            // TODO: This shouldn't happen, but what if it does? Maybe move towards any production buildings

        } else {
            for(RobotInfo robot : robots){
                if(robot.getType() == RobotType.MINER
                        || robot.getType() == RobotType.LANDSCAPER
                        || robot.getType() == RobotType.COW){

//                    if(ActionHelper.tryPickup(victim.ID, rc)){
//                        holding = victim;
//                        targetLocation = nearestWater;
//                        raidState = RaidState.DROPPING;
//                        return;
//                    }

                    if(robot.getLocation().distanceSquaredTo(rc.getLocation()) < closest){
                        closest = robot.getLocation().distanceSquaredTo(rc.getLocation());
                        victim = robot;
                    }
                }
            }

            if(victim != null){

                if(victim.getLocation().isAdjacentTo(rc.getLocation())){
                    if(ActionHelper.tryPickup(victim.ID, rc)){
                        holding = victim;
                        targetLocation = nearestWater;
                        raidState = RaidState.DROPPING;
                        System.out.println("KIDNAP: Switching to DROPPING state");
                        return;
                    }

                } else {
                    targetLocation = victim.getLocation();
                    raidState = RaidState.MOVING;
                    moving();
                    return;
                }
            }
        }
        Simple.moveToLocationFuzzy(targetLocation, rc);
        //Simple.tryMove(rc);


    }

    // TODO: maybe if enough bytecode check for near water

    private void dropping() throws GameActionException {
        for (int i = 0; i < Constants.DIRECTIONS.length; i++){
            if(rc.senseFlooding(rc.getLocation().add(Constants.DIRECTIONS[i])) && ActionHelper.tryDrop(Constants.DIRECTIONS[i], rc)){
                holding = null;
                raidState = RaidState.KIDNAP;
                System.out.println("DROPPING: Switching to KIDNAP state");
                return;
            }
        }


        if(rc.getLocation().isAdjacentTo(nearestWater)){
            if(ActionHelper.tryDrop(rc.getLocation().directionTo(nearestWater), rc)){
                holding = null;
                raidState = RaidState.KIDNAP;
                System.out.println("DROPPING: Switching to KIDNAP state");
                return;
            }
        }
        targetLocation = nearestWater;
        moving();
        return;
    }

    private void moving() throws GameActionException {
        if(rc.getLocation().isAdjacentTo(targetLocation)){
            targetLocation = null;
            // TODO: This isn't the most effecient, but if we call this we enter an infinite loop sometimes (like if the unit is blocked)
            //raid();
            return;
        } else {
            //Simple.tryMove(rc.getLocation().directionTo(targetLocation), rc);
            Simple.moveToLocationFuzzy(targetLocation, rc);
            return;
        }
    }
}
