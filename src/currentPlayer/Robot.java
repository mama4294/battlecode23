package currentPlayer;
import battlecode.common.Direction;
import battlecode.common.*;
import java.util.Random;

public class Robot {

    RobotController rc;
    Random rng;
    int turnCount = 0;

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

    }

    public void takeTurn() throws GameActionException {
        if(turnCount==0){
            turnCount=rc.getRoundNum();
        }
        turnCount += 1;
    }

}
