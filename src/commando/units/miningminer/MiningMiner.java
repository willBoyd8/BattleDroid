package commando.units.miningminer;

import battlecode.common.*;
import commando.base.KillMeNowException;
import commando.base.MobileUnit;
import commando.communication.Bucket;
import commando.communication.Tell;
import commando.pathing.Simple;
import commando.utility.ActionHelper;
import commando.utility.Constants;
import commando.utility.DroidList;
import commando.utility.Unsorted;

import java.util.Random;

public class MiningMiner extends MobileUnit {
    public MiningState state;
    public int totalDist;
    public int distTraveled;
    private boolean announced;
    public Direction travelDir;
    public DroidList<MapLocation> depositLocations;
    public DroidList<MapLocation> knownSoup;
    public DroidList<MapLocation> locationsToSend;
    private Bucket buck;
    private Tell tell;
    MapLocation[] broadcasted;

    public MiningMiner(RobotController rc){
        super(rc);
        state = MiningState.LOOK;
        targetLocation = null;
        depositLocations = new DroidList<MapLocation>();
        knownSoup = new DroidList<MapLocation>();
        locationsToSend = new DroidList<MapLocation>();
        depositLocations.add(hqLocation);
        totalDist = 0;
        distTraveled = 0;
        buck = new Bucket(rc);
        tell = new Tell(rc);
        broadcasted = null;
        try {
            catchup();
        } catch (Exception e){
            System.out.println("Couldn't catch up on messages");
        }
    }

    enum MiningState {
        LOOK,
        MOVE_TO_SOUP,
        ARRIVED,
        MINING,
        MOVE_TO_DEPOSIT,
        DEPOSIT,
        SEARCH,
        FLOODING,
    }

    public void turn() throws GameActionException, KillMeNowException {
        if(rc.getLocation().distanceSquaredTo(hqLocation) <= 8 && rc.getRoundNum() > Constants.WALL_START_ROUND){
            state = MiningState.FLOODING;
        }

        if(rc.getRoundNum() > Constants.WALL_START_ROUND && hqLocation != null){
            depositLocations.remove(hqLocation);
        }

        scanDepositLocations();
        lookForSoup();
        send();

        switch(state){
            case LOOK: look(); break;
            case MOVE_TO_SOUP: moveToSoup(); break;
            case ARRIVED: arrived(); break;
            case MOVE_TO_DEPOSIT: moveToDeposit(); break;
            case MINING: mining(); break;
            case SEARCH: search(); break;
            case DEPOSIT: deposit(); break;
            // Protocal 13 is the immediate evacuation of all imperial forces from a planet
            case FLOODING: protocal13(); break;
        }
    }

    private void look() throws GameActionException {

        if(knownSoup.size() > 0){
            targetLocation = Unsorted.getClosestMapLocation(knownSoup, rc);
            state = MiningState.MOVE_TO_SOUP;
            moveToSoup();
            return;
        } else {
            state = MiningState.SEARCH;
//            buck.Listen();
//            broadcasted = buck.getAnnouncedLocation();
//            if(broadcasted.length > 0) { // a mining location has been announced, free miners listen up
//                targetLocation = Unsorted.getClosestMapLocation(broadcasted, rc); // just focus on the first item for now
//                announced = false;
//                state = MiningState.MOVE_TO_SOUP;
//                moveToSoup();
//                return;
//            }
            search();
            return;
        }
    }

    private void moveToSoup() throws GameActionException {
        if(rc.canSenseLocation(targetLocation)){
            if(rc.senseSoup(targetLocation) <= 0){
                knownSoup.remove(targetLocation);
                if(knownSoup.size() > 0){
                    targetLocation = knownSoup.get(0);
                } else {
                    targetLocation = null;
                    state = MiningState.SEARCH;
                    search();
                    return;
                }
            }
        }

        if(rc.getLocation().isAdjacentTo(targetLocation) || rc.getLocation().equals(targetLocation)){
            state = MiningState.ARRIVED;
            arrived();
            return;
        } else {
            targetLocation = Unsorted.getClosestMapLocation(knownSoup, rc);
            Simple.moveToLocationFuzzy(targetLocation, rc);
            // Tries to move straight towards destination, but jiggle paths if it needs to.
//            if(!Simple.tryMove(rc.getLocation().directionTo(targetLocation), rc)){
//                if(rand.nextBoolean()){
//                    if(!Simple.tryMove(rc.getLocation().directionTo(targetLocation).rotateLeft(), rc)){
//                        if(!Simple.tryMove(rc.getLocation().directionTo(targetLocation).rotateRight(), rc)){
//                            Simple.tryMove(rc);
//                        }
//                    }
//                } else {
//                    if(!Simple.tryMove(rc.getLocation().directionTo(targetLocation).rotateRight(), rc)){
//                        if(!Simple.tryMove(rc.getLocation().directionTo(targetLocation).rotateLeft(), rc)){
//                            Simple.tryMove(rc);
//                        }
//                    }
//                }
//            }
        }
    }

    private void arrived() throws GameActionException {
        if(rc.canSenseLocation(targetLocation)){
            if(rc.senseSoup(targetLocation) <= 0){
                state = MiningState.LOOK;
                knownSoup.remove(targetLocation);
                targetLocation = null;
                look();
                return;
            } else {
//                if(!announced) {
                    tell.announceSoupLocation(rc.getLocation().x,rc.getLocation().y); // announce location of soup
                    tell.forceSend(0);
                    announced = true;
//                }
                state = MiningState.MINING;
                mining();
                return;
            }
        }
    }

    private void mining() throws GameActionException {
        if(rc.getSoupCarrying() >= RobotType.MINER.soupLimit){
            state = MiningState.MOVE_TO_DEPOSIT;
            moveToDeposit();
            return;
        } else {
            if(rc.senseSoup(targetLocation) > 0){
                ActionHelper.tryMine(rc.getLocation().directionTo(targetLocation), rc);
                return;
            } else {
                state = MiningState.LOOK;
                targetLocation = null;
                look();
            }
        }
    }

    private void moveToDeposit() throws GameActionException {
        MapLocation closestDepositLocation = getClosestDepositLocation();
        if(rc.getTeamSoup() > RobotType.REFINERY.cost && closestDepositLocation.distanceSquaredTo(rc.getLocation()) > Constants.MIN_REFINERY_SPREAD_DISTANCE && hqLocation != null && rc.getLocation().distanceSquaredTo(hqLocation) > Constants.MIN_REFINERY_SPREAD_DISTANCE){
            for(Direction dir : Constants.DIRECTIONS){
                if(ActionHelper.tryBuild(RobotType.REFINERY, dir, rc)){
                    return;
                }
            }
            return;
        }

        if(rc.getLocation().isAdjacentTo(closestDepositLocation) || rc.getLocation().equals(closestDepositLocation)){
            state = MiningState.DEPOSIT;
            deposit();
            return;
        } else {
            // Tries to move straight towards destination, but jiggle paths if it needs to.
            if(!Simple.tryMove(rc.getLocation().directionTo(closestDepositLocation), rc)){
                if(rand.nextBoolean()){
                    if(!Simple.tryMove(rc.getLocation().directionTo(closestDepositLocation).rotateLeft(), rc)){
                        if(!Simple.tryMove(rc.getLocation().directionTo(closestDepositLocation).rotateRight(), rc)){
                            Simple.tryMove(rc);
                        }
                    }
                } else {
                    if(!Simple.tryMove(rc.getLocation().directionTo(closestDepositLocation).rotateRight(), rc)){
                        if(!Simple.tryMove(rc.getLocation().directionTo(closestDepositLocation).rotateLeft(), rc)){
                            Simple.tryMove(rc);
                        }
                    }
                }
            }
        }
    }

    private void deposit() throws GameActionException {
        if(rc.getSoupCarrying() <= 0){
            state = MiningState.MOVE_TO_SOUP;
            moveToSoup();
        } else {
            ActionHelper.tryDeposit(rc.getLocation().directionTo(depositLocations.get(0)), rc);
            return;
        }
    }

    private void protocal13() throws GameActionException, KillMeNowException {
        throw new KillMeNowException();
    }

    private void search() throws GameActionException {
        //ActionHelper.tryMove(rc);
        if(broadcasted != null) {
            // DO PATHFINDING to location variable 'broadcasted'
            // or moveToDeposit at 'broadcasted' map location
            broadcasted = null; // reset to null
        }
        if (totalDist == 0) {
            Random r = new Random();
            int dist = r.nextInt((rc.getMapHeight() + rc.getMapWidth()) / 2);
            totalDist = dist;
            Random rand = new Random();
            int dir = 0;
            for (int i = 0; i < Constants.DIRECTIONS.length; i++) {
                dir = rand.nextInt(Constants.DIRECTIONS.length);
                travelDir = Constants.DIRECTIONS[dir];
                if (Simple.tryMove(travelDir, rc)){
                    totalDist--;
                    state = MiningState.LOOK;
                    targetLocation = null;
                    look();
                    return;
                }
            }


        } else {
            if (Simple.tryMove(travelDir, rc)){
                totalDist--;
                state = MiningState.LOOK;
                targetLocation = null;
                look();
                return;
            } else {
                totalDist = 0;
                state = MiningState.LOOK;
                targetLocation = null;
                look();
                return;
            }
        }

    }

    public MapLocation getClosestDepositLocation() throws GameActionException {
        MapLocation best = null;
        int closest = Integer.MAX_VALUE;

        for(MapLocation loc : depositLocations){
            if(loc.distanceSquaredTo(rc.getLocation()) < closest){
                best = loc;
                closest = loc.distanceSquaredTo(rc.getLocation());
            }
        }

        return best;
    }

    private void scanDepositLocations() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ || robot.getType() == RobotType.REFINERY){
                if(!depositLocations.contains(robot.getLocation())){
                    depositLocations.add(robot.getLocation());
                }
            }
        }

    }

    public void catchup() throws GameActionException{
        int counter = 1;
        while(counter < rc.getRoundNum()){
            checkMessages(counter);
            counter++;
        }
    }

    public void checkMessages(int roundNum) throws GameActionException{
        Transaction[] transactions = rc.getBlock(roundNum);

        for(Transaction trans : transactions){
            int[] message = trans.getMessage();
            if(message[0] == 31415926){
                MapLocation loc = new MapLocation(message[1], message[2]);
                if(!knownSoup.contains(loc)){
                    knownSoup.add(loc);
                }
            }
        }
    }

    public void lookForSoup() throws GameActionException {
        for(MapLocation loc : ActionHelper.getSoupLocations(rc)){
            if(!knownSoup.contains(loc)){
                knownSoup.add(loc);
                locationsToSend.add(loc);

            }
        }

        checkMessages(rc.getRoundNum() - 1);
    }

    public void send() throws GameActionException {
        if(locationsToSend.size() > 0) {

            int[] message = {31415926, locationsToSend.get(0).x, locationsToSend.get(0).y, 0, 0, 0, 0};
            if (rc.canSubmitTransaction(message, 1)) {
                rc.submitTransaction(message, 1);
                locationsToSend.remove(0);
            }
        }
    }

}
