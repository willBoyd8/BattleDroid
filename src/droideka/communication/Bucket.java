package droideka.communication;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Transaction;

public class Bucket {

    RobotController rc;
    int block = 0;
    int round = 0;
    // what we want to do is wait for a bot to send something, and then listen
    public Bucket(RobotController loadRC, int loadRound) {
        rc = loadRC;
        round = loadRound;
    }

    public int[] Listen() throws GameActionException { // need round index
        Transaction trans[] = rc.getBlock(round);
        Message msg = new Message();
        int[] blocks = new int[trans.length];
        int[] messy = new int[trans.length];
        int count = 0;
        for(int i = 0; i < trans.length; i++) {
            messy = trans[i].getMessage();
            for(int j = 0; j < messy.length; j++) {
                msg.loadBlock(messy[j]);
                if(msg.getMessageType() == 0x33) // it is our message. this should be how messages work...
                    blocks[count++] = msg.getMessage();
            }
        }
        // we should now have all messages intended for our bots in blocks[]
        return blocks;
    }
}
