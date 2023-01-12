package currentPlayer;
import battlecode.common.*;

import java.util.ArrayList;


public class Headquarters extends Robot {

    public Headquarters(RobotController r) {
        super(r);
    }

    static boolean isUnderAttack = false;
    static int turnsSinceAttach = 100;

    static ArrayList<MapLocation> minesLocsAdamantium = new ArrayList<>();
    static ArrayList<MapLocation> minesLocsMana = new ArrayList<>();

    enum Strategy {
        BUILD_CARRIERS,

        BUILD_LAUNCHERS,
        GENTLE_ATTACK,
        ANCHORS,


    }

    static Strategy strategy = Strategy.BUILD_CARRIERS;

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        scanForNearbyWells();
        checkIfUnderAttack();
        tryChangeStrategy();
        enactStrategy();

    }

    public void tryChangeStrategy() throws GameActionException {
        if (rc.getRoundNum() < 100) {
            strategy = Strategy.BUILD_CARRIERS;
        }else if(rc.getRoundNum() < 350){
            strategy = Strategy.GENTLE_ATTACK;
        }else{
            strategy = Strategy.ANCHORS;
        }

        if(isUnderAttack || turnsSinceAttach < 10){
            strategy = Strategy.BUILD_LAUNCHERS;
        }
    }

    public void scanForNearbyWells() throws GameActionException {
        WellInfo[] adamantiumsWells = rc.senseNearbyWells(ResourceType.ADAMANTIUM); //find adamatium wells
        if (adamantiumsWells.length > 0) {  //if there are any
            for (WellInfo wellToAdd : adamantiumsWells) {  //for each well
                if (!minesLocsAdamantium.contains(wellToAdd.getMapLocation())) { //if we don't already know about it
                    minesLocsAdamantium.add(wellToAdd.getMapLocation()); //add it to the list
                }
            }
        }

        WellInfo[] mamaWells = rc.senseNearbyWells(ResourceType.MANA); //find adamatium wells
        if (mamaWells.length > 0) {  //if there are any
            for (WellInfo wellToAdd : mamaWells) {  //for each well
                if (!minesLocsMana.contains(wellToAdd.getMapLocation())) { //if we don't already know about it
                    minesLocsMana.add(wellToAdd.getMapLocation()); //add it to the list
                }
            }
        }
    }

    public void enactStrategy() throws GameActionException {
          switch (strategy) {
                case BUILD_CARRIERS:
                    boolean buildAdamantium = rng.nextBoolean();
                    if(buildAdamantium){
                        buildCarrier(ResourceType.ADAMANTIUM);
                    }else{
                        buildCarrier(ResourceType.MANA);
                    }
                    Debug.setString("Building Carriers");
                    break;
              case BUILD_LAUNCHERS:
                  tryBuild(RobotType.LAUNCHER);
                  Debug.setString("UNDER ATTACK: Building Launchers");
                  break;
                case GENTLE_ATTACK:
                    int buildInt = rng.nextInt(100);
                    Debug.setString("Building carriers and launchers");
                    if(buildInt > 50) {
                        tryBuildAnchor();
                    }else if(buildInt > 25){
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
                return true;
            }
        }
        return false;
    }

    public boolean buildCarrier(ResourceType resource) throws GameActionException {
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) < RobotType.CARRIER.buildCostAdamantium || rc.getResourceAmount(ResourceType.MANA) < RobotType.CARRIER.buildCostMana)  return false;
        MapLocation currentLocation = rc.getLocation();
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
            MapLocation optimalBuildLocation = getLocNearestToDestInActionRadius(minesLocs.get(0));
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
        isUnderAttack = nearbyEnemies.length > nearbyAllies.length;
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
                return true;
            }
        }
        return false;
    }

    private MapLocation getLocNearestToDestInActionRadius( MapLocation destination) throws GameActionException{
        //finds the nearest location within action radius of the destination
        MapLocation bestLoc = null;
        MapLocation locToCheck = null;
        int closesdDist = Integer.MAX_VALUE;
        MapLocation[] possibleLocs = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);

        for(int i=possibleLocs.length; --i>=0;){
            locToCheck = possibleLocs[i];
            if(rc.canSenseLocation(locToCheck) && !rc.isLocationOccupied(locToCheck) && destination.distanceSquaredTo(locToCheck) < closesdDist){
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
