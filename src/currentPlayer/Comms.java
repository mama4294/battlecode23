package currentPlayer;

import battlecode.common.*;

import java.util.HashSet;
import java.util.Set;

public class Comms {

        //For working with the shared array for communications
        //The array is 64 non-negative integers strictly less than 216

        //Read: 2 bytecode
        //Write: 75 bytcode


        static RobotController rc;
        static final int INDEX_ROUND_NUM = 0;
        static final int INDEX_HQ_LOC_1 = 1;
        static final int INDEX_HQ_LOC_2 = 2;
        static final int INDEX_HQ_LOC_3 = 3;
        static final int INDEX_HQ_LOC_4 = 4;

        static final int IDX_NUM_HQ_EVEN= 5;
        static final int IDX_NUM_CARRIERS_EVEN = 6;
        static final int IDX_NUM_LAUNCHERS_EVEN = 7;
        static final int IDX_NUM_BOOSTERS_EVEN = 8;
        static final int IDX_NUM_DESTABILIZERS_EVEN = 9;
        static final int IDX_NUM_AMPLIFIERS_EVEN = 10;


        static final int IDX_NUM_HQ_ODD = 11;
        static final int IDX_NUM_CARRIERS_ODD = 12;
        static final int IDX_NUM_LAUNCHERS_ODD = 13;
        static final int IDX_NUM_BOOSTERS_ODD = 14;
        static final int IDX_NUM_DESTABILIZERS_ODD = 15;
        static final int IDX_NUM_AMPLIFIERS_ODD = 16;


        static final int INDEX_ISLANDS_START = 17; //Next 34 ints are for island locations

        static  final int INDEX_MINE_LOC_ADAMANTIUM = 51;
        static  final int INDEX_MINE_LOC_MANA = 52;

        enum MessageCode {
                ADAMANTIUM_MINE_LOC,
                MANA_MINER_LOC,
                ELIXIR_MINER_LOC,
        }

        static final int MESSAGE_OFFSET = 1;
        static final int MAP_OFFSET = 1;

        static final int NUM_TYPES_OF_ROBOTS = 6;

        static final int TOTAL_BITS = 16;
        static final int LOCATION_BITS = 12;
        static final int ISLAND_TEAM_BITS= 1;
        static final int ISLAND_HEALTH_BITS = 3;

        static final int ISLAND_HEALTH_SIZE = (int) Math.ceil(Anchor.ACCELERATING.totalHealth/8); //scale max anchor heath to 8 bits


        public static void init(RobotController r) {
            rc = r;
        }

        public static int locationToInt(MapLocation loc){
                // 0 is a placeholder for unused
                if(loc == null) return  0;

                //All values have +1 offset
                return MAP_OFFSET + loc.x + loc.y * rc.getMapWidth();
        }

        public static MapLocation intToLocation(int m){
                if(m == 0) return null;
                m = m-MAP_OFFSET; //Remove the offset
                return new MapLocation(m % rc.getMapWidth()-MAP_OFFSET, m/rc.getMapWidth());
        }

        public static int encodeIslandData(MapLocation islandLoc, int islandHeath, Team islandTeam){
                int scaledHealth = (int) Math.ceil((float)islandHeath/ ISLAND_HEALTH_SIZE); //0-8 scale
                int encodedMsg = locationToInt(islandLoc);
                encodedMsg = encodedMsg << (TOTAL_BITS- LOCATION_BITS);
                encodedMsg += islandTeam.ordinal() << ISLAND_HEALTH_BITS;
                encodedMsg += scaledHealth;
                return encodedMsg;
        }

        public static Team readTeamHoldingIsland(int encodedMsg) throws GameActionException{
                int health = encodedMsg & 0b111;
                int team = (encodedMsg >> ISLAND_HEALTH_BITS) & 0b1;
                if(health > 0) return Team.values()[team];
                return Team.NEUTRAL;
        }

        public static MapLocation readIslandLocation(int encodedMsg) throws GameActionException{
                int locationInt = encodedMsg >> (TOTAL_BITS- LOCATION_BITS);
                return intToLocation(locationInt);
        }

        public static void broadcastMineLocation(WellInfo well) throws GameActionException{
                int encodedMsg = locationToInt(well.getMapLocation());
               ResourceType type = well.getResourceType();
                switch (type){
                        case ADAMANTIUM:
                                if(rc.canWriteSharedArray(INDEX_MINE_LOC_ADAMANTIUM, encodedMsg)){
                                        rc.writeSharedArray(INDEX_MINE_LOC_ADAMANTIUM, encodedMsg);
                                }
                                break;
                        case MANA:
                                if(rc.canWriteSharedArray(INDEX_MINE_LOC_MANA, encodedMsg)){
                                        rc.writeSharedArray(INDEX_MINE_LOC_MANA, encodedMsg);
                                }
                                break;
                }
        }

        public static MapLocation readMineLocationFromSharedArray (ResourceType type) throws GameActionException{
                switch (type){
                        case ADAMANTIUM:
                                return intToLocation(rc.readSharedArray(INDEX_MINE_LOC_ADAMANTIUM));
                        case MANA:
                                return intToLocation(rc.readSharedArray(INDEX_MINE_LOC_MANA));
                }
                return null;
        }


        public static void reportHQLocation() throws GameActionException{
                for(int i= 0; i < 4; i++){
                      if(rc.readSharedArray(INDEX_HQ_LOC_1 + i) == 0){
                            rc.writeSharedArray(INDEX_HQ_LOC_1 + i, locationToInt(rc.getLocation()));
                            return;
                      }
                }
        }

        public static MapLocation[] getHQLocations() throws GameActionException{
                MapLocation[] hqs = new MapLocation[4];
                for(int i= 0; i < 4; i++){
                        hqs[i] = intToLocation(rc.readSharedArray(INDEX_HQ_LOC_1 + i));
                }
                return hqs;
        }



        public static int reportAlive() throws GameActionException{
                int robotNumber = 0;

                //Determine what the actual round number mod is and what the shared array thinks.
                int roundNum = rc.getRoundNum();
                int sharedArrRoundNum = rc.readSharedArray(INDEX_ROUND_NUM);
                boolean roundIsEven = roundNum % 2 == 0;

                //if the round number on the shared array is incorrect, you are the first to report. Reset the rest of the array.
                if(roundNum != sharedArrRoundNum){
                        for (int i= NUM_TYPES_OF_ROBOTS; --i >= 0;){
                                if(roundIsEven) { //even rounds
                                        if(rc.canWriteSharedArray(i+IDX_NUM_HQ_EVEN, 0)) rc.writeSharedArray(i+IDX_NUM_HQ_EVEN, 0); //reset to zero
                                }else{ //odd rounds
                                        if(rc.canWriteSharedArray(i+IDX_NUM_HQ_ODD, 0)) rc.writeSharedArray(i+IDX_NUM_HQ_ODD, 0); //reset to zero
                                }
                        }
                        rc.writeSharedArray(INDEX_ROUND_NUM, roundNum); //correct round number
                }

                int typeCountIndex = 0;
                RobotType type = rc.getType();
                switch(type){
                        case HEADQUARTERS:
                                if(roundIsEven) typeCountIndex= IDX_NUM_HQ_EVEN;
                                else typeCountIndex= IDX_NUM_HQ_ODD;
                                break;
                        case CARRIER:
                                if(roundIsEven) typeCountIndex= IDX_NUM_CARRIERS_EVEN;
                                else typeCountIndex= IDX_NUM_CARRIERS_ODD;
                                break;
                        case LAUNCHER:
                                if(roundIsEven) typeCountIndex= IDX_NUM_LAUNCHERS_EVEN;
                                else typeCountIndex= IDX_NUM_LAUNCHERS_ODD;
                                break;
                        case BOOSTER:
                                if(roundIsEven) typeCountIndex= IDX_NUM_BOOSTERS_EVEN;
                                else typeCountIndex= IDX_NUM_BOOSTERS_ODD;
                                break;
                        case DESTABILIZER:
                                if(roundIsEven) typeCountIndex= IDX_NUM_DESTABILIZERS_EVEN;
                                else typeCountIndex= IDX_NUM_DESTABILIZERS_ODD;
                                break;
                        case AMPLIFIER:
                                if(roundIsEven) typeCountIndex= IDX_NUM_AMPLIFIERS_EVEN;
                                else typeCountIndex= IDX_NUM_AMPLIFIERS_ODD;
                                break;
                }

                if(typeCountIndex != 0 ){
                        int currentCount = rc.readSharedArray(typeCountIndex);
                        robotNumber = currentCount+1;
                        if(rc.canWriteSharedArray(typeCountIndex, robotNumber)) rc.writeSharedArray(typeCountIndex, robotNumber);
                }

                return robotNumber;
        }




        public static void updateIslandInfo(int islandIndex) throws GameActionException{
                int arrayMsg = rc.readSharedArray(islandIndex+ INDEX_ISLANDS_START -1);
                Team arrayIslandTeam = readTeamHoldingIsland(arrayMsg);
                Team islandTeam = rc.senseTeamOccupyingIsland(islandIndex);
                if(arrayMsg == 0 || arrayIslandTeam != islandTeam){
                        MapLocation[] nearbyIslandTiles =  rc.senseNearbyIslandLocations(islandIndex);
                        int islandHealth = rc.senseAnchorPlantedHealth(islandIndex);
                        int encodedMsg = encodeIslandData(nearbyIslandTiles[0], islandHealth, islandTeam);
                        if(rc.canWriteSharedArray(islandIndex + INDEX_ISLANDS_START -1, encodedMsg)) rc.writeSharedArray(islandIndex + INDEX_ISLANDS_START -1, encodedMsg);
                }
        }

        public static Set<MapLocation> getIslandLocations() throws GameActionException{
                MapLocation island = null;
                Set<MapLocation> islandLocs = new HashSet<>();
                for(int i= 0; i < 34; i++){
                        island = intToLocation(rc.readSharedArray(INDEX_ISLANDS_START + i -1));
                        if(island != null) islandLocs.add(island);
                }
                return islandLocs;
        }

        public static Set<MapLocation> getNeutralIslandLocations() throws GameActionException{
                MapLocation islandLoc = null;
                Set<MapLocation> islandLocs = new HashSet<>();
                for(int i= 0; i < 34; i++){
                        int encodedMsg = rc.readSharedArray(INDEX_ISLANDS_START + i -1);
                        if(encodedMsg ==0) continue;
                        Team islandTeam = readTeamHoldingIsland(encodedMsg);
                        if(islandTeam == Team.NEUTRAL){
                                islandLoc = readIslandLocation(encodedMsg);
                                if(islandLoc != null) islandLocs.add(islandLoc);
                        }
                }
                return islandLocs;
        }






}
