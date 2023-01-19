package testPlayerV4;
import battlecode.common.*;

import java.util.ArrayList;


public class Headquarters extends Robot {

    public Headquarters(RobotController r) {
        super(r);
    }

    static boolean isUnderAttack = false;
    static int turnsSinceAttach = 100;

    static int buildCount = 0;

    static ArrayList<MapLocation> minesLocsAdamantium = new ArrayList<>();
    static ArrayList<MapLocation> minesLocsMana = new ArrayList<>();

    enum Strategy {
        INITIAL,
        BUILD_CARRIERS,
        BUILD_LAUNCHERS,
        CARRIERS_AND_LAUNCHERS,
        ANCHORS,


    }

    static Strategy strategy = Strategy.INITIAL;

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        if(rc.getRoundNum() == 1){
            Comms.reportHQLocation(); //write location to shared array;

        }
        if(rc.getRoundNum() <= 5){
            scanForNearbyWells(); //find nearby wells and broadcasts their locations
            updateWellLocsFromSharedArray(); //read from shared array
        }

        checkIfUnderAttack();
        tryChangeStrategy();
        enactStrategy();
    }

    public void tryChangeStrategy() throws GameActionException {
        if (rc.getRoundNum() <= 1) {
            strategy = Strategy.INITIAL;
        } else if (rc.getRoundNum() < 20) {
            strategy = Strategy.BUILD_LAUNCHERS;
        }else if(rc.getRoundNum() < 1000){
            strategy = Strategy.CARRIERS_AND_LAUNCHERS;
        }else{
            strategy = Strategy.ANCHORS;
        }

        if(isUnderAttack || turnsSinceAttach < 10){
            strategy = Strategy.BUILD_LAUNCHERS;
        }
    }

    public void scanForNearbyWells() throws GameActionException {
        //Adamantium
        WellInfo[] adamantiumsWells = rc.senseNearbyWells(ResourceType.ADAMANTIUM); //find adamatium wells
        if (adamantiumsWells.length > 0) {  //if there are any
            boolean didBroadcastMsg = false;
            for (WellInfo wellToAdd : adamantiumsWells) {  //for each well
                if (!minesLocsAdamantium.contains(wellToAdd.getMapLocation())) { //if we don't already know about it
                    minesLocsAdamantium.add(wellToAdd.getMapLocation()); //add it to the list
                    if(!didBroadcastMsg && rc.getRoundNum() == robotNumber){ //Only one HQ should broadcast at a time
                        Comms.broadcastMineLocation(wellToAdd); //broadcast it
                        didBroadcastMsg = true;
                    }

                }
            }
        }
        //Mana
        WellInfo[] mamaWells = rc.senseNearbyWells(ResourceType.MANA); //find mana wells
        if (mamaWells.length > 0) {  //if there are any
            boolean didBroadcastMsg = false;
            for (WellInfo wellToAdd : mamaWells) {  //for each well
                if (!minesLocsMana.contains(wellToAdd.getMapLocation())) { //if we don't already know about it
                    minesLocsMana.add(wellToAdd.getMapLocation()); //add it to the list
                    if(!didBroadcastMsg && rc.getRoundNum() == robotNumber){ //Only one HQ should broadcast at a time
                        Comms.broadcastMineLocation(wellToAdd); //broadcast it
                        didBroadcastMsg = true;
                    }
                }
            }
        }
    }



    public void updateWellLocsFromSharedArray () throws GameActionException{
        //Adamantium
        MapLocation wellToAdd = Comms.readMineLocationFromSharedArray(ResourceType.ADAMANTIUM);
        if (!minesLocsAdamantium.contains(wellToAdd)) { //if we don't already know about it
            minesLocsAdamantium.add(wellToAdd);
        }


         //Mana
         wellToAdd = Comms.readMineLocationFromSharedArray(ResourceType.MANA);
            if (!minesLocsMana.contains(wellToAdd)) { //if we don't already know about it
                minesLocsMana.add(wellToAdd);
            }
    }

    public void enactStrategy() throws GameActionException {
          switch (strategy) {
              case INITIAL:
                  buildCarrier(ResourceType.ADAMANTIUM);
                  buildCarrier(ResourceType.MANA);
                  buildCarrier(ResourceType.ADAMANTIUM);
                  buildCarrier(ResourceType.MANA);
                  tryBuild(RobotType.LAUNCHER);
                  tryBuild(RobotType.LAUNCHER);
                  tryBuild(RobotType.LAUNCHER);
                case BUILD_CARRIERS:
                    boolean buildAdamantium = rc.getResourceAmount(ResourceType.ADAMANTIUM) < rc.getResourceAmount(ResourceType.MANA);
                    if(buildAdamantium){
                        buildCarrier(ResourceType.ADAMANTIUM);
                        Debug.setString("Building Admantium Carriers");
                    }else{
                        buildCarrier(ResourceType.MANA);
                        Debug.setString("Building Mana carriers");
                    }

                    break;
              case BUILD_LAUNCHERS:
                  tryBuild(RobotType.LAUNCHER);
                  Debug.setString("Building Launchers");
                  break;
                case CARRIERS_AND_LAUNCHERS:
                    int buildInt = rng.nextInt(100);
                    Debug.setString("Building carriers and launchers");
                    if(buildInt > 50){
                        tryBuild(RobotType.CARRIER);
                    }else{
                        tryBuild(RobotType.LAUNCHER);
                    }
                    break;
                case ANCHORS:
                    Debug.setString("Building anchors");
                    tryBuildAnchor();
                    break;
            }
    }

    public boolean tryBuild(RobotType type) throws GameActionException {
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) < type.buildCostAdamantium || rc.getResourceAmount(ResourceType.MANA) < type.buildCostMana || rc.getResourceAmount(ResourceType.ELIXIR) < type.buildCostElixir)  return false;
        MapLocation currentLocation = rc.getLocation();
        for(int i = 0; i < directions.length; i++) {
            Direction dir = directions[i];
            if (rc.canBuildRobot(type, currentLocation.add(dir))) {
                rc.buildRobot(type, currentLocation.add(dir));
                buildCount++;
                return true;
            }
        }
        return false;
    }

    public boolean buildCarrier(ResourceType resource) throws GameActionException {
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) < RobotType.CARRIER.buildCostAdamantium || rc.getResourceAmount(ResourceType.MANA) < RobotType.CARRIER.buildCostMana)  return false;
        ArrayList<MapLocation> minesLocs = new ArrayList<>();

        switch (resource){
            case ADAMANTIUM:
                minesLocs = minesLocsAdamantium;
                break;
            case MANA:
                minesLocs = minesLocsMana;
                break;
        }

        if(minesLocs.size() > 0){
            //Find optimal location to build carrier near mine
            MapLocation optimalBuildLocation = getLocNearestToDestInActionRadius(RobotType.CARRIER, minesLocs.get(0));
            if (optimalBuildLocation != null &&rc.canBuildRobot(RobotType.CARRIER, optimalBuildLocation)) {
                rc.buildRobot(RobotType.CARRIER, optimalBuildLocation);
                return true;
            }else{
                buildRobotNearHQ(RobotType.CARRIER);
            }

        }else{
            //build carrier near HQ
            buildRobotNearHQ(RobotType.CARRIER);
        }



        return false;
    }

    private void checkIfUnderAttack() throws GameActionException {
        isUnderAttack = nearbyEnemies.length > 0;
        if(isUnderAttack){
            turnsSinceAttach = 0;
        }else{
            turnsSinceAttach ++;
        }
    }

    private boolean buildRobotNearHQ(RobotType type) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        for(int i = 0; i < directions.length; i++) {
            Direction dir = directions[i];
            if (rc.canBuildRobot(type, currentLocation.add(dir))) {
                rc.buildRobot(type, currentLocation.add(dir));
                buildCount++;
                return true;
            }
        }
        return false;
    }

    private MapLocation getLocNearestToDestInActionRadius(RobotType type, MapLocation destination) throws GameActionException{
        //finds the nearest location within action radius of the destination
        if(destination == null) return null;
        MapLocation bestLoc = null;
        MapLocation locToCheck;
        int closesdDist = Integer.MAX_VALUE;
        MapLocation[] possibleLocs = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);

        for(int i=possibleLocs.length; --i>=0;){
            locToCheck = possibleLocs[i];
            if(rc.canBuildRobot(type, locToCheck) && destination.distanceSquaredTo(locToCheck) < closesdDist){
                bestLoc = locToCheck;
                closesdDist = destination.distanceSquaredTo(locToCheck);
            }
        }
        return bestLoc;
    }



    public boolean tryBuildAnchor() throws GameActionException {
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                rc.buildAnchor(Anchor.STANDARD);
                return true;
            }
        return false;
    }


}
