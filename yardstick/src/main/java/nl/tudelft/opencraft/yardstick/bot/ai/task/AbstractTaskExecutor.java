package nl.tudelft.opencraft.yardstick.bot.ai.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.tudelft.opencraft.yardstick.bot.Bot;
import nl.tudelft.opencraft.yardstick.logging.SubLogger;

public abstract class AbstractTaskExecutor implements TaskExecutor {

    @JsonIgnore
    protected final Bot bot;
    protected final String shortName;
    @JsonIgnore
    protected final SubLogger logger;
    protected TaskStatus lastStatus;

    public AbstractTaskExecutor(Bot bot) {
        this.bot = bot;
        this.shortName = getClass().getSimpleName();
        this.logger = bot.getLogger().newSubLogger(shortName);
    }

    @Override
    public final String getShortName() {
        return shortName;
    }

    @Override
    public final TaskStatus tick() {
        return this.lastStatus = onTick();
    }

    @Override
    public final void stop() {
        onStop();
    }

    @Override
    public final TaskStatus getStatus() {
        if (lastStatus == null) {
            return TaskStatus.forInProgress();
        } else {
            return lastStatus;
        }
    }

    protected abstract TaskStatus onTick();

    protected abstract void onStop();
}