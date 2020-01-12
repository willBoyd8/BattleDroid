package droideka.communication;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Transaction;

//enum BotTypes { DRONE, DESIGN_SCHOOL, FULFILLMENT_CENTER, HQ, LANDSCAPER, MINER, NET_GUN, REFINERY, VAPORATOR }


public class Bucket {

    RobotController rc;
    int block = 0;
    int round = 0;
    int[] blocks;
    // what we want to do is wait for a bot to send something, and then listen
    public Bucket(RobotController loadRC) {
        rc = loadRC;
        round = rc.getRoundNum();
    }

    public int[] Listen() throws GameActionException { // need round index
        Transaction trans[] = rc.getBlock(round);
        Message msg = new Message();
        blocks = new int[trans.length];
        int[] messy = new int[trans.length];
        int count = 0;
        for(int i = 0; i < trans.length; i++) {
            messy = trans[i].getMessage();
            for(int j = 0; j < messy.length; j++) {
                msg.loadBlock(messy[j]);
                if(msg.getMessageType() == 0x33) // it is our message. this should be how messages work...
                    blocks[count] = msg.getMessage();
                    count++;
            }
        }
        // we should now have all messages intended for our bots in blocks[]
        return blocks;
    }

    public int[] forMiners() {
        Message msg = new Message();
        int[] forUs = new int[blocks.length];
        int count = 0;
        for(int i = 0; i < blocks.length; i++) {
            msg.loadBlock(blocks[i]);
            if(msg.getBotType() == 5) {
                forUs[count++] = msg.getMessage();
            }
        }
        return forUs;
    }

    public MapLocation[] getAnnouncedLocation() {
        // just for miners
        int[] possibleLocs = forMiners();
        int count = 0;
        MapLocation[] loc = new MapLocation[possibleLocs.length];
        for(int i = 0; i < possibleLocs.length; i++) {
            Message msg = new Message();
            msg.loadBlock(possibleLocs[i]);
            switch(msg.getData()) {
                case 0x4D:
                    break;
                case 0x44:
                    break;
                case 0x4D53:
                    break;
                case 0x4D46:
                    break;
                default:
                    // this is probably a location, parse it
                    loc[count] = msg.getMinerLocation();
                    count++;
                    break;
            }
        }
        if(count < 1)
            return null;
        return loc;
    }
}
