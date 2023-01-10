package currentPlayer;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Debug {
        static RobotController rc;

        public static void init(RobotController r) {
            rc = r;
        }
        static final boolean showDebug = true;
        static final boolean showYellow = true;

    public static void setString (String message) throws GameActionException{
        if(showDebug)  rc.setIndicatorString(message);
    }

    public static void setIndicatorLineYellow (MapLocation target) throws GameActionException{
        if(showDebug && showYellow) rc.setIndicatorLine(rc.getLocation(),target, 255,255,0);
    }



}
