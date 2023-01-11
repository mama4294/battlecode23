package currentPlayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static currentPlayer.Robot.directions;
import static currentPlayer.Robot.rng;

public class Comms {

        //For working with the shared array for communications
        //The array is 64 non-negative integers strictly less than 216

        //Read: 2 bytecode
        //Write: 75 bytcode


        static RobotController rc;
        static final int mapOffset = 1;
        static final int INDEX_TURN_COUNT = 0;
        static final int INDEX_HQ_LOC_1 = 1;
        static final int INDEX_HQ_LOC_2 = 2;
        static final int INDEX_HQ_LOC_3 = 3;
        static final int INDEX_HQ_LOC_4 = 4;


        public static void init(RobotController r) {
            rc = r;
        }




}
