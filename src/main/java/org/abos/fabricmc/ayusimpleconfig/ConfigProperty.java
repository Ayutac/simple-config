package org.abos.fabricmc.ayusimpleconfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * An abstract configuration property. Can save default values and optionally declare a property as a game rule.
 * @param <T> The type of the property.
 * @param <R> The rule type corresponding to <code>T</code>.
 * @see IntConfigProperty
 * @see BooleanConfigProperty
 */
public abstract class ConfigProperty<T, R extends GameRules.Rule<R>> {

    private final String name;

    /**
     * the cached value
     */
    private T value;

    private final T defaultValue;

    private final boolean withGameRule;

    protected GameRules.Key<R> ruleKey;

    private final GameRules.Category ruleCategory;

    /**
     * Creates a new {@link ConfigProperty}.
     * @param name the name of the property and also of the game rule if <code>withGameRule</code> is <code>true</code>
     * @param defaultValue the default value, which will only be validated against <code>null</code>
     * @param withGameRule if this property should be treated as a game rule
     * @param ruleCategory should be not <code>null</code> exactly when <code>withGameRule</code> is <code>true</code>
     * @see #ConfigProperty(String, Object)
     * @see #ConfigProperty(String, Object, GameRules.Category)
     * @see #validate(Object)
     */
    protected ConfigProperty(@NotNull String name, @NotNull T defaultValue, boolean withGameRule, GameRules.Category ruleCategory) {
        this.name = Objects.requireNonNull(name);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        resetValue();
        this.withGameRule = withGameRule;
        this.ruleCategory = ruleCategory;
    }

    /**
     * Creates a new {@link ConfigProperty} that is not a game rule.
     * @param name the name of the property
     * @param defaultValue the default value, which will only be validated against <code>null</code>
     * @see #ConfigProperty(String, Object, GameRules.Category)
     * @see #validate(Object)
     */
    public ConfigProperty(@NotNull String name, @NotNull T defaultValue) {
        this(name, defaultValue, false, null);
    }


    /**
     * Creates a new {@link ConfigProperty} which is also a game rule.
     * @param name the name of the property and also of the game rule
     * @param defaultValue the default value, which will only be validated against <code>null</code>
     * @param ruleCategory the category of the rule
     * @see #ConfigProperty(String, Object)
     * @see #validate(Object)
     */
    public ConfigProperty(@NotNull String name, @NotNull T defaultValue, @NotNull GameRules.Category ruleCategory) {
        this(name, defaultValue, true, Objects.requireNonNull(ruleCategory));
    }

    /**
     * @return the name of this property and maybe rule
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * @return the default value of this property
     */
    @NotNull
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the value from the given rule. Use {@link #getValue(World)} instead of this method.
     * @return the rule value or <code>null</code> if the rule couldn't be found
     * @throws IllegalStateException If this method is called but {@link #isWithGameRule()} returns <code>false</code>.
     * @see #getValue(World)
     */
    @Nullable
    protected abstract T getRuleValue(@NotNull World world) throws IllegalStateException;

    /**
     * Returns the value from the given rule if it exists or the cached value.
     * @param world needed for accessing the game rule value
     * @return the cached value if the game rule doesn't exist, wasn't accessible or <code>world</code> was <code>null</code>,
     * else the game rule value
     */
    @NotNull
    public T getValue(@Nullable World world) {
        if (isWithGameRule() && world != null) {
            T rVal = getRuleValue(world);
            if (rVal == null) {
                AbstractConfig.LOGGER.warn("Rule "+getName()+" couldn't be found!");
                return value;
            }
            return value = rVal;
        }
        return value;
    }

    /**
     * @return the cached value
     */
    @NotNull
    public T getValue() {
        return getValue(null);
    }

    /**
     * Sets the rule to the given value. Use {@link #setValue(Object, MinecraftServer)} instead of this method.
     * If the rule should exist but cannot be found and updated, no exception will be thrown.
     * This method does NOT validate the given new value, that should happen in {@link #setValue(Object, MinecraftServer)}.
     * @throws IllegalStateException  If this method is called but {@link #isWithGameRule()} returns <code>false</code>.
     * @see #setValue(Object, MinecraftServer)
     */
    protected abstract void setRuleValue(@NotNull T value, @NotNull MinecraftServer server) throws IllegalStateException;

    /**
     * Sets the cached value to the specified one and updates the game rule, if existent and possible.
     * @param value the new value
     * @param server needed for accessing the game rule value
     * @throws IllegalArgumentException If validation of <code>value</code> fails.
     * @see #validate(Object)
     */
    public void setValue(@NotNull T value, @Nullable MinecraftServer server) throws IllegalArgumentException {
        this.value = validate(value);
        if (isWithGameRule() && server != null) {
            setRuleValue(value, server);
        }
    }

    /**
     * Sets the cached value to the specified one.
     * @param value the new value
     * @throws IllegalArgumentException If validation of <code>value</code> fails.
     * @see #validate(Object)
     */
    public void setValue(@NotNull T value) {
        setValue(value, null);
    }

    /**
     * Resets the cached value to the default value. Rule values are not affected.
     */
    public void resetValue() {
        value = getDefaultValue();
    }

    /**
     * Validates the input parameter or throws an {@link IllegalArgumentException}l
     * @param value the value to be validated
     * @return the input parameter
     * @throws IllegalArgumentException If the given value is invalid.
     */
    @NotNull
    public abstract T validate(T value) throws IllegalArgumentException;

    /**
     * @return <code>true</code> if this property is also a game rule
     */
    public boolean isWithGameRule() {
        return withGameRule;
    }

    /**
     * @return the rule key if this property is also a game rule AND has been registered, else <code>null</code>
     * @see #registerRule()
     */
    public GameRules.Key<R> getRuleKey() {
        return ruleKey;
    }

    /**
     * @return the rule, if this property is one, and after it has been registered, else <code>null</code>.
     */
    public R getRule(@NotNull World world) {
        Objects.requireNonNull(world);
        if (!isWithGameRule()) {
            return null;
        }
        return world.getGameRules().get(ruleKey);
    }

    /**
     * Registers this property's game rule.
     * @return the game rule key, which is also accessible via {@link #getRuleKey()} after calling this method
     * @throws IllegalStateException If this property is not a rule or if this method is called more than once.
     */
    public abstract GameRules.Key<R> registerRule() throws IllegalStateException;

    /**
     * @return this rule's category or <code>null</code> if this rule isn't a category
     */
    public GameRules.Category getRuleCategory() {
        return ruleCategory;
    }
}
