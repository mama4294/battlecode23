package currentPlayer;
import battlecode.common.*;

import java.util.HashSet;
import java.util.Set;

public class Carrier extends Robot {

    int heldWeight = 0;
    boolean isFull = false;

    int optimalCarryWeight = 39;

    ResourceType myResourceType = null;


    MapLocation targetWell;

    Set<MapLocation> neutralIslandLocations = new HashSet<>();

    State state = State.EXPLORE;


    enum State {

        MINE,
        EXPLORE,
        RETURN,
        ANCHORDELIVER,
    }

    public Carrier(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        neutralIslandLocations = Comms.getTeamIslandLocations(Team.NEUTRAL);
        findNearbyNeutralIslands();
        heldWeight = getHeldWeight();
        isFull = heldWeight >= optimalCarryWeight;
        getMinerType();   //Sets myResourceType (Adamantium, Mana, Elixir)
        tryAttack();
        findWell();       //Sets targetWell if null
        tryChangeState(); //Ex: change from an Explorer to a Miner if there is a well nearby
        runStateAction(); //Ex: if you are a Miner, mine
    }

    public void runStateAction() throws GameActionException{
        MapLocation enemyIslandLoc = senseNearbyForEnemyIslands();
        switch (state){
            case MINE:
                if(enemyIslandLoc != null){
                    tryMoveTwice(enemyIslandLoc);//GoTo enemy island location
                    Debug.setString(myResourceType + " Miner: going to enemy island at : " + enemyIslandLoc);
                    break;
                }

                if(targetWell != null) {
                    Debug.setString(myResourceType + " Miner: Moving to well at: " + targetWell);
                    collectFromWell(targetWell);
                    boolean isAdjactentToWell = rc.getLocation().isAdjacentTo(targetWell);
                    if(isAdjactentToWell){
                        //stay put
                    }else tryMoveTwice(targetWell);

                }else{
                    Nav.moveRandomly();
                    Debug.setString(myResourceType + " Miner: Moving randomly");
                }
                break;
            case EXPLORE:
                if(enemyIslandLoc != null){
                    tryMoveTwice(enemyIslandLoc);//GoTo enemy island location
                    Debug.setString(myResourceType + " Miner: going to enemy island at : " + enemyIslandLoc);
                    break;
                }
                explore();
                Debug.setString(myResourceType + " EXPlORER: exploring to " + explorationTarget);
                break;
            case RETURN:
                Debug.setString(myResourceType + " RETURNER: Delivering paylod to " + homeHQ);
                tryTransferToHomeHQ();
                boolean isAdjecentToHomeHQ = rc.getLocation().isAdjacentTo(homeHQ);
                if(!isAdjecentToHomeHQ){
                    tryMoveTwice(homeHQ);
                }
                break;
            case ANCHORDELIVER:
                Debug.setString("ANCHOR: taking anchor to island");
                if(rc.canTakeAnchor(homeHQ, Anchor.STANDARD)){
                    rc.takeAnchor(homeHQ, Anchor.STANDARD);
                }
                bringAnchorToIsland();
                break;
        }
    }

    public void findNearbyNeutralIslands() throws GameActionException{
        int[] nearbyIslandIds = rc.senseNearbyIslands();
        for(int id : nearbyIslandIds){
            Team islandTeam = rc.senseTeamOccupyingIsland(id);
            if(islandTeam == Team.NEUTRAL && rc.readSharedArray(id + Comms.INDEX_ISLANDS_START -1) == 0){ //shared array does not contain this island
               MapLocation[] islandLocations = rc.senseNearbyIslandLocations(id);
                neutralIslandLocations.add(islandLocations[0]);
            }
        }
    }

    public void tryMoveTwice(MapLocation target) throws GameActionException{
        Nav.goTo(target);
        Nav.goTo(target);
    }

    public void tryAttack() throws GameActionException{
        for(int i = nearbyEnemies.length; --i>=0;){
            RobotInfo enemy = nearbyEnemies[i];
            if(enemy.type == RobotType.LAUNCHER && rc.getAnchor() == null){ //don't throw anchors
                if(rc.canAttack(enemy.location)){
                    rc.attack(enemy.location);
                    state = state.EXPLORE; //reset state because we lost the payload
                }
            }
        }
    }

    public void findWell() throws GameActionException {
        if(targetWell == null && myResourceType != null){
            targetWell = getWellLoc(myResourceType);

            if(targetWell != null){
                //Set the optimal carry weight based on the distance from the HQ
                int squaresTargetFromHQ = (int) Math.ceil(Math.sqrt(homeHQ.distanceSquaredTo(targetWell)));
                optimalCarryWeight = getOptimalResourceCount(squaresTargetFromHQ, false);
            }
        }
    }

    public void getMinerType() throws GameActionException {
        if(myResourceType == null){
            if(rc.getRoundNum() % 2 ==0 ){
                myResourceType = ResourceType.ADAMANTIUM;
            }else{
                myResourceType = ResourceType.MANA;
            }
        }
    }




    public void tryChangeState() throws GameActionException{

        switch (state){
            case MINE:
                if(isFull){
                    state = State.RETURN;
                }
                break;
            case EXPLORE:
                if(targetWell != null){
                    state = State.MINE;
                }
        }

        if(!isFull && rc.canTakeAnchor(homeHQ, Anchor.STANDARD)) state = State.ANCHORDELIVER;

    }

    public boolean collectFromWell(MapLocation wellLocation) throws GameActionException{
        if (rc.canCollectResource(wellLocation, -1)) {

                rc.collectResource(wellLocation, -1);
                Debug.setString("Collecting, now have, ADMAN:" +
                        rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                        " MANA: " + rc.getResourceAmount(ResourceType.MANA) +
                        " ELIXER: " + rc.getResourceAmount(ResourceType.ELIXIR)+
                        " out of: " + optimalCarryWeight);
            return true;

        }
        return false;
    }

    public boolean tryTransferToHomeHQ() throws GameActionException{
        if(homeHQ == null) return false;
        boolean transfered = false;
        Debug.setIndicatorDotGreen(rc.getLocation());

        int carriedAdmantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);
        int carriedMana = rc.getResourceAmount(ResourceType.MANA);
        int carriedElixer = rc.getResourceAmount(ResourceType.ELIXIR);

        if(carriedAdmantium > 0 && rc.canTransferResource(homeHQ, ResourceType.ADAMANTIUM, carriedAdmantium)){
            Debug.setIndicatorDotYellow(homeHQ);
            rc.transferResource(homeHQ, ResourceType.ADAMANTIUM, carriedAdmantium);
            transfered = true;
        }
        if(carriedMana > 0 && rc.canTransferResource(homeHQ, ResourceType.MANA, carriedMana)){
            Debug.setIndicatorDotBlue(homeHQ);
            rc.transferResource(homeHQ, ResourceType.MANA, carriedMana);
            transfered = true;
        }
        if(carriedElixer > 0 && rc.canTransferResource(homeHQ, ResourceType.ELIXIR, carriedElixer)){
            Debug.setIndicatorDotGreen(homeHQ);
            rc.transferResource(homeHQ, ResourceType.ELIXIR, carriedElixer);
            transfered = true;
        }

        if(transfered && getHeldWeight() < 40) state = State.EXPLORE;
        return transfered;
    }

    public MapLocation getWellLoc(ResourceType resource) throws GameActionException{
        if(resource == null) return null;
        WellInfo[] wells = rc.senseNearbyWells(resource);
        if (wells.length > 0){
            return Utils.getClosestWell(rc.getLocation(), wells); //finds closest well
        }
        return null;
    }


    public int getHeldWeight() throws GameActionException {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR);
    }

    public void bringAnchorToIsland() throws GameActionException{
        if (rc.getAnchor() != null) {
            // If I have an anchor singularly focus on getting it to the nearest neutral island
            if (neutralIslandLocations.size() > 0) {
                MapLocation nearestNeutralIsland = Utils.nearestLocation(rc.getLocation(), neutralIslandLocations);
                Debug.setString("ANCHOR: Moving my anchor towards " + nearestNeutralIsland);
                if (rc.canPlaceAnchor() && rc.getLocation().equals(nearestNeutralIsland)) {
                    Debug.setString("Huzzah, placed anchor!");
                    rc.placeAnchor();
                    state = State.EXPLORE;
                }
                tryMoveTwice(nearestNeutralIsland);
            }else{
                explore();
                Debug.setString("ANCHOR: exploring to " + explorationTarget);
            }
        }
    }


    public static int getOptimalResourceCount(int distance, boolean isUpgradedWell){
        int resourceGatheringRate = isUpgradedWell ? GameConstants.WELL_ACCELERATED_RATE : GameConstants.WELL_STANDARD_RATE;
        double bestRate = 0;
        int bestAmount = 1;
        for (int m = 1; m < GameConstants.CARRIER_CAPACITY; m++){
            int numTurns = 0;
            //number of turns to get from HQ to well
            numTurns += numTurns(distance, getCarrierMovementCooldown(0), 1);
            //number of turns to gather m amount of resources
            numTurns += numTurns(m, 8, resourceGatheringRate);
            //number of turns to get from well to HQ
            numTurns += numTurns(distance, getCarrierMovementCooldown(m), 1);
            //rate = total number of resources/distance
            double curRate = ((double) m)/numTurns;
            if (curRate > bestRate){
                bestRate = curRate;
                bestAmount = m;
            }
        }
        return bestAmount;
    }
    public static int getCarrierMovementCooldown(int amount){
        return (int) (GameConstants.CARRIER_MOVEMENT_INTERCEPT+ GameConstants.CARRIER_MOVEMENT_SLOPE*amount);
    }
    public static int numTurns(int distance, int cooldown, int increment){
        int numTurns = 1;
        int curDistance = 0;
        int curCD = 0;
        while (curDistance != distance){
            if (curCD < 10){
                curDistance += increment;
                curCD += cooldown;
            }
            else{
                numTurns++;
                curCD -= 10;
            }
        }
        return numTurns;
    }

}

