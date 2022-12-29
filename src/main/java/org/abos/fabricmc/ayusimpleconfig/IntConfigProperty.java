package org.abos.fabricmc.ayusimpleconfig;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * An integer configuration property. Can save default values, a range and optionally declare a property as a game rule.
 * @see ConfigProperty
 */
public class IntConfigProperty extends ConfigProperty<Integer, GameRules.IntRule> {

    private final int minValue;

    private final int maxValue;

    /**
     * Creates a new {@link IntConfigProperty}.
     *
     * @param name         the name of the property and also of the game rule if <code>withGameRule</code> is <code>true</code>
     * @param namespace    the namespace of the property
     * @param defaultValue the default value, which will only be validated against <code>null</code>
     * @param minValue     the minimum value of this property
     * @param maxValue     the maximum value of this property
     * @param withGameRule if this property should be treated as a game rule
     * @param ruleCategory should be not <code>null</code> exactly when <code>withGameRule</code> is <code>true</code>
     * @see #IntConfigProperty(String, String, Integer, int, int)
     * @see #IntConfigProperty(String, String, Integer, int, int, GameRules.Category)
     */
    protected IntConfigProperty(@NotNull String name, @Nullable String namespace, @NotNull Integer defaultValue, int minValue, int maxValue, boolean withGameRule, GameRules.Category ruleCategory) {
        super(name, namespace, defaultValue, withGameRule, ruleCategory);
        if (maxValue < minValue) {
            throw new IllegalArgumentException("Min value must be smaller than or equal to max value!");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        validate(defaultValue);
    }

    /**
     * Creates a new {@link IntConfigProperty} that is not a game rule.
     *
     * @param name         the name of the property
     * @param namespace    the namespace of the property
     * @param defaultValue the default value, which will only be validated against <code>null</code>
     * @param minValue     the minimum value of this property
     * @param maxValue     the maximum value of this property
     * @see #IntConfigProperty(String, String, Integer, int, int, GameRules.Category)
     */
    public IntConfigProperty(@NotNull String name, @Nullable String namespace, @NotNull Integer defaultValue, int minValue, int maxValue) {
        this(name, namespace, defaultValue, minValue, maxValue, false, null);
    }

    /**
     * Creates a new {@link IntConfigProperty} which is also a game rule.
     *
     * @param name         the name of the property and also of the game rule
     * @param namespace    the namespace of the property
     * @param defaultValue the default value, which will only be validated against <code>null</code>
     * @param minValue     the minimum value of this property
     * @param maxValue     the maximum value of this property
     * @param ruleCategory the category of the rule
     * @see #IntConfigProperty(String, String, Integer, int, int)
     */
    public IntConfigProperty(@NotNull String name, @Nullable String namespace, @NotNull Integer defaultValue, int minValue, int maxValue, @NotNull GameRules.Category ruleCategory) {
        this(name, namespace, defaultValue, minValue, maxValue, true, Objects.requireNonNull(ruleCategory));
    }

    /**
     * @return the minimum value this property can attain
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * @return the maximum value this property can attain
     */
    public int getMaxValue() {
        return maxValue;
    }

    @Override
    @Nullable
    protected Integer getRuleValue(@NotNull World world) throws IllegalStateException {
        if (!isWithGameRule()) {
            throw new IllegalStateException("This property doesn't have a rule!");
        }
        GameRules.IntRule rule = world.getGameRules().get(getRuleKey());
        if (rule == null) {
            AbstractConfig.LOGGER.warn("Rule "+getRuleName()+" couldn't be found!");
            return null;
        }
        return rule.get();
    }

    @Override
    protected void setRuleValue(@NotNull Integer value, @NotNull MinecraftServer server) throws IllegalStateException {
        GameRules.IntRule rule = getRule(server.getOverworld());
        if (rule != null) {
            rule.set(value, server);
        }
        else {
            AbstractConfig.LOGGER.warn("Rule "+getRuleName()+" couldn't be found!");
        }
    }

    @Override
    @NotNull
    public Integer validate(Integer value) throws IllegalArgumentException {
        if (value < getMinValue()) {
            throw new IllegalArgumentException(getName()+" value must be greater than or equal to "+getMinValue()+"!");
        }
        if (value > getMaxValue()) {
            throw new IllegalArgumentException(getName()+" value must be smaller than or equal to "+getMaxValue()+"!");
        }
        return value;
    }

    @Override
    public GameRules.Key<GameRules.IntRule> registerRule() throws IllegalStateException {
        if (!isWithGameRule()) {
            throw new IllegalStateException("Only rules can be registered! "+getName()+" is not a rule!");
        }
        if (ruleKey != null) {
            throw new IllegalStateException("Attempted to register "+getRuleName()+" twice!");
        }
        return ruleKey = GameRuleRegistry.register(getRuleName(), getRuleCategory(), GameRuleFactory.createIntRule(getDefaultValue()));
    }
}
