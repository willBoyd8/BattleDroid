package commando.units.probedroid;

import battlecode.common.*;
import commando.base.MobileUnit;
import commando.communication.CommunicationHelper;
import commando.pathing.Bug;
import commando.pathing.Simple;
import commando.units.laticelandscaper.LaticeLandscaper;
import commando.utility.*;


public class ProbeDroid extends MobileUnit {

    DroidList<MapLocation> enemyHQLocations;
    DroneState state;
    DroidList<MapLocation> wallLocations;
    int gridOffsetX, gridOffsetY;
    int homeQuad;
    RobotInfo closestNetGun;
    RobotInfo target;
    MapLocation targetSpot;
    MapLocation patrolPoint;
    MapLocation nearestFlooding;
    Bug path;
    DroidList<RobotInfo> knownNetGuns;
    DroidList<MapLocation> knownFlooding;
    DroidList<MapLocation> enemyHQBlacklist;
    DroidList<MapLocation> patrolPath;
    boolean combineToFormBarrier;

    public ProbeDroid (RobotController rc) {
        super(rc);
        wallLocations = new DroidList<>();
        enemyHQBlacklist = new DroidList<>();
        patrolPath = new DroidList<>();
        knownNetGuns = new DroidList<>();
        knownFlooding = new DroidList<>();
        closestNetGun = null;
        target = null;
        targetSpot = null;
        patrolPoint = null;
        nearestFlooding = null;
        path = null;
        combineToFormBarrier = false;
        state = DroneState.PATROL;
        homeQuad = 0;
        gridOffsetX = 0;
        gridOffsetY = 0;

    }

    enum DroneState {
        PATROL,
        INTERCEPT,
        HELP,
        DROPOFF,
        DROPOFF_DRONE,
        RETURNING,
        ROAMING,
    }


    public void onInitialization() throws GameActionException {
        catchup();

        if(hqLocation.x % 2 == 0){
            gridOffsetX = 1;
        }
        if(hqLocation.y % 2 == 0){
            gridOffsetY = 1;
        }
        Unsorted.updateKnownNetguns(knownNetGuns, rc);
        int radius = Constants.PATROL_RADIUS;
        int neg_radius = Constants.PATROL_RADIUS * -1;
        homeQuad = Unsorted.getHQQuadrant(hqLocation, rc);

        //For the purpose of accounting for the edge cases of us being either close to or up against the map edge

        //case 1: HQ is not close to or against map edge
        //Path: Circle the HQ
        if (((hqLocation.x >= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.x <= (rc.getMapWidth() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD))) && ((hqLocation.y >= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) && (hqLocation.y <= (rc.getMapHeight() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD)))) {
            //No quadrant specific paths
            patrolPath.add(hqLocation.translate(radius, radius));
            patrolPath.add(hqLocation.translate(neg_radius, radius));
            patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
            patrolPath.add(hqLocation.translate(radius, neg_radius));
        }

        //case 2: HQ is in a corner
        //Path: Semi-circle on the side of the HQ facing the center of the map
        //Endpoints: Depending on the corner, 1 - North/South; 2 - East/West
        else if (((hqLocation.x <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.x >= (rc.getMapWidth() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD))) && ((hqLocation.y <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.y >= (rc.getMapHeight() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD)))) {
            switch (homeQuad){
                case 1:
                    patrolPath.add(hqLocation.translate(0, neg_radius));
                    patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
                    patrolPath.add(hqLocation.translate(neg_radius, 0));
                    patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
                    break;

                case 2:
                    patrolPath.add(hqLocation.translate(0, neg_radius));
                    patrolPath.add(hqLocation.translate(radius, neg_radius));
                    patrolPath.add(hqLocation.translate(radius, 0));
                    patrolPath.add(hqLocation.translate(radius, radius));
                    break;

                case 3:
                    patrolPath.add(hqLocation.translate(0, radius));
                    patrolPath.add(hqLocation.translate(radius, radius));
                    patrolPath.add(hqLocation.translate(radius, 0));
                    patrolPath.add(hqLocation.translate(radius, radius));
                    break;

                case 4:
                    patrolPath.add(hqLocation.translate(0, neg_radius));
                    patrolPath.add(hqLocation.translate(neg_radius, radius));
                    patrolPath.add(hqLocation.translate(neg_radius, 0));
                    patrolPath.add(hqLocation.translate(neg_radius, radius));
                    break;
            }
        }

        //case 3: HQ is not in a corner but is against the bottom of the map
        //Path: Semi-circle on the side of the HQ facing the center of the map
        //Endpoints: 1 - EAST; 2 - WEST
        else if (((hqLocation.x <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.x >= (rc.getMapWidth() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD))) && ((hqLocation.y >= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) && (hqLocation.y <= (rc.getMapHeight() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD)))) {
            if (hqLocation.y <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD){
                //Bottom of the Map
                patrolPath.add(hqLocation.translate(radius, 0));
                patrolPath.add(hqLocation.translate(radius, radius));
                patrolPath.add(hqLocation.translate(neg_radius, radius));
                patrolPath.add(hqLocation.translate(neg_radius, 0));
                patrolPath.add(hqLocation.translate(neg_radius, radius));
                patrolPath.add(hqLocation.translate(radius, radius));
            }
            else{
                patrolPath.add(hqLocation.translate(neg_radius,0));
                patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
                patrolPath.add(hqLocation.translate(radius, neg_radius));
                patrolPath.add(hqLocation.translate(radius, 0));
                patrolPath.add(hqLocation.translate(radius, neg_radius));
                patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
            }
        }

        //case 4: HQ is not in a corner but is against the side of the map
        //Path: Semi-circle on the side of the HQ facing the center of the map
        //Endpoints: 1 - SOUTH; 2 - NORTH
        else if (((hqLocation.x >= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) && (hqLocation.x <= (rc.getMapWidth() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD))) && ((hqLocation.y <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.y >= (rc.getMapHeight() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD)))) {
            if (hqLocation.x <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) {
                patrolPath.add(hqLocation.translate(0, neg_radius));
                patrolPath.add(hqLocation.translate(radius, neg_radius));
                patrolPath.add(hqLocation.translate(radius, radius));
                patrolPath.add(hqLocation.translate(0, radius));
                patrolPath.add(hqLocation.translate(radius, radius));
                patrolPath.add(hqLocation.translate(radius, neg_radius));
            }
            else {
                patrolPath.add(hqLocation.translate(0, neg_radius));
                patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
                patrolPath.add(hqLocation.translate(neg_radius, radius));
                patrolPath.add(hqLocation.translate(0, radius));
                patrolPath.add(hqLocation.translate(neg_radius, radius));
                patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
            }
        }

        //case 5: there is some serious issue in the code if it executes this
        else {
            System.out.println("I guess we have either a logic error or a some kind of edge case");
        }

        patrolPoint = patrolPath.get(0);
        Bug path = new Bug(rc.getLocation(), patrolPoint, rc);
        enemyHQLocations = Unsorted.generatePossibleEnemyHQLocation(hqLocation, rc);
        enemyHQBlacklist.removeAll(enemyHQBlacklist);
    }

    public void turn() throws GameActionException {
        Unsorted.updateKnownFlooding(knownFlooding, rc);
        switch(state){
            case PATROL: patrolling(); break;
            case INTERCEPT: intercepting(); break;
            case HELP: helping(); break;
            case DROPOFF: droppingOff(); break;
            case DROPOFF_DRONE: droppingOffDrone(); break;
            case RETURNING: returning(); break;
            case ROAMING: roam(); break;
        }
    }

    public void patrolling() throws GameActionException {
        Unsorted.updateKnownNetguns(knownNetGuns, rc);
        target = checkForTargets();

        if (target != null) {
            //There is a target, switch to intercept mode
            Bug path = new Bug(rc.getLocation(), target.location, rc);
            targetSpot = target.location;
            state = DroneState.INTERCEPT;
            intercepting();
            return;
        } else {
            //There is no target, stay in patrol mode
            if (rc.getLocation().isAdjacentTo(patrolPoint)){
                //Patrol point reached, setting next patrol point
                patrolPath.add(patrolPath.get(0));
                patrolPath.remove(0);
                patrolPoint = patrolPath.get(0);
                Bug path = new Bug(rc.getLocation(), patrolPoint, rc);
                path.run();
            } else {
                //Patrol point not reached, moving to patrol point
                path.run();
            }
        }
    }

    public void intercepting() throws GameActionException {
        //If the drone has reached and is adjacent to target
        if (rc.getLocation().isAdjacentTo(target.location)) {
            if (rc.canPickUpUnit(target.ID)) {
                rc.pickUpUnit(target.ID);
                if (target.team == myTeam) {
                    state = DroneState.HELP;
                }
                if (target.type == RobotType.DELIVERY_DRONE) {
                    closestNetGun = Unsorted.getClosestUnit(knownNetGuns, rc);
                    Bug path = new Bug(rc.getLocation(), closestNetGun.location, rc);
                    state = DroneState.DROPOFF_DRONE;
                    droppingOffDrone();
                    return;
                } else {
                    state = DroneState.DROPOFF;
                    droppingOff();
                }
            } else {
                //Shouldn't ever reach this point, but just in case putting in path.run to try and have it move somewhere else and hopefully be able to pick it up
                path.run();
            }
        }
        //A quick check of whether it can still sense should solve the potential issue of multiple drones going for a single unit
        if (rc.canSenseRobot(target.ID)) {
            //For when the target inevitably moves while moving to intercept
            if (targetSpot != target.location) {
                //If target changed locations since switching to intercept mode, then update path object
                targetSpot = target.location;
                Bug path = new Bug(rc.getLocation(), targetSpot, rc);
            }
            //Once path has been corrected or if target didn't move, move to target
            path.run();
        } else {
            //Switch targets if there are more targets
            target = checkForTargets();

            if (target != null) {
                Bug path = new Bug(rc.getLocation(), target.location, rc);
                targetSpot = target.location;
                //Since a new target was found, call intercepting() again to go through
                //1. Checking if it's adjacent to the new target
                //      - Pick up new target and switch to Dropoff mode
                //2. Otherwise move towards target
                //Should definitely be able to sense the target if it found a new one, so it theoretically shouldn't reach this point more than once in a turn
                intercepting();
                return;
            } else {
                //No Valid targets found, switching to return state
                target = null;
                state = DroneState.RETURNING;
            }
        }
    }

    public void helping() throws GameActionException {
        DroidList<MapLocation> laticeTiles = Unsorted.lookForLatice(gridOffsetX, gridOffsetY, rc);
        if (!(laticeTiles.isEmpty())){
            for (MapLocation spot : laticeTiles){
                if ((rc.getLocation().isAdjacentTo(spot))&&(rc.canDropUnit(rc.getLocation().directionTo(spot)))){
                    rc.dropUnit(rc.getLocation().directionTo(spot));
                    target = null;
                    state = DroneState.RETURNING;
                    returning();
                    return;
                }
            }
        }
        Bug path = new Bug(rc.getLocation(), hqLocation, rc);
        path.run();
    }

    public void droppingOff() throws GameActionException {
        MapLocation nearestWater = Unsorted.getClosestMapLocation(knownFlooding, rc);
        if (rc.getLocation().isAdjacentTo(nearestWater)) {
            if (rc.canDropUnit(rc.getLocation().directionTo(nearestWater))) {
                rc.dropUnit(rc.getLocation().directionTo(nearestWater));
                target = null;
                state = DroneState.RETURNING;
                returning();
                return;
            }
        }

        if (!(nearestWater.equals(nearestFlooding))) {
            nearestFlooding = nearestWater;
            Bug path = new Bug(rc.getLocation(), nearestWater, rc);
            path.run();
        } else {
            path.run();
        }
    }

    public void droppingOffDrone() throws GameActionException {
        if (rc.getLocation().distanceSquaredTo(closestNetGun.location) <= GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED){
            boolean ableToDrop;
            DroidList<Direction> validDrops = new DroidList<Direction>();
            for (Direction dropDir : Constants.DIRECTIONS){
                if (rc.getLocation().add(dropDir).distanceSquaredTo(closestNetGun.location) <= GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED){
                    if (rc.canDropUnit(dropDir)){
                        rc.dropUnit(dropDir);
                        target = null;
                        state = DroneState.RETURNING;
                        returning();
                        return;
                    }
                }
            }
            path.run();
        } else {
            path.run();
        }
    }

    public void returning() throws GameActionException {
        //Purpose is to reset Bug path to the last patrol point and get it back on its patrol path;
        Bug path = new Bug(rc.getLocation(), patrolPoint, rc);
        state = DroneState.PATROL;
        patrolling();
        return;

    }

    public void roam() throws GameActionException {

    }

    public void catchup() throws GameActionException{
        int counter = 1;
        while(counter < rc.getRoundNum()){
            checkMessages(counter);
            counter++;
        }
    }

    public void checkMessages() throws GameActionException {
        checkMessages(rc.getRoundNum() - 1);
    }

    public void checkMessages(int roundNumber) throws GameActionException{
        Transaction[] transactions = rc.getBlock(roundNumber);

        for(Transaction trans : transactions){
            int[] message = trans.getMessage();
            if(message[0] == Constants.MESSAGE_KEY){
                processMessage(message);
            }
        }
    }

    public void processMessage(int[] message){
        switch(message[1]){
            case 0:
                hqLocation = CommunicationHelper.convertMessageToLocation(message[2]);
                hqElevation = message[3];
                break;
            case 1:
                wallLocations.remove(CommunicationHelper.convertMessageToLocation(message[2]));
                break;
            case 2:
                wallLocations.add(CommunicationHelper.convertMessageToLocation(message[2]));
                //state = LaticeLandscaper.LaticeState.MOVING_TO_WALL;
                break;
            case 3:
                break;
            case 4:
                enemyHQBlacklist.add(CommunicationHelper.convertMessageToLocation(message[2]));
                if(targetLocation.equals(CommunicationHelper.convertMessageToLocation(message[2]))){
                    targetLocation = null;
                }
                break;
            case 5:
                enemyHQ = CommunicationHelper.convertMessageToLocation(message[2]);
                targetLocation = null;
                break;
        }

    }

    public RobotInfo checkForTargets() {
        RobotInfo targetFound = null;
        RobotInfo threats[] = rc.senseNearbyRobots(-1, enemy);
        RobotInfo friendlies[] = rc.senseNearbyRobots(-1, myTeam);
        if (threats.length >= 1){
            //Enemy spotted
            targetFound = Unsorted.getClosestUnit(threats, rc);
        } else if (!(Unsorted.checkForHelpNeeded(friendlies, gridOffsetX, gridOffsetY, rc).isEmpty())) {
            //No enemies spotted, Unit in need of assistance spotted
            targetFound = Unsorted.getClosestUnit(Unsorted.checkForHelpNeeded(friendlies, gridOffsetX, gridOffsetY, rc), rc);
        } else {
            //No Enemies or Units in need of assistance spotted, should switch to return state
            targetFound = null;
        }

        return targetFound;
    }

}