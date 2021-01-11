package MyRobot;
import battlecode.common.*;

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

    static final double passabilityThreshold = 0.7;

    static Direction bugDirection = null;

    static double percentage = (Math.random()*(31)+5) / 100.0;

    static int currentVotes = 0;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

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
        double random = Math.random();
        RobotType toBuild = null;
        int influence = 0;

        if (rc.getRoundNum() < 200)
        {
            if (random < 0.6)
            {
                toBuild = RobotType.POLITICIAN;
                influence = (int) (0.2 * (rc.getInfluence()));
            }

            else if (random < 0.9)
            {
                toBuild = RobotType.SLANDERER;
                influence = (int) (0.2 * (rc.getInfluence()));

            }

            else
            {
                toBuild = RobotType.MUCKRAKER;
                influence = 1;
            }
        }

        else
        {
            if (random < 0.6)
            {
                toBuild = RobotType.POLITICIAN;
                influence = (int) (0.1 * (rc.getInfluence()));
            }

            else if (random < 0.9)
            {
                toBuild = RobotType.SLANDERER;
                influence = (int) (0.1 * (rc.getInfluence()));
            }

            else
            {
                toBuild = RobotType.MUCKRAKER;
                influence = 1;
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
                System.out.println("Built robot");
                break;
            }
        }

        if (rc.getRoundNum() > 200)
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

        if (rc.canBid((int) Math.ceil(rc.getInfluence()*percentage)))
        {
            rc.bid((int) Math.ceil(rc.getInfluence()*percentage));
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
        int index = 0;

        for (Direction dir : directions)
        {
            if (rc.canMove(dir))
            {
                possibleDirections[index] = dir;
                index++;
            }
        }

        if (possibleDirections[0] != null)
        {
            Direction highestPassability = possibleDirections[0];

            for (Direction dir : possibleDirections)
            {
                if (dir != null)
                {
                    if (rc.sensePassability(rc.getLocation().add(dir)) > rc.sensePassability(rc.getLocation().add(highestPassability)))
                    {
                        highestPassability = dir;
                    }
                }

                else
                {
                    break;
                }
            }

            if (rc.canMove(highestPassability))
            {
                rc.move(highestPassability);
            }
        }
    }

    static void runMuckraker() throws GameActionException 
    {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        
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

    /**
     * Sets the robot's flag to its location.
     *
     * @throws GameActionException
     */
    static void sendLocation() throws GameActionException
    {
        MapLocation location = rc.getLocation();
        int x = location.x, y = location.y;
        int encodedLocation = (x % 128) * 128 + (y % 128);
        if (rc.canSetFlag(encodedLocation))
        {
            rc.setFlag(encodedLocation);
        }
    }

    /**
     * Sets the robot's flag to its location and any extra information.
     *
     * @param extraInformation Extra information to encode in the flag
     * @throws GameActionException
     */
    static void sendLocation(int extraInformation) throws GameActionException
    {
        MapLocation location = rc.getLocation();
        int x = location.x, y = location.y;
        int encodedLocation = extraInformation * 128 * 128 + (x%128) * 128 + (y % 128);
        if (rc.canSetFlag(encodedLocation))
        {
            rc.setFlag(encodedLocation);
        }
    }

    /**
     * Returns the location encoded in the flag.
     *
     * @param flag The flag to decode
     * @return the location encoded in the flag
     */
    static MapLocation getLocationFromFlag(int flag)
    {
        int y = flag % 128;
        int x = (flag / 128) % 128;
        int extraInformation = flag / 128 / 128;

        MapLocation currentLocation = rc.getLocation();
        int offsetX128 = currentLocation.x / 128;
        int offsetY128 = currentLocation.y / 128;
        MapLocation actualLocation = new MapLocation(offsetX128 * 128 + x, offsetY128 * 128 + y);

        MapLocation alternative = actualLocation.translate(-128, 0);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation))
        {
            actualLocation = alternative;
        }

        alternative = actualLocation.translate(128, 0);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation))
        {
            actualLocation = alternative;
        }

        alternative = actualLocation.translate(0, -128);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation))
        {
            actualLocation = alternative;
        }

        alternative = actualLocation.translate(0, 128);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation))
        {
            actualLocation = alternative;
        }

        return actualLocation;
    }

    /**
     * Returns the location encoded in the flag.
     *
     * @param target The target location that the bug is aiming for.
     * @throws GameActionException
     */
    static void basicBug(MapLocation target) throws GameActionException
    {
        Direction d = rc.getLocation().directionTo(target);
        
        if (rc.getLocation().equals(target))
        {
            // do something else, now that you're there
        }

        else if (rc.isReady())
        {
            if (rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold)
            {
                rc.move(d);
                bugDirection = null;
            }

            else
            {
                if (bugDirection == null)
                {
                    bugDirection = d.rotateRight();
                }

                for (int i = 0; i < 8; i++)
                {
                    if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold)
                    {
                        rc.move(bugDirection);
                        break;
                    }
                    bugDirection = bugDirection.rotateRight();
                }
                bugDirection = bugDirection.rotateLeft();
            }
        }
    }
}
