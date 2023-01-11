package currentPlayer;
import battlecode.common.*;



public class Headquarters extends Robot {

    public Headquarters(RobotController r) {
        super(r);
    }

    enum Strategy {
        BUILD_CARRIERS,
        GENTLE_ATTACK,
        ANCHORS,
    }

    static Strategy strategy = Strategy.BUILD_CARRIERS;

    public void takeTurn() throws GameActionException {

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
    }

    public void enactStrategy() throws GameActionException {
          switch (strategy) {
                case BUILD_CARRIERS:
                    tryBuild(RobotType.CARRIER);
                    Debug.setString("Building Carriers");
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

    public boolean tryBuildAnchor() throws GameActionException {
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                rc.buildAnchor(Anchor.STANDARD);
                return true;
            }
        return false;
    }


}
