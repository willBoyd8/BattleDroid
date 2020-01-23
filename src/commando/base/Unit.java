package commando.base;
import battlecode.common.*;
import commando.utility.*;

import java.util.Random;

interface TalksOverBlockChain {
    void checkMessages() throws GameActionException;
    void checkMessages(int roundNumber) throws GameActionException;
    void processMessage(int[] message);

    /**
     * Handle the setting of the Enemy's HQ
     * @param message a message array
     */
    void SetEnemyHQLocationHandler(int[] message);

    /**
     * Handle adding an enemy HQ blacklist location
     * @param message a message array
     */
    void EnemyHQBlacklistAddMessageHandler(int[] message);

    /**
     * Add a wall location to the list of wall locations
     * @param message a message array
     */
    void AddWallLocationMessageHandler(int[] message);

    /**
     * Handle removing a wall location from the list of wall locations
     * @param message a message array
     */
    void RemoveWallLocationMessageHandler(int[] message);

    /**
     * Handle setting the team's HQ location
     * @param message a message array
     */
    void SetHQLocationMessageHandler(int[] message);
}

public abstract class Unit implements TalksOverBlockChain {
    public RobotController rc;
    public MapLocation spawn;
    public Team enemy;
    public Team myTeam;
    public int age; // The number of rounds this unit has been alive
    public int birthday;
    public MapLocation hqLocation;
    public Random rand;
    public int hqElevation;
    public RobotInfo hqInfo;
    public MapLocation targetLocation;
    public MapLocation enemyHQ;
    public DroidList<int[]> messageQueue;
    public DroidList<MapLocation> enemyHQLocations;

    /**
     * The last round we checked messages for
     */
    private int last_checked_messages_round;

    public Unit(RobotController rc){
        this.rc = rc;
        spawn = rc.getLocation();
        myTeam = rc.getTeam();
        enemy = myTeam.opponent();
        rand = new Random();
        hqElevation = Integer.MIN_VALUE;
        targetLocation = null;
        enemyHQ = null;
        messageQueue = new DroidList<>();

        this.last_checked_messages_round = 1;

        age = 0;
    }

    public Unit(Unit unit){
        this.rc = unit.rc;
        spawn = unit.spawn;
        myTeam = unit.myTeam;
        enemy = unit.enemy;
        rand = unit.rand;
        hqElevation = unit.hqElevation;
        targetLocation = unit.targetLocation;
        hqLocation = unit.hqLocation;
        hqInfo = unit.hqInfo;

        this.last_checked_messages_round = 1;

        age = unit.age;
    }

    public final void run() throws KillMeNowException {
        System.out.println("I'm a " + rc.getType().toString());

        try {
            onInitialization();

            //noinspection InfiniteLoopStatement
            while (true) {

                try {
                    this.catchup();
                    preStart();
                    roundStart();
                    turn();
                    preEnd();
                    roundEnd();
                } catch (KillMeNowException e) {
                    ForTheGloryOfTheEmpire.print(rc);
                    return;
                    //throw new KillMeNowException();
                } catch (Exception e) {
                    System.out.println(rc.getType().toString() + " Exception");
                    e.printStackTrace();
                    Clock.yield(); // if we fail, yield the clock
                }
            }
        } catch (Exception e) {
            System.out.println(rc.getType().toString() + " Exception");
            e.printStackTrace();
            Clock.yield();
        }

    }

    /**
     * This method should be implemented for every bot. This is the code that the bot will run every round
     * This is effectively where the individual unique unit types "strategy" should go.
     * @throws GameActionException
     */
    public abstract void turn() throws GameActionException, KillMeNowException;

    /**
     * This method is run at the start of every round for every unit we own
     */
    public final void roundStart(){
        age = rc.getRoundNum() - birthday; // Update age
    }

    /**
     * This method is run at the end of every round for every unit
     */
    public final void roundEnd() throws GameActionException{
        trySendMessage();

        if(targetLocation != null) {
            DebugHelper.setIndicatorLine(rc.getLocation(), targetLocation, 0, 0, 255, rc);
        }
        Clock.yield();
    }

    /**
     * Overload this method to run stuff before the normal round start gets run
     */
    public void preStart() {
    }

    /**
     * Overload this method to run stuff before the normal round ending
     */
    public void preEnd() throws GameActionException {

    }

    /**
     * Run once on startup, immediately before the main loop begins
     * @throws GameActionException
     */
    public void onInitialization() throws GameActionException {
        this.catchup();
    }

    public void trySendMessage() throws GameActionException{
        if(messageQueue.size() > 0 && rc.canSubmitTransaction(messageQueue.get(0), 1)){
            rc.submitTransaction(messageQueue.get(0), 1);
            messageQueue.remove(0);
        }
    }

    /**
     * Read through all the messages in the
     * @throws GameActionException
     */
    public void catchup() throws GameActionException{
        while(this.last_checked_messages_round < rc.getRoundNum()){
            checkMessages(this.last_checked_messages_round);
            this.last_checked_messages_round++;
        }
    }

    public void checkMessages() throws GameActionException {
        checkMessages(rc.getRoundNum() - 1);
    }

    public void checkMessages(int roundNumber) throws GameActionException {
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
                SetHQLocationMessageHandler(message);
                break;
            case 1:
                RemoveWallLocationMessageHandler(message);
                break;
            case 2:
                AddWallLocationMessageHandler(message);
                break;
            case 3:
                break;
            case 4:
                EnemyHQBlacklistAddMessageHandler(message);
                break;
            case 5:
                SetEnemyHQLocationHandler(message);
                break;
        }

    }

    public void SetEnemyHQLocationHandler(int[] message) {}

    public void EnemyHQBlacklistAddMessageHandler(int[] message) {}

    public void AddWallLocationMessageHandler(int[] message) {}

    public void RemoveWallLocationMessageHandler(int[] message) {}

    public void SetHQLocationMessageHandler(int[] message) {}

}
