package learningBot;

import battlecode.common.*;
import learningBot.Constants.SymmetryType;
import learningBot.Constants.TileType;
import learningBot.util.FastIterableLocSet;

public class MapRecorder extends RobotPlayer {



  static int[][] tileType = new int[Constants.MAX_MAP_SIZE][];
  static boolean initialized = false;
  static int initializedRow = 0;
  static final int INIT_BYTECODE_LEFT = 1000;

  public static int validSymmetries = Constants.SymmetryType.ALL;
  static boolean symetryValidated = false;



  static void initializeMapRecorder() {
    if (initialized)
      return;

    while (initializedRow < Constants.MAX_MAP_SIZE) {
      if (Clock.getBytecodesLeft() < INIT_BYTECODE_LEFT)
        return;
      tileType[initializedRow] = new int[Constants.MAX_MAP_SIZE];
      initializedRow++;
    }
    initialized = true;
  }

  public static void recordSeenTile(MapLocation loc)
    throws GameActionException {
    if (rc.senseCloud(loc)) {
      tileType[loc.x][loc.y] = TileType.CLOUD;
    } else if (!rc.sensePassability(loc)) {
      tileType[loc.x][loc.y] = TileType.WALL;
    } else if (rc.senseWell(loc) != null) {
      tileType[loc.x][loc.y] = TileType.WELL;
    } else {
      RobotInfo robot = rc.senseRobotAtLocation(loc);
      if (robot != null && robot.type == RobotType.HEADQUARTERS) {
        tileType[loc.x][loc.y] =
          robot.team == myTeam ? TileType.FRIENDLY_HQ : TileType.ENEMY_HQ;
      } else {
        tileType[loc.x][loc.y] = TileType.EMPTY;
      }
    }
  }

  public static void recordSurroundings(int leaveBytecodeCnt) throws GameActionException {
    if (!initialized) return;
    MapInfo[] surroundingTiles = rc.senseNearbyMapInfos();
    for (int i = surroundingTiles.length; --i >= 0;) {
      if (Clock.getBytecodesLeft() <= leaveBytecodeCnt) {
        return; //Leave early if going to run out of bytecode
      }
      MapLocation tile = surroundingTiles[i].getMapLocation();

        //Record tile if new
     if(tileType[tile.x][tile.y] == 0){
       recordSeenTile(tile);
     }

     if(!symetryValidated){
       invalidateSymmetries(tile);
     }

//     if(symetryValidated){
//       System.out.println("Symetry Validated" + getValidSymmetries());
//     }
    }
//    System.out.println("Map Records--------------------------");
//    System.out.println("ByteCode Remaining: " + Clock.getBytecodesLeft());
//    System.out.println(tileType);
  }

  static int getValidSymmetries() throws GameActionException {
    //TODO add comms
    // return Comms.readSymmetryAll() & validSymmetries;
    return validSymmetries;
  }


  public static MapLocation[] getValidSymmetryLocs(MapLocation hqLoc, int symmetry) throws GameActionException {
    MapLocation verticalFlip = new MapLocation(
            mapHeight - hqLoc.x - 1,
            hqLoc.y
    );
    MapLocation horizontalFlip = new MapLocation(
            hqLoc.x,
            mapHeight - hqLoc.y - 1

    );
    MapLocation rotation = new MapLocation(
      mapHeight - hqLoc.x - 1,
      mapHeight - hqLoc.y - 1
    );
    switch (symmetry) {
      case SymmetryType.VERTICAL |
        SymmetryType.HORIZONTAL |
        SymmetryType.ROTATIONAL:
        return new MapLocation[] { verticalFlip, horizontalFlip, rotation };
      case SymmetryType.VERTICAL | SymmetryType.HORIZONTAL:
        return new MapLocation[] { verticalFlip, horizontalFlip };
      case SymmetryType.VERTICAL | SymmetryType.ROTATIONAL:
        return new MapLocation[] { verticalFlip, rotation };
      case SymmetryType.HORIZONTAL | SymmetryType.ROTATIONAL:
        return new MapLocation[] { horizontalFlip, rotation };
      case SymmetryType.VERTICAL:
        return new MapLocation[] { verticalFlip };
      case SymmetryType.HORIZONTAL:
        return new MapLocation[] { horizontalFlip };
      case SymmetryType.ROTATIONAL:
        return new MapLocation[] { rotation };
      default:
        // This shouldn't happen
        return new MapLocation[] { verticalFlip, horizontalFlip, rotation };
    }
  }

  public static void invalidateSymmetries(MapLocation loc)
    throws GameActionException {
    if(symetryValidated) return;
    validSymmetries = getValidSymmetries();

    // Don't invalidate if there is only one left.
    switch (validSymmetries) {
      case Constants.SymmetryType.HORIZONTAL:
      case Constants.SymmetryType.VERTICAL:
      case Constants.SymmetryType.ROTATIONAL:
        symetryValidated = true;
        System.out.println("Symetry Validated: " + validSymmetries + ", Turn: " + rc.getRoundNum() +", ID: " + rc.getID());
        return;
      default:
        break;
    }

    int otherTileType;

    //Try to invalidate Horizontal symmetry
    if ((validSymmetries & Constants.SymmetryType.HORIZONTAL) != 0) {
      otherTileType = tileType[loc.x][mapHeight - loc.y - 1];

      switch (otherTileType) {
        case TileType.UNKNOWN:
          break;
        case TileType.WALL:
        case TileType.CLOUD:
        case TileType.WELL:
        case TileType.EMPTY:
          if (otherTileType != tileType[loc.x][loc.y]) {
            validSymmetries &= ~Constants.SymmetryType.HORIZONTAL;
          }

          break;
        case TileType.FRIENDLY_HQ:
          if (tileType[loc.x][loc.y] != TileType.ENEMY_HQ) {
            validSymmetries &= ~Constants.SymmetryType.HORIZONTAL;
          }
          break;
        case TileType.ENEMY_HQ:
          if (tileType[loc.x][loc.y] != TileType.FRIENDLY_HQ) {
            validSymmetries &= ~Constants.SymmetryType.HORIZONTAL;
          }
          break;
        default:
          // Debug.println("Invalid tile type: " + otherTileType, id);
          break;
      }
    }
    //Try to invalidate Vertical symmetry
    if ((validSymmetries & Constants.SymmetryType.VERTICAL) != 0) {
      otherTileType = tileType[mapWidth - loc.x - 1][loc.y];
      switch (otherTileType) {
        case TileType.UNKNOWN:
          break;
        case TileType.WALL:
        case TileType.CLOUD:
        case TileType.WELL:
        case TileType.EMPTY:
          if (otherTileType != tileType[loc.x][loc.y]) {
            validSymmetries &= ~Constants.SymmetryType.VERTICAL;
          }
          break;
        case TileType.FRIENDLY_HQ:
          if (tileType[loc.x][loc.y] != TileType.ENEMY_HQ) {
            validSymmetries &= ~Constants.SymmetryType.VERTICAL;
          }
          break;
        case TileType.ENEMY_HQ:
          if (tileType[loc.x][loc.y] != TileType.FRIENDLY_HQ) {
            validSymmetries &= ~Constants.SymmetryType.VERTICAL;
          }
          break;
        default:
          break;
      }
    }

    //Try to invalidate Rotational symmetry
    if ((validSymmetries & Constants.SymmetryType.ROTATIONAL) != 0) {
      otherTileType = tileType[mapWidth - loc.x - 1][mapHeight - loc.y - 1];
      switch (otherTileType) {
        case TileType.UNKNOWN:
          break;
        case TileType.WALL:
        case TileType.CLOUD:
        case TileType.WELL:
        case TileType.EMPTY:
          if (otherTileType != tileType[loc.x][loc.y]) {
            validSymmetries &= ~Constants.SymmetryType.ROTATIONAL;
          }
          break;
        case TileType.FRIENDLY_HQ:
          if (tileType[loc.x][loc.y] != TileType.ENEMY_HQ) {
            validSymmetries &= ~Constants.SymmetryType.ROTATIONAL;
          }
          break;
        case TileType.ENEMY_HQ:
          if (tileType[loc.x][loc.y] != TileType.FRIENDLY_HQ) {
            validSymmetries &= ~Constants.SymmetryType.ROTATIONAL;
          }
          break;
        default:
          break;
      }
    }
  }



  private static final int HQ_SPAWNABLE_DX[] = {
    -3,
    0,
    0,
    3,
    -2,
    -2,
    2,
    2,
    -2,
    -2,
    -1,
    -1,
    1,
    1,
    2,
    2,
    -2,
    0,
    0,
    2,
    -1,
    -1,
    1,
    1,
    -1,
    0,
    0,
    1,
  };
  private static final int HQ_SPAWNABLE_DY[] = {
    0,
    -3,
    3,
    0,
    -2,
    2,
    -2,
    2,
    -1,
    1,
    -2,
    2,
    -2,
    2,
    -1,
    1,
    0,
    -2,
    2,
    0,
    -1,
    1,
    -1,
    1,
    0,
    -1,
    1,
    0,
  };

  // this func called at the start of each HQ to get us out of jail,
  // and avoid spawning on tiles with current (that messes up formation)
//  public static void hqInit() throws GameActionException {
//    // use scripts/pos_gen.py
//    MapInfo[] infos = rc.senseNearbyMapInfos();
////    for (int i = infos.length; --i >= 0;) {
////      if (infos[i].isPassable()) {
////        vals[infos[i].getMapLocation().x *
////          mapHeight +
////          infos[i].getMapLocation().y] =
////          (char) (PASSIABLE_BIT | infos[i].getCurrentDirection().ordinal());
////        Headquarter.sensablePassibleArea++;
////      }
////    }
//    FastIterableLocSet spawnableSet = new FastIterableLocSet(29);
//    int hqX = rc.getLocation().x;
//    int hqY = rc.getLocation().y;
//    spawnableSet.add(hqX, hqY);
//    // this is not a BFS and will miss maze-like tiles but it's fast and good enough
//    for (int i = HQ_SPAWNABLE_DX.length; --i >= 0;) {
//      int x = hqX + HQ_SPAWNABLE_DX[i];
//      int y = hqY + HQ_SPAWNABLE_DY[i];
//      if (
//        x < 0 ||
//        x >= mapWidth ||
//        y < 0 ||
//        y >= mapHeight ||
//        (vals[x * mapHeight + y] & PASSIABLE_BIT) == 0 ||
//        (vals[x * mapHeight + y] & CURRENT_MASK) != 8
//      ) continue;
//      if (spawnableSet.contains(x + 1, y)) {
//        spawnableSet.add(x, y);
//        continue;
//      }
//      if (spawnableSet.contains(x + 1, y + 1)) {
//        spawnableSet.add(x, y);
//        continue;
//      }
//      if (spawnableSet.contains(x + 1, y - 1)) {
//        spawnableSet.add(x, y);
//        continue;
//      }
//      if (spawnableSet.contains(x, y + 1)) {
//        spawnableSet.add(x, y);
//        continue;
//      }
//      if (spawnableSet.contains(x, y - 1)) {
//        spawnableSet.add(x, y);
//        continue;
//      }
//      if (spawnableSet.contains(x - 1, y)) {
//        spawnableSet.add(x, y);
//        continue;
//      }
//      if (spawnableSet.contains(x - 1, y + 1)) {
//        spawnableSet.add(x, y);
//        continue;
//      }
//      if (spawnableSet.contains(x - 1, y - 1)) {
//        spawnableSet.add(x, y);
//      }
//    }
//    // if there are enemy HQ, don't spawn in its range
//    for (RobotInfo robot : rc.senseNearbyRobots(-1, oppTeam)) {
//      if (robot.type == RobotType.HEADQUARTERS) {
//        int enemyX = robot.location.x;
//        int enemyY = robot.location.y;
//        for (int i = HQ_SPAWNABLE_DX.length; --i >= 0;) {
//          int x = enemyX + HQ_SPAWNABLE_DX[i];
//          int y = enemyY + HQ_SPAWNABLE_DY[i];
//          spawnableSet.remove(x, y);
//        }
//      }
//    }
//    if (spawnableSet.size < 3) {
//      System.out.println("weird map, allowing all spawns");
//      for (int i = HQ_SPAWNABLE_DX.length; --i >= 0;) {
//        int x = hqX + HQ_SPAWNABLE_DX[i];
//        int y = hqY + HQ_SPAWNABLE_DY[i];
//        if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) {
//          spawnableSet.add(x, y);
//        }
//      }
//    }
//    spawnableSet.remove(hqX, hqY);
//    spawnableSet.updateIterable();
//    // Headquarter.spawnableSet = spawnableSet;
//  }

//  public static final int WITHIN_HQ_RANGE = SEEN_BIT + 7;

  // this magic number will be treat it as a wall in pathfinder,
  // but will be considered by Launcher blind attack

  //    remnents of defensive launcher strategy
  //    public static MapLocation findBestLoc(MapLocation wellLoc) {
  //        // returns a defensive location around a mine
  //        // all enemy HQ apply an attractive vector
  //        // force proportional to 1/dis
  //        double fx = 0, fy = 0;
  //        double disSq;
  //        for (int i = Comm.numHQ; --i >= 0;) {
  //            disSq = wellLoc.distanceSquaredTo(Comm.enemyHQLocations[i]);
  //            fx += (Comm.enemyHQLocations[i].x - wellLoc.x) / disSq * 10;
  //            fy += (Comm.enemyHQLocations[i].y - wellLoc.y) / disSq * 10;
  //        }
  //        double dis = Math.sqrt(fx * fx + fy * fy);
  //        if (dis <= 1e-6) {
  //            fx = 1;
  //            fy = 0;
  //        } else {
  //            fx = fx / dis;
  //            fy = fy / dis;
  //        }
  //        int x = (int)(wellLoc.x + fx * 6);
  //        int y = (int)(wellLoc.y + fy * 6);
  //        for (int[] dir : Unit.BFS25) {
  //            int tx = x + dir[0];
  //            int ty = y + dir[1];
  //            if ((vals[tx * mapWidth + mapHeight] & SEEN_BIT) == 0
  //                    || ((vals[tx * mapWidth + mapHeight] & PASSIABLE_BIT) != 0 && (vals[tx * mapWidth + mapHeight] & CURRENT_MASK) == 8)) {
  //                x = tx;
  //                y = ty;
  //                break;
  //            }
  //        }
  //        return new MapLocation(x, y);
  //    }
}
