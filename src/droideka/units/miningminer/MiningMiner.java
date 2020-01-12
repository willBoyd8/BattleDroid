package droideka.units.miningminer;

import battlecode.common.*;
import droideka.base.KillMeNowException;
import droideka.base.MobileUnit;
import droideka.utility.ActionHelper;
import droideka.utility.Constants;
import droideka.communication.Bucket;
import droideka.communication.Tell;

import java.util.ArrayList;
import java.util.Random;

public class MiningMiner extends MobileUnit {
    public MiningState state;
    public int totalDist;
    public int distTraveled;
    private boolean announced;
    public Direction travelDir;
    public ArrayList<MapLocation> depositLocations;
    private Bucket buck;
    private Tell tell;
    MapLocation broadcasted;
    // TODO: Implement looking for refineries occasionally

    public MiningMiner(RobotController rc){
        super(rc);
        state = MiningState.LOOK;
        targetLocation = null;
        depositLocations = new ArrayList<MapLocation>();
        depositLocations.add(hqLocation);
        totalDist = 0;
        distTraveled = 0;
        buck = new Bucket(rc);
        tell = new Tell(rc);
        broadcasted = null;
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
        if(rc.getRoundNum() > Constants.WALL_START_ROUND){
            state = MiningState.FLOODING;
        }

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
        ArrayList<MapLocation> soups = ActionHelper.getSoupLocations(rc);

        if(soups.size() > 0){
            targetLocation = soups.get(0);
            state = MiningState.MOVE_TO_SOUP;
            moveToSoup();
            return;
        } else {
            state = MiningState.SEARCH;
            if(announced) { // a mining location has been announced, free miners listen up
                buck.Listen();
                broadcasted = buck.getAnnouncedLocation()[0]; // just focus on the first item for now
                announced = false;
            }
            search();
            return;
        }
    }

    private void moveToSoup() throws GameActionException {
        if(rc.getLocation().isAdjacentTo(targetLocation) || rc.getLocation().equals(targetLocation)){
            state = MiningState.ARRIVED;
            arrived();
            return;
        } else {
            // Tries to move straight towards destination, but jiggle paths if it needs to.
            if(!ActionHelper.tryMove(rc.getLocation().directionTo(targetLocation), rc)){
                if(rand.nextBoolean()){
                    if(!ActionHelper.tryMove(rc.getLocation().directionTo(targetLocation).rotateLeft(), rc)){
                        if(!ActionHelper.tryMove(rc.getLocation().directionTo(targetLocation).rotateRight(), rc)){
                            ActionHelper.tryMove(rc);
                        }
                    }
                } else {
                    if(!ActionHelper.tryMove(rc.getLocation().directionTo(targetLocation).rotateRight(), rc)){
                        if(!ActionHelper.tryMove(rc.getLocation().directionTo(targetLocation).rotateLeft(), rc)){
                            ActionHelper.tryMove(rc);
                        }
                    }
                }
            }
        }
    }

    private void arrived() throws GameActionException {
        if(rc.canSenseLocation(targetLocation)){
            if(rc.senseSoup(targetLocation) <= 0){
                state = MiningState.LOOK;
                targetLocation = null;
                look();
                return;
            } else {
                if(!announced) {
                    tell.announceSoupLocation(rc.getLocation().x,rc.getLocation().y); // announce location of soup
                    tell.forceSend();
                    announced = true;
                }
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
                look();
            }
        }
    }

    private void moveToDeposit() throws GameActionException {
        if(rc.getLocation().isAdjacentTo(depositLocations.get(0)) || rc.getLocation().equals(depositLocations.get(0))){
            state = MiningState.DEPOSIT;
            deposit();
            return;
        } else {
            // Tries to move straight towards destination, but jiggle paths if it needs to.
            if(!ActionHelper.tryMove(rc.getLocation().directionTo(depositLocations.get(0)), rc)){
                if(rand.nextBoolean()){
                    if(!ActionHelper.tryMove(rc.getLocation().directionTo(depositLocations.get(0)).rotateLeft(), rc)){
                        if(!ActionHelper.tryMove(rc.getLocation().directionTo(depositLocations.get(0)).rotateRight(), rc)){
                            ActionHelper.tryMove(rc);
                        }
                    }
                } else {
                    if(!ActionHelper.tryMove(rc.getLocation().directionTo(depositLocations.get(0)).rotateRight(), rc)){
                        if(!ActionHelper.tryMove(rc.getLocation().directionTo(depositLocations.get(0)).rotateLeft(), rc)){
                            ActionHelper.tryMove(rc);
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
        // TODO: implement intelligent searching
        //ActionHelper.tryMove(rc);
        if(broadcasted != null) {
            // DO PATHFINDING to location variable 'broadcasted'
            // or moveToDeposit at 'broadcasted' map location
            broadcasted = null; // reset to null
        }
        if (totalDist == 0) {
            Random r = new Random();
            int dist = r.nextInt(9);
            totalDist = dist;
            Random rand = new Random();
            int dir = 0;
            for (int i = 0; i < Constants.DIRECTIONS.length; i++) {
                dir = rand.nextInt(Constants.DIRECTIONS.length);
                travelDir = Constants.DIRECTIONS[dir];
                if (ActionHelper.tryMove(travelDir, rc)){
                    totalDist--;
                    state = MiningState.LOOK;
                    look();
                    return;
                }
            }


        } else {
            if (ActionHelper.tryMove(travelDir, rc)){
                totalDist--;
                state = MiningState.LOOK;
                look();
                return;
            } else {
                totalDist = 0;
                state = MiningState.LOOK;
                look();
                return;
            }
        }




    }

}
