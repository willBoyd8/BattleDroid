package commando.communication;

/*
                                                    BATTLECODE COMMUNICATION

 32  31  30  29  28  27  26  25  24  23  22  21  20  19  18  17  16  15  14  13  12  11  10   9   8   7   6   5   4   3   2   1
| 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |

|   MSG_TYPE    | BOT_TYPE  |   |         BOT_ID            |                     LOCATION / DATA                           |   |
                              ^                                                                                               ^
                              |                                                                                               |
                              |                                                                                               |
                        COMMAND_TYPE                                                                                          |
                      REQUEST / COMMAND                                                                                   PARITY BIT
 */

// meet tomorrow night
// basic structure
//    location


import battlecode.common.MapLocation;

public class Message {

    private int block;
    //private CommandType cmd;
    private int msg_type;
    private int bot_type;
    private int command;
    private int bot_id;
    private int data;

    public Message() {
        block = 0;
        msg_type = 0;
        bot_type = 0;
        command = 0;
        bot_id = 0;
        data = 0;
    }

    public int loadBlock(int blk) {
        msg_type = 0;
        bot_type = 0;
        command = 0;
        bot_id = 0;
        data = 0;
        if(!checkParity(blk)) {
            return -1;
        }
        block = blk;
        msg_type = extractMessageType();
        bot_type = extractBotType();
        command = extractCommandType();
        bot_id = extractBotID();
        data = extractData();
        return 0;
    }

    private int extractMessageType() {
        return extractBits(block,32,29);
    }

    private int extractBotType() {
        return extractBits(block,28,26);
    }

    private int extractCommandType() {
        return extractBitAt(block,25);
    }

    private int extractBotID() {
        return extractBits(block,24,18);
    }

    private int extractData() {
        return extractBits(block,17,2);
    }

    private int countOneSetBits(int number) {
        int count = 0;
        for(int i = 0; i < 32; i++) {
            if((number & 1) == 1)
                count++;
            number = number >>> 1;
        }
        return count;
    }

    private int extractBits(int v, int begin, int end) {
        return ((v)>>(end)) % (1 << ((begin)-(end)+1));
    }

    private int extractBitAt(int v, int pos) {
        return (v >> pos) % 2;
    }

    private boolean checkParity(int blk) {
        int ones = countOneSetBits(blk);
        // make sure there are an even number of ones
        return ((ones % 2) == 0);
    }

    public int createMessage(int msg_type, int bot_type, int command_type, int bot_id, int data) {
        block = 0; // zero out block
        if (this.msg_type >= 16) {
            System.err.println("msg_type too long, failing");
            return -1;
        }
        block = (this.msg_type << 29);
        if(this.bot_type >= 8) {
            System.err.println("bot_type too long, failing");
            return -1;
        }
        block = block | (this.bot_type << 26);
        if((command_type != 0) && (command_type != 1)) {
            System.err.println("command_type is not 0 or 1, failing");
            return -1;
        }
        block = block | (command_type << 25);
        if(this.bot_id >= 128) {
            System.err.println("bot_id too long, failing");
            return -1;
        }
        block = block | (this.bot_id << 18);
        if(this.data > 0xFFFF) {
            System.err.println("data too long, failing");
            return -1;
        }
        block = block | (this.data << 2);
        int numOfOnes = countOneSetBits(block);
        if((numOfOnes % 2) != 0)
            block = block | 1 << 0;
        // set vars outside of method
        msg_type = this.msg_type;
        bot_type = this.bot_type;
        command = command_type;
        bot_id = this.bot_id;
        data = this.data;
        return 0;
    }

    public int getMessage() {
        return block;
    }
    public int getBotType() { return bot_type;}
    public int getMessageType() { return msg_type;}
    public int getCommandType() { return command;}
    public int getBotID() { return bot_id;}
    public int getData() { return data;}

    public MapLocation getMinerLocation() {
        int x = extractBits(data,16,8);
        int y = extractBits(data,7,0);
        MapLocation loc = new MapLocation(x,y);
        return loc;
    }

    public void printMessage() {
        System.out.println("msg_type: " + msg_type);
        System.out.println("bot_type: " + bot_type);
        System.out.println("cmd_type: " + command);
        System.out.println("bot_id:   " + bot_id);
        System.out.println("data:     " + data);
    }

}