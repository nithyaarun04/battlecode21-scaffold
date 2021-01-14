package MyRobot;
import battlecode.common.*;
import java.util.ArrayList;
import java.util.Arrays;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

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

    static int turnCount;

    static final double passabilityBound = 0.7;

    static Direction bugDirection = null;

    static double percentage = (Math.random()*(31)+5) / 100.0;

    static int currentVotes = 0;

    static ArrayList<Integer> muckrakersCreatedIDs = new ArrayList<Integer>();

    static int parentID;

    static int test = 0;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException 
    {
        if (!rc.getType().equals(RobotType.ENLIGHTENMENT_CENTER))
        {            
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) 
            {
                if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) 
                {
                    parentID = robot.ID;
                    break;
                }
            }
        }

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException 
    {
        for (int ID : muckrakersCreatedIDs)
        {
            if (rc.canGetFlag(ID) && rc.getFlag(ID) != 0)
            {
                if (rc.canSetFlag(rc.getFlag(ID)))
                {
                    rc.setFlag(rc.getFlag(ID));
                    break;
                }
            }
        }

        double random = Math.random();
        RobotType toBuild = null;
        int influence = 0;

        if (rc.getRoundNum() < 150 
            || (rc.getRoundNum() > 500 && rc.getRoundNum() < 600) 
            || (rc.getRoundNum() > 1000 && rc.getRoundNum() < 1100))
        {
            toBuild = RobotType.SLANDERER;
            if (rc.getRoundNum() < 25)
            {
                influence = (int) (0.2 * rc.getInfluence());
            }

            else if (rc.getRoundNum() < 50)
            {
                influence = (int) (0.25 * rc.getInfluence());
            }

            else if (rc.getRoundNum() < 75)
            {
                influence = (int) (0.3 * rc.getInfluence());
            }

            else if (rc.getRoundNum() < 100)
            {
                influence = (int) (0.35 * rc.getInfluence());
            }

            else if (rc.getRoundNum() < 125)
            {
                influence = (int) (0.4 * rc.getInfluence());
            }

            else if (rc.getRoundNum() < 150)
            {
                influence = (int) (0.45 * rc.getInfluence());
            }

            else
            {
                influence = (int) (0.2 * rc.getInfluence());
            }
        }

        else
        {
            if (random < 0.1)
            {
                toBuild = RobotType.POLITICIAN;
                influence = (int) (0.2 * rc.getInfluence());
            }

            else if (random < 0.4)
            {
                toBuild = RobotType.SLANDERER;
                influence = (int) (0.2 * rc.getInfluence());
            }

            else
            {
                toBuild = RobotType.MUCKRAKER;
                influence = 10;
            }
        }

        if (influence > 400)
        {
            influence = 400;
        }

        for (Direction dir : directions)
        {
            if (rc.canBuildRobot(toBuild, dir, influence))
            {
                rc.buildRobot(toBuild, dir, influence);
                if (toBuild.equals(RobotType.MUCKRAKER))
                {
                    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2,rc.getTeam());
                    for (RobotInfo robot : nearbyRobots)
                    {
                        if (robot.location.equals(rc.getLocation().add(dir)))
                        {
                            muckrakersCreatedIDs.add(robot.ID);
                            break;
                        }
                    }
                }
                break;
            }
        }

        int maxBid = 50;
        boolean lastBidMax = false;

        if (rc.getRoundNum() >= 200)
        {
            // Lost or tied the previous round
            if (currentVotes == rc.getTeamVotes())
            {
                if (percentage+0.025 < 0.8)
                {
                    percentage += 0.025;
                }

                else
                {
                    percentage = 0.8;
                }
            }

            if (currentVotes == rc.getTeamVotes() && lastBidMax)
            {
                maxBid += 5;
                lastBidMax = false;
            }

            // Won the previous round
            else
            {
                if (percentage-0.005 > 0.002)
                {
                    percentage -= 0.005;
                }

                else
                {
                    percentage = 0.002;
                }
            }

            currentVotes = rc.getTeamVotes();

            if (rc.canBid((int) Math.ceil(rc.getInfluence()*percentage)) && (int) Math.ceil(rc.getInfluence()*percentage) < maxBid)
            {
                rc.bid((int) Math.ceil(rc.getInfluence()*percentage));
            }

            else if (rc.canBid(maxBid))
            {
                rc.bid(maxBid);
                lastBidMax = true;
            }
        }
    }

    static void runPolitician() throws GameActionException 
    {
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(actionRadius);
        
        for (RobotInfo a : nearbyRobots)
        {
            if (a.getTeam() == Team.NEUTRAL)
            {
                if (rc.canEmpower(actionRadius))
                {
                    rc.empower(actionRadius);
                }
            }
        }

        tryMove(randomDirection());
    }

    static void runSlanderer() throws GameActionException 
    {
        Direction[] possibleDirections = new Direction[8];
        int index1 = 0;

        for (Direction dir : directions)
        {
            if (rc.canMove(dir))
            {
                possibleDirections[index1] = dir;
                index1++;
            }
        }

        Direction[] possiblePassableDirections = new Direction[8];
        int index2 = 0;

        for (Direction dir : possibleDirections)
        {
            if (dir != null)
            {
                if (rc.sensePassability(rc.getLocation().add(dir)) >= passabilityBound)
                {
                    possiblePassableDirections[index2] = dir;
                    index2++;
                }
            }

            else
            {
                break;
            }
        }

        if (possiblePassableDirections[0] != null)
        {
            rc.move(possiblePassableDirections[(int) (Math.random()*index2)]);
        }

        else if (possibleDirections[0] != null)
        {
            rc.move(possibleDirections[(int) (Math.random()*index1)]);
        }
    }

    static void runMuckraker() throws GameActionException 
    {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;

        if (rc.canGetFlag(parentID) && rc.getFlag(parentID) != 0)
        {
            basicBug(getLocationFromFlag(rc.getFlag(parentID)));
        }

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) 
        {
            if (robot.type.canBeExposed()) 
            {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) 
                {
                    rc.expose(robot.location);
                    return;
                }
            }

            if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER))
            {
                sendLocation(robot.location);
            }
        }

        tryMove(randomDirection());
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Pathfinding Lecture Code

    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;

    static void sendLocation(MapLocation location) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = ((x & BITMASK) << NBITS) + (y & BITMASK);
        if (rc.canSetFlag(encodedLocation)) {
            rc.setFlag(encodedLocation);
        }
    }

    @SuppressWarnings("unused")
    static void sendLocation(MapLocation location, int extraInformation) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = (extraInformation << (2*NBITS)) + ((x & BITMASK) << NBITS) + (y & BITMASK);
        if (rc.canSetFlag(encodedLocation)) {
            rc.setFlag(encodedLocation);
        }
    }

    static MapLocation getLocationFromFlag(int flag) {
        int y = flag & BITMASK;
        int x = (flag >> NBITS) & BITMASK;
        // int extraInformation = flag >> (2*NBITS);

        MapLocation currentLocation = rc.getLocation();
        int offsetX128 = currentLocation.x >> NBITS;
        int offsetY128 = currentLocation.y >> NBITS;
        MapLocation actualLocation = new MapLocation((offsetX128 << NBITS) + x, (offsetY128 << NBITS) + y);

        // You can probably code this in a neater way, but it works
        MapLocation alternative = actualLocation.translate(-(1 << NBITS), 0);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(1 << NBITS, 0);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, -(1 << NBITS));
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, 1 << NBITS);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        return actualLocation;
    }


    ////////////////////////////////////////////////////////////////////////////
    // BASIC BUG - just follow the obstacle while it's in the way
    //             not the best bug, but works for "simple" obstacles
    //             for better bugs, think about Bug 2!

    static final double passabilityThreshold = 0.7;

    static void basicBug(MapLocation target) throws GameActionException {
        Direction d = rc.getLocation().directionTo(target);
        if (rc.getLocation().equals(target)) {
            // do something else, now that you're there
            // here we'll just explode
            if (rc.canEmpower(1)) {
                rc.empower(1);
            }
        } else if (rc.isReady()) {
            if (rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold) {
                rc.move(d);
                bugDirection = null;
            } else {
                if (bugDirection == null) {
                    bugDirection = d;
                }
                for (int i = 0; i < 8; ++i) {
                    if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold) {
                        rc.setIndicatorDot(rc.getLocation().add(bugDirection), 0, 255, 255);
                        rc.move(bugDirection);
                        bugDirection = bugDirection.rotateLeft();
                        break;
                    }
                    rc.setIndicatorDot(rc.getLocation().add(bugDirection), 255, 0, 0);
                    bugDirection = bugDirection.rotateRight();
                }
            }
        }
    }
}
