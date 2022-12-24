# Ayutac's Simple Config

A very basic and very lightweight configuration library for very basic needs. Like say, you make a mod and don't really care about configurations for your mod's users but want to address their needs anyway? Then this might be the perfect config lib for you.

## Features
- save integer config values (with a range)
- save boolean config values
- no comments
- no annotations
- that's it

## Setup & Use

```gradle
dependencies {
    modImplementation "org.abos.fabricmc:simpleconfig:<VERSION>"
    include "org.abos.fabricmc:simpleconfig:<VERSION>"
}
```
Since this lib is so small, `include` is recommended to avoid your users having to add yet another mod dependency.

Once the Simple Config is on your build path, you can create your config class like so:

```java
public final class Config extends AbstractConfig {

    public static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("yourmodid.json");

    private final IntConfigProperty yourIntProperty = new IntConfigProperty("your_int_property", /*defaultValue*/ 1, GameRules.Category.MISC);

    private final BooleanConfigProperty yourBooleanProperty = new BooleanConfigProperty("your_boolean_property", /*defaultValue*/ true, GameRules.Category.MISC);

    @Override
    public Iterator<ConfigProperty<?, ? extends GameRules.Rule<?>>> iterator() {
        // note that these items don't have to be in any particular order
        return List.of(yourIntProperty, yourBooleanProperty).iterator();
    }

    public IntConfigProperty getYourIntProperty() {
        return yourIntProperty;
    }

    public BooleanConfigProperty getYourBooleanProperty() {
        return yourBooleanProperty;
    }
}
```

Next just add this to your mod initializer:
```java
public class YourMod implements ModInitializer {
    public final static Config CONFIG = new Config();

    @Override
    public void onInitialize() {
        CONFIG.initialize();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> CONFIG.loadFrom(Config.PATH, server));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> CONFIG.saveTo(Config.PATH, server.getOverworld()));
    }
}
```

Now you are all set.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

