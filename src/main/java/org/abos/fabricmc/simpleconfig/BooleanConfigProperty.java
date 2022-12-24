package org.abos.fabricmc.simpleconfig;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * A boolean configuration property. Can save default values and optionally declare a property as a game rule.
 * @see ConfigProperty
 */
public class BooleanConfigProperty extends ConfigProperty<Boolean, GameRules.BooleanRule> {

    /**
     * Creates a new {@link BooleanConfigProperty} that is not a game rule.
     * @param name the name of the property
     * @param defaultValue the default value
     * @see #BooleanConfigProperty(String, Boolean, GameRules.Category)
     */
    public BooleanConfigProperty(@NotNull String name, @NotNull Boolean defaultValue) {
        super(name, defaultValue);
    }

    /**
     * Creates a new {@link BooleanConfigProperty} which is also a game rule.
     * @param name the name of the property and also of the game rule
     * @param defaultValue the default value
     * @param ruleCategory the category of the rule
     * @see #BooleanConfigProperty(String, Boolean)
     */
    public BooleanConfigProperty(@NotNull String name, @NotNull Boolean defaultValue, @NotNull GameRules.Category ruleCategory) {
        super(name, defaultValue, ruleCategory);
    }

    @Override
    @Nullable
    protected Boolean getRuleValue(@NotNull World world) throws IllegalStateException {
        if (!isWithGameRule()) {
            throw new IllegalStateException("This property doesn't have a rule!");
        }
        GameRules.BooleanRule rule = world.getGameRules().get(getRuleKey());
        if (rule == null) {
            AbstractConfig.LOGGER.warn("Rule "+getName()+" couldn't be found!");
            return null;
        }
        return rule.get();
    }

    @Override
    protected void setRuleValue(@NotNull Boolean value, @NotNull MinecraftServer server) throws IllegalStateException {
        GameRules.BooleanRule rule = getRule(server.getOverworld());
        if (rule != null) {
            rule.set(value, server);
        }
        else {
            AbstractConfig.LOGGER.warn("Rule "+getName()+" couldn't be found!");
        }
    }

    @Override
    @NotNull
    public Boolean validate(Boolean value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Value for "+getName()+" must be true or false!");
        }
        return value;
    }

    @Override
    public GameRules.Key<GameRules.BooleanRule> registerRule() throws IllegalStateException {
        if (!isWithGameRule()) {
            throw new IllegalStateException("Only rules can be registered! "+getName()+" is not a rule!");
        }
        if (ruleKey != null) {
            throw new IllegalStateException("Attempted to register "+getName()+" twice!");
        }
        return ruleKey = GameRuleRegistry.register(getName(), getRuleCategory(), GameRuleFactory.createBooleanRule(getDefaultValue()));
    }
}
