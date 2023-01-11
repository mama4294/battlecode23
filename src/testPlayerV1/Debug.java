package testPlayerV1;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Debug {
        static RobotController rc;

        public static void init(RobotController r) {
            rc = r;
        }
        static final boolean showDebug = false;
        static final boolean showYellow = false;

     public static void consoleLog (String s) {
         if (showDebug) {
             System.out.println(s);
         }
     }

    public static void setString (String message) throws GameActionException{
        if(showDebug)  rc.setIndicatorString(message);
    }

    public static void setIndicatorLineYellow (MapLocation target) throws GameActionException{
        if(showDebug && showYellow) rc.setIndicatorLine(rc.getLocation(),target, 255,255,0);
    }

    public static void setIndicatorDotYellow (MapLocation target) throws GameActionException{
        if(showDebug) rc.setIndicatorDot(target, 255,255,0);
    }

    public static void setIndicatorDotBlue (MapLocation target) throws GameActionException{
        if(showDebug) rc.setIndicatorDot(target, 0,255,255);
    }

    public static void setIndicatorDotGreen (MapLocation target) throws GameActionException{
        if(showDebug) rc.setIndicatorDot(target, 0,128,0);
    }



}
