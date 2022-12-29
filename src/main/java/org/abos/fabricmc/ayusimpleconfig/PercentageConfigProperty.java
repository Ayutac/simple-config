package org.abos.fabricmc.ayusimpleconfig;

import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A percentage configuration property, which is really just an integer configuration property in disguise.
 * Can save default values, a range and optionally declare a property as a game rule.
 * @see IntConfigProperty
 * @see #getDecimalValue(World)
 */
public class PercentageConfigProperty extends IntConfigProperty {

    /**
     * Creates a new {@link PercentageConfigProperty}.
     *
     * @param name         the name of the property and also of the game rule if <code>withGameRule</code> is <code>true</code>
     * @param namespace    the namespace of the property
     * @param defaultValue the default value, which will only be validated against <code>null</code>
     * @param minValue     the minimum value of this property
     * @param maxValue     the maximum value of this property
     * @param withGameRule if this property should be treated as a game rule
     * @param ruleCategory should be not <code>null</code> exactly when <code>withGameRule</code> is <code>true</code>
     * @see #PercentageConfigProperty(String, String, Integer, int, int)
     * @see #PercentageConfigProperty(String, String, Integer, int, int, GameRules.Category)
     */
    protected PercentageConfigProperty(@NotNull String name, @Nullable String namespace, @NotNull Integer defaultValue, int minValue, int maxValue, boolean withGameRule, GameRules.Category ruleCategory) {
        super(name, namespace, defaultValue, minValue, maxValue, withGameRule, ruleCategory);
    }

    /**
     * Creates a new {@link PercentageConfigProperty} that is not a game rule.
     *
     * @param name         the name of the property
     * @param namespace    the namespace of the property
     * @param defaultValue the default value, which will only be validated against <code>null</code>
     * @param minValue     the minimum value of this property
     * @param maxValue     the maximum value of this property
     * @see #PercentageConfigProperty(String, String, Integer, int, int, GameRules.Category)
     */
    public PercentageConfigProperty(@NotNull String name, @Nullable String namespace, @NotNull Integer defaultValue, int minValue, int maxValue) {
        this(name, namespace, defaultValue, minValue, maxValue, false, null);
    }

    /**
     * Creates a new {@link PercentageConfigProperty} which is also a game rule.
     *
     * @param name         the name of the property and also of the game rule
     * @param namespace    the namespace of the property
     * @param defaultValue the default value, which will only be validated against <code>null</code>
     * @param minValue     the minimum value of this property
     * @param maxValue     the maximum value of this property
     * @param ruleCategory the category of the rule
     * @see #PercentageConfigProperty(String, String, Integer, int, int)
     */
    public PercentageConfigProperty(@NotNull String name, @Nullable String namespace, @NotNull Integer defaultValue, int minValue, int maxValue, @NotNull GameRules.Category ruleCategory) {
        this(name, namespace, defaultValue, minValue, maxValue, true, Objects.requireNonNull(ruleCategory));
    }

    /**
     * @return {@link #getValue(World)}<code>/100d</code>
     */
    public double getDecimalValue(World world) {
        return getValue(world) / 100d;
    }

    /**
     * @see #getDecimalValue(World)
     */
    public double getDecimalValue() {
        return getDecimalValue(null);
    }
}
