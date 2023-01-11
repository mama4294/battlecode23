package testPlayerV1;
import battlecode.common.*;


public class Headquarters extends Robot {

    public Headquarters(RobotController r) {
        super(r);
    }

    enum Strategy {
        BUILD,
        ATTACK
    }

    static Strategy strategy = Strategy.BUILD;

    public void takeTurn() throws GameActionException {

        tryChangeStrategy();
        enactStrategy();

    }

    public void tryChangeStrategy() throws GameActionException {
        if (rc.getRoundNum() < 100) {
            strategy = Strategy.BUILD;
        }else{
            strategy = Strategy.ATTACK;
        }
    }

    public void enactStrategy() throws GameActionException {
          switch (strategy) {
                case BUILD:
                    tryBuild(RobotType.CARRIER);
                    break;
                case ATTACK:
                    if(rng.nextBoolean()) {
                        tryBuild(RobotType.LAUNCHER);
                    }else{
                        tryBuild(RobotType.CARRIER);
                    }
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

    public boolean tryBuildAnchor() throws GameActionException {
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) < Anchor.STANDARD.adamantiumCost || rc.getResourceAmount(ResourceType.MANA) > Anchor.STANDARD.manaCost) return false;
        MapLocation currentLocation = rc.getLocation();
        for(int i = 0; i < directions.length; i++) {
            Direction dir = directions[i];
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                rc.buildAnchor(Anchor.STANDARD);
                return true;
            }
        }
        return false;
    }


}
