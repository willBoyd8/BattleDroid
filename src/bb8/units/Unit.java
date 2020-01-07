package bb8.units;
import battlecode.common.*;

public abstract class Unit {
    public RobotController rc;
    public MapLocation spawn;
    public Team enemy;
    public Team myTeam;
    public int age; // The number of rounds this unit has been alive
    public int birthday;

    public Unit(RobotController rc){
        this.rc = rc;
        spawn = rc.getLocation();
        myTeam = rc.getTeam();
        enemy = myTeam.opponent();

        age = 0;
    }

    public final void run(){
        System.out.println("I'm a " + rc.getType().toString());

        while(true){
            try{
                preStart(); //Can overload in files
                roundStart(); //can only change in this file
                turn(); //Has to be implemented
                preEnd(); //Can overload in files
                roundEnd(); //can only change in this file
            } catch (Exception e){
                System.out.println(rc.getType().toString() + " Exception");
                e.printStackTrace();
                Clock.yield(); // if we fail, yield the clock
            }
        }

    }

    /**
     * This method should be implemented for every bot. This is the code that the bot will run every round
     * This is effectively where the individual unique unit types "strategy" should go.
     * @throws GameActionException
     */
    public abstract void turn() throws GameActionException;

    /**
     * This method is run at the start of every round for every unit we own
     */
    public final void roundStart(){
        age = rc.getRoundNum() - birthday; // Update age
    }

    /**
     * This method is run at the end of every round for every unit
     */
    public final void roundEnd(){
        Clock.yield();
    }

    /**
     * Overload this method to run stuff before the normal round start gets run
     */
    public void preStart(){

    }

    /**
     * Overload this method to run stuff before the normal round ending
     */
    public void preEnd(){

    }
}
