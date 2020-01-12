package droideka.communication;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import droideka.communication.Message;

enum BotTypes { DRONE, DESIGN_SCHOOL, FULFILLMENT_CENTER, HQ, LANDSCAPER, MINER, NET_GUN, REFINERY, VAPORATOR }

enum CommandType { REQUEST, COMMAND }

public class Tell {
    private int[] blocks = new int[7];
    private int count = 0;
    private int defaultSoupPrice = 10;
    RobotController rc;

    public Tell(RobotController loadRC) {
        rc = loadRC;
    }

    public int announceSoupLocation(int x, int y) throws GameActionException {
        int data = 0;
        data |= (x << 4);
        data = data | y;
        Message msg = new Message();
        msg.createMessage(0x33, 5,1,0,data);
        int block = msg.getMessage();
        blocks[count++] = block;
        if(count >= 7) {
            rc.submitTransaction(blocks, defaultSoupPrice);
            for(int i = 0; i < 7; i++)
                blocks[i] = 0;
            count = 0;
        }
        return block;
    }

    private int genericMove(int x, int y,int bot_type, int bot_id) throws GameActionException {
        // first byte of data is x, second byte is y. we can use all the room we want
        int data = 0;
        data = data | (x << 8);
        data = data | y;
        Message msg = new Message();
        msg.createMessage(0x33, bot_type,1,bot_id,data);
        int block = msg.getMessage();
        blocks[count++] = block;
        // do sending of block on blockchain
        if(count >= 7) {
            rc.submitTransaction(blocks, defaultSoupPrice);
            for(int i = 0; i < 7; i++)
                blocks[i] = 0;
            count = 0;
        }
        return block;
    }

    public class Drone {
        public int move(int x, int y, int bot_id) throws GameActionException {
            return genericMove(x, y, 0, bot_id);
        }
    }

    public class Miner {

        public int move(int x, int y, int bot_id) throws GameActionException {
            return genericMove(x,y,5,bot_id);
        }

        public int mine(int bot_id) throws GameActionException {
            // miner bots should recognize data of 0x4D (M)
            int data = 0x4D;
            Message msg = new Message();
            msg.createMessage(0x33,5,1, bot_id,data);
            int block = msg.getMessage();
            blocks[count++] = block;
            if(count >= 7) {
                rc.submitTransaction(blocks, defaultSoupPrice);
                for(int i = 0; i < 7; i++)
                    blocks[i] = 0;
                count = 0;
            }
            return block;
        }

        public int deposit(int bot_id) throws GameActionException {
            // miner bots should recognize data of 0x44 (D)
            int data = 0x44;
            Message msg = new Message();
            msg.createMessage(0x33,5,1, bot_id,data);
            int block = msg.getMessage();
            blocks[count++] = block;
            if(count >= 7) {
                rc.submitTransaction(blocks, defaultSoupPrice);
                for(int i = 0; i < 7; i++)
                    blocks[i] = 0;
                count = 0;
            }
            return block;
        }

        public int buildSchool(int bot_id) throws GameActionException {
            // miner bots should recognize data of 0x4D53 (BS)
            int data = 0x4D53;
            Message msg = new Message();
            msg.createMessage(0x33,5,1, bot_id,data);
            int block = msg.getMessage();
            blocks[count++] = block;
            if(count >= 7) {
                rc.submitTransaction(blocks, defaultSoupPrice);
                for(int i = 0; i < 7; i++)
                    blocks[i] = 0;
                count = 0;
            }
            return block;
        }

        public int buildFulfillmentCenter(int bot_id) throws GameActionException {
            // miner bots should recognize data of 0x4D46 (BF)
            int data = 0x4D46;
            Message msg = new Message();
            msg.createMessage(0x33,5,1, bot_id,data);
            int block = msg.getMessage();
            blocks[count++] = block;
            if(count >= 7) {
                rc.submitTransaction(blocks, defaultSoupPrice);
                for(int i = 0; i < 7; i++)
                    blocks[i] = 0;
                count = 0;
            }
            return block;
        }
    }

    public class Landscaper {
        public int move(int x, int y, int bot_id) throws GameActionException {
            return genericMove(x,y,4,bot_id);
        }
    }

    public int forceSend(int... soupPrice) throws GameActionException { // force send whatever is in our blocks array
        switch(soupPrice[0]) {
            case 0:
                rc.submitTransaction(blocks,defaultSoupPrice);
                break;
            default:
                rc.submitTransaction(blocks,soupPrice[0]);
                break;
        }
        for(int i = 0; i < 7; i++)
            blocks[i] = 0;
        count = 0;
        return 0;
    }

}