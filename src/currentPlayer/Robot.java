package currentPlayer;
import battlecode.common.Direction;
import battlecode.common.*;
import java.util.Random;

public class Robot {

    RobotController rc;
    static Random rng;
    int turnCount = 0;

    MapLocation homeHQ = null;

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public Robot(RobotController r) {
        this.rc = r;
        rng = new Random(rc.getRoundNum()*23981 + rc.getID()*10289);
        Debug.init(rc);
        Nav.init(rc);
        Comms.init(rc);
    }

    public void takeTurn() throws GameActionException {
        if(turnCount==0){
            turnCount=rc.getRoundNum();
        }
        turnCount += 1;
        findHomeHQ();
    }

    public void findHomeHQ() throws GameActionException {
        if (homeHQ == null) {
            //set home HQ
            int radius = rc.getType().visionRadiusSquared;
            RobotInfo[] allies = rc.senseNearbyRobots(radius, rc.getTeam());
            if (allies.length >= 0) {
                for(int i = 0; i< allies.length; i++){
                    if(allies[i].type == RobotType.HEADQUARTERS){
                        homeHQ = allies[i].location;
                        break;
                    }
                }
            }
        }
    }

}
