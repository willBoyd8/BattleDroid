package droideka.utility;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class ForTheGloryOfTheEmpire {
    public static int red = 188;
    public static int green = 19;
    public static int blue = 254;

    public static void print(RobotController rc){
        int x = (rc.getMapWidth()-22)/2;
        int y = (rc.getMapHeight()-27)/2;

        // F in For
        rc.setIndicatorLine(new MapLocation(x + 2,y + 22), new MapLocation(x + 2, y + 26), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 2,y + 24), new MapLocation(x + 3, y + 24), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 2,y + 26), new MapLocation(x + 4, y + 26), red, green, blue);

        // o in For
        rc.setIndicatorLine(new MapLocation(x + 5,y + 22), new MapLocation(x + 5, y + 24), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 5,y + 22), new MapLocation(x + 7, y + 22), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 5,y + 24), new MapLocation(x + 7, y + 24), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 7,y + 22), new MapLocation(x + 7, y + 24), red, green, blue);

        // R in For
        rc.setIndicatorLine(new MapLocation(x + 8,y + 22), new MapLocation(x + 8, y + 24), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 8,y + 24), new MapLocation(x + 9, y + 24), red, green, blue);


        // T in the first The
        rc.setIndicatorLine(new MapLocation(x + 13,y + 22), new MapLocation(x + 13, y + 26), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 11,y + 26), new MapLocation(x + 15, y + 26), red, green, blue);

        // H in the first The
        rc.setIndicatorLine(new MapLocation(x + 16,y + 22), new MapLocation(x + 16, y + 26), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 16,y + 24), new MapLocation(x + 18, y + 24), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 18,y + 22), new MapLocation(x + 18, y + 24), red, green, blue);

        // E in the first The
        rc.setIndicatorLine(new MapLocation(x + 19,y + 22), new MapLocation(x + 21, y + 22), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 19,y + 22), new MapLocation(x + 19, y + 24), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 19,y + 23), new MapLocation(x + 21, y + 23), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 19,y + 24), new MapLocation(x + 21, y + 24), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 21,y + 23), new MapLocation(x + 21, y + 24), red, green, blue);





        // G in Glory
        rc.setIndicatorLine(new MapLocation(x + 5,y + 16), new MapLocation(x + 5, y + 20), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 5,y + 16), new MapLocation(x + 8, y + 16), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 5,y + 20), new MapLocation(x + 8, y + 20), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 8,y + 16), new MapLocation(x + 8, y + 17), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 7,y + 17), new MapLocation(x + 8, y + 17), red, green, blue);

        // L in Glory
        rc.setIndicatorLine(new MapLocation(x + 9,y + 16), new MapLocation(x + 9, y + 20), red, green, blue);

        // O in Glory
        rc.setIndicatorLine(new MapLocation(x + 10,y + 16), new MapLocation(x + 10, y + 18), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 10,y + 16), new MapLocation(x + 12, y + 16), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 10,y + 18), new MapLocation(x + 12, y + 18), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 12,y + 16), new MapLocation(x + 12, y + 18), red, green, blue);

        // R in Glory
        rc.setIndicatorLine(new MapLocation(x + 13,y + 16), new MapLocation(x + 13, y + 18), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 13,y + 18), new MapLocation(x + 14, y + 18), red, green, blue);

        // Y in Glory
        rc.setIndicatorLine(new MapLocation(x + 15,y + 14), new MapLocation(x + 17, y + 18), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 15,y + 18), new MapLocation(x + 16, y + 16), red, green, blue);




        // O in Of
        rc.setIndicatorLine(new MapLocation(x + 3,y + 10), new MapLocation(x + 3, y + 12), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 3,y + 10), new MapLocation(x + 5, y + 10), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 3,y + 12), new MapLocation(x + 5, y + 12), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 5,y + 10), new MapLocation(x + 5, y + 12), red, green, blue);

        // F in of
        rc.setIndicatorLine(new MapLocation(x + 7,y + 10), new MapLocation(x + 7, y + 14), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 6,y + 12), new MapLocation(x + 8, y + 12), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 7,y + 14), new MapLocation(x + 8, y + 14), red, green, blue);




        // T in the second The
        rc.setIndicatorLine(new MapLocation(x + 10,y + 14), new MapLocation(x + 14, y + 14), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 12,y + 10), new MapLocation(x + 12, y + 14), red, green, blue);

        // H in the second The
        rc.setIndicatorLine(new MapLocation(x + 15,y + 10), new MapLocation(x + 15, y + 14), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 15,y + 12), new MapLocation(x + 17, y + 12), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 17,y + 10), new MapLocation(x + 17, y + 12), red, green, blue);

        // E in the second The
        rc.setIndicatorLine(new MapLocation(x + 18,y + 10), new MapLocation(x + 20, y + 10), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 18,y + 10), new MapLocation(x + 18, y + 12), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 18,y + 11), new MapLocation(x + 20, y + 11), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 18,y + 12), new MapLocation(x + 20, y + 12), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 20,y + 11), new MapLocation(x + 20, y + 12), red, green, blue);




        // E in Empire
        rc.setIndicatorLine(new MapLocation(x + 3,y + 4), new MapLocation(x + 3, y + 8), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 3,y + 8), new MapLocation(x + 5, y + 8), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 3,y + 6), new MapLocation(x + 4, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 3,y + 4), new MapLocation(x + 5, y + 4), red, green, blue);

        // M in Empire
        rc.setIndicatorLine(new MapLocation(x + 6,y + 4), new MapLocation(x + 6, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 8,y + 4), new MapLocation(x + 8, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 10,y + 4), new MapLocation(x + 10, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 6,y + 6), new MapLocation(x + 10, y + 6), red, green, blue);

        // P in Empire
        rc.setIndicatorLine(new MapLocation(x + 11,y + 2), new MapLocation(x + 11, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 11,y + 6), new MapLocation(x + 13, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 13,y + 4), new MapLocation(x + 13, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 11,y + 4), new MapLocation(x + 13, y + 4), red, green, blue);

        // I in Empire
        rc.setIndicatorLine(new MapLocation(x + 14,y + 4), new MapLocation(x + 14, y + 6), red, green, blue);
        rc.setIndicatorDot(new MapLocation(x + 14, y + 7), red, green, blue);

        // R in Empire
        rc.setIndicatorLine(new MapLocation(x + 15,y + 4), new MapLocation(x + 15, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 15,y + 6), new MapLocation(x + 16, y + 6), red, green, blue);

        // E in Empire
        rc.setIndicatorLine(new MapLocation(x + 17,y + 4), new MapLocation(x + 19, y + 4), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 17,y + 4), new MapLocation(x + 17, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 17,y + 6), new MapLocation(x + 19, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 19,y + 5), new MapLocation(x + 19, y + 6), red, green, blue);
        rc.setIndicatorLine(new MapLocation(x + 17,y + 5), new MapLocation(x + 19, y + 5), red, green, blue);




    }
}
