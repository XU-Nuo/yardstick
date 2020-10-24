package nl.tudelft.opencraft.yardstick.model;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import java.util.Random;
import nl.tudelft.opencraft.yardstick.bot.Bot;
import nl.tudelft.opencraft.yardstick.bot.ai.task.TaskExecutor;
import nl.tudelft.opencraft.yardstick.bot.ai.task.WalkTaskExecutor;
import nl.tudelft.opencraft.yardstick.bot.world.Block;
import nl.tudelft.opencraft.yardstick.bot.world.BlockFace;
import nl.tudelft.opencraft.yardstick.bot.world.ChunkNotLoadedException;
import nl.tudelft.opencraft.yardstick.util.Vector3d;
import nl.tudelft.opencraft.yardstick.util.Vector3i;
import nl.tudelft.opencraft.yardstick.util.ZigZagRange;

/**
 * Represents a model which moves the bot randomly to short and long distance
 * locations.
 */
public class SimpleMovementModel implements BotModel {

    private static final Random RANDOM = new Random(System.nanoTime());

    private final boolean anchored;
    private Vector3d anchor;
    private final int boxDiameter;

    public SimpleMovementModel() {
        anchored = false;
        boxDiameter = 32;
    }

    public SimpleMovementModel(int boxDiameter) {
        this.anchored = false;
        this.boxDiameter = boxDiameter;
    }

    public SimpleMovementModel(int boxDiameter, boolean spawnAnchor) {
        this.anchored = spawnAnchor;
        this.boxDiameter = boxDiameter;
    }

    @Override
    public TaskExecutor newTask(Bot bot) {
        return new WalkTaskExecutor(bot, newTargetLocation(bot));
    }

    public Vector3i newTargetLocation(Bot bot) {
        if (RANDOM.nextDouble() < 0.1) {
            return getNewLongDistanceTarget(bot);
        } else {
            return getNewFieldLocation(bot);
        }
    }

    /**
     * Function to make bot walk in a specific area.
     *
     * @return New random location in a field that has the original location at
     * its center.
     */
    Vector3i getNewFieldLocation(Bot bot) {
        Vector3d originalLocation = getStartLocation(bot);
        int maxx = ((int) originalLocation.getX()) + boxDiameter / 2;
        int minx = ((int) originalLocation.getX()) - boxDiameter / 2;
        int maxz = ((int) originalLocation.getZ()) + boxDiameter / 2;
        int minz = ((int) originalLocation.getZ()) - boxDiameter / 2;

        int newX = (int) (Math.floor(RANDOM.nextInt(maxx - minx) + minx) + 0.5);
        int newZ = (int) (Math.floor(RANDOM.nextInt(maxz - minz) + minz) + 0.5);

        return getTargetAt(bot, newX, newZ);
    }

    private Vector3d getStartLocation(Bot bot) {
        if (anchored) {
            if (anchor == null) {
                Position pos = bot.getWorld().getSpawnPoint();
                anchor = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
            }
            return anchor;
        }
        return bot.getPlayer().getLocation();
    }

    private Vector3i getNewLongDistanceTarget(Bot bot) {
        // TODO make param for this value.
        int maxDist = 64 * 5;
        int minDist = 64 * 1;
        int distance = RANDOM.nextInt(maxDist - minDist) + minDist;
        int angle = RANDOM.nextInt(360);

        Vector3d location = getStartLocation(bot);
        int newX = (int) (Math.floor(location.getX() + (distance * Math.cos(angle))) + 0.5);
        int newZ = (int) (Math.floor(location.getZ() + (distance * Math.sin(angle))) + 0.5);

        return getTargetAt(bot, newX, newZ);
    }

    // TODO make sure this also uses the getStartingLoc Function
    // TODO remove bot from param list
    private Vector3i getTargetAt(Bot bot, int x, int z) {
        Vector3d botLoc = bot.getPlayer().getLocation();

        int y = -1;
        try {
            for (ZigZagRange it = new ZigZagRange(0, 255, (int) botLoc.getY()); it.hasNext(); ) {
                y = it.next();
                Block test = bot.getWorld().getBlockAt(x, y, z);
                if (test.getMaterial().isTraversable()
                        && !test.getRelative(BlockFace.BOTTOM).getMaterial().isTraversable()) {
                    break;
                }
            }

            if (y < 0 || y > 255) {
                return botLoc.intVector();
            }

            return new Vector3i(x, y, z);
        } catch (ChunkNotLoadedException ex) {
            bot.getLogger().warning("Bot target not loaded: (" + x + "," + y + "," + z + ")");
            return botLoc.intVector();
        }
    }

}
