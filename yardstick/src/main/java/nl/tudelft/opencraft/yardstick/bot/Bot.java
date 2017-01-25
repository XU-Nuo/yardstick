package nl.tudelft.opencraft.yardstick.bot;

import nl.tudelft.opencraft.yardstick.bot.ai.pathfinding.EuclideanHeuristic;
import nl.tudelft.opencraft.yardstick.bot.ai.pathfinding.SimpleWorldPhysics;
import nl.tudelft.opencraft.yardstick.bot.ai.pathfinding.astar.SaneAStar;
import nl.tudelft.opencraft.yardstick.bot.ai.task.Task;
import nl.tudelft.opencraft.yardstick.bot.entity.BotPlayer;
import nl.tudelft.opencraft.yardstick.bot.world.World;
import nl.tudelft.opencraft.yardstick.logging.GlobalLogger;
import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.tcp.TcpSessionFactory;

import java.util.logging.Logger;

public class Bot {

    private final Logger logger;
    private final MinecraftProtocol protocol;
    private final String name;
    private final BotTicker ticker;
    //
    private Client client;
    private World world;
    private Server server;
    private BotPlayer player;
    private SaneAStar pathFinder;
    private Task task;

    public Bot(MinecraftProtocol protocol) {
        this.name = protocol.getProfile().getName();
        this.logger = GlobalLogger.getLogger().newSubLogger("Bot").newSubLogger(name);
        this.protocol = protocol;
        this.ticker = new BotTicker(this);
    }

    public void connect(String host, int port) {
        if (client != null && client.getSession().isConnected()) {
            throw new IllegalStateException("Can not start connection. Bot already connected!");
        }

        client = new Client(host, port, protocol, new TcpSessionFactory());
        client.getSession().addListener(new BotListener(this));
        client.getSession().connect();
        ticker.start();
    }

    public void disconnect(String reason) {
        ticker.stop();

        if (client == null || !client.getSession().isConnected()) {
            return;
        }

        this.task.stop();
        client.getSession().disconnect(reason);
    }

    public void setTask(Task activity) {
        if (this.task != null) {
            this.task.stop();
        }
        this.task = activity;
    }

    public Task getTask() {
        return this.task;
    }

    public Logger getLogger() {
        return logger;
    }

    public SaneAStar getPathFinder() {
        return pathFinder;
    }

    public MinecraftProtocol getProtocol() {
        return protocol;
    }

    public String getName() {
        return name;
    }

    public Client getClient() {
        return client;
    }

    public World getWorld() {
        return world;
    }

    public Server getServer() {
        return server;
    }

    public BotPlayer getPlayer() {
        return player;
    }

    public void setWorld(World world) {
        this.world = world;
        // TODO: This shouldn't go here
        if (this.pathFinder == null) {
            this.pathFinder = new SaneAStar(new EuclideanHeuristic(), new SimpleWorldPhysics(world));
        }
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setPlayer(BotPlayer player) {
        this.player = player;
    }

}
