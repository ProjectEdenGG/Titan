package gg.projecteden.titan.config;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.config.annotations.Description;
import gg.projecteden.titan.config.annotations.Group;
import gg.projecteden.titan.config.annotations.Name;
import gg.projecteden.titan.config.annotations.OldConfig;
import gg.projecteden.titan.discord.RichPresence;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gg.projecteden.titan.config.annotations.Group.BACKPACKS;
import static gg.projecteden.titan.config.annotations.Group.DEVELOPER;
import static gg.projecteden.titan.config.annotations.Group.SATURN;
import static gg.projecteden.titan.config.annotations.Group.UTILITIES;
import static gg.projecteden.titan.utils.Utils.isOnEden;

@Getter
@Setter
public class ConfigItem<T> {

    private T value;
    private T defaultValue;

    public ConfigItem(T defaultValue) {
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public Type getType() {
        if (value instanceof Boolean)
            return Type.BOOLEAN;
        if (value instanceof Enum<?>)
            return Type.ENUM;
        if (value instanceof Integer)
            return Type.INTEGER;
        if (value instanceof Double)
            return Type.DOUBLE;
        if (value instanceof String)
            return Type.STRING;
        return Type.UNKNOWN;
    }

    public void onUpdate(T newValue) {}

    public enum Type {
        BOOLEAN,
        ENUM,
        INTEGER,
        DOUBLE,
        STRING,
        UNKNOWN
    }

    public static List<Field> getAllNonDeveloperOptions() {
        return Arrays.stream(ConfigItem.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> f.getType() == ConfigItem.class)
                .filter(f -> f.isAnnotationPresent(Group.class) && !f.getAnnotation(Group.class).value().equals(DEVELOPER))
                .collect(Collectors.toList());
    }

    public static List<Field> getDeveloperOptions() {
        return Arrays.stream(ConfigItem.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> f.getType() == ConfigItem.class)
                .filter(f -> f.isAnnotationPresent(Group.class) && f.getAnnotation(Group.class).value().equals(DEVELOPER))
                .collect(Collectors.toList());
    }

    @Group(SATURN)
    @Name(value = "Update Instances", config = "update-instances")
    @Description("When should Saturn be updated?")
    public static final ConfigItem<SaturnUpdater.Mode> SATURN_UPDATE_INSTANCES = new ConfigItem<>(SaturnUpdater.Mode.BOTH);

    @Group(SATURN)
    @Name(value = "Enabled by Default", config = "enabled-default")
    @Description("Should Saturn be enabled by default?")
    @OldConfig("saturn-enabled-default")
    public static final ConfigItem<Boolean> SATURN_ENABLED_DEFAULT = new ConfigItem<>(true);

    @Group(SATURN)
    @Name(value = "Manage Status", config = "manage-status")
    @OldConfig("saturn-manage-status")
    @Description("Should Titan enable and disable Saturn\nautomatically when playing on Project Eden?")
    public static final ConfigItem<Boolean> SATURN_MANAGE_STATUS = new ConfigItem<>(false);

    @Group(SATURN)
    @Name(value = "Hard Reset", config = "hard-reset")
    @OldConfig("saturn-hard-reset")
    @Description("""
                            Should Titan remove any local changes to Saturn on update?

                            This is a developer setting. Do not change unless you know what you're doing!""")
    public static final ConfigItem<Boolean> SATURN_HARD_RESET = new ConfigItem<>(true);

    @Group(UTILITIES)
    @Name("Stop Entity Culling")
    @Description("""
                            Should Titan prevent ArmorStands and ItemFrames from
                            un-rendering when inside a certain radius?

                            This prevents flickering and has no impact on performance.""")
    public static final ConfigItem<Boolean> STOP_ENTITY_CULLING = new ConfigItem<>(true);

    @Group(UTILITIES)
    @Name("Stop Custom Block Flashing")
    @Description("""
                            Should Titan stop custom blocks from flashing
                            when placing/breaking nearby blocks?""")
    public static final ConfigItem<Boolean> STOP_CUSTOM_BLOCK_FLASHING = new ConfigItem<>(true);

    @Group(UTILITIES)
    @Name("Discord Rich Presence")
    @Description("Should we update your Discord Status while on Project Eden")
    public static final ConfigItem<Boolean> DISCORD_RICH_PRESENCE = new ConfigItem<>(true) {
        @Override
        public void onUpdate(Boolean newValue) {
            if (!isOnEden())
                return;
            if (getValue())
                RichPresence.start();
            else
                RichPresence.stop();
        }
    };

    @Group(UTILITIES)
    @Name("Chat Channel Renderer")
    @Description("Should your current chat channel be rendered in chat")
    public static final ConfigItem<Boolean> CHAT_CHANNEL_RENDER = new ConfigItem<>(true);

    @Group(BACKPACKS)
    @Name("Show Backpack Previews")
    @Description("Should Titan render previews of Backpacks\nwhen hovered in your inventory?")
    public static final ConfigItem<Boolean> DO_BACKPACK_PREVIEWS = new ConfigItem<>(true);

    @Group(BACKPACKS)
    @Name("Backpack Previews Require Shift-Key")
    @Description("Should Backpack previews only show\nwhile the shift-key is pressed?")
    public static final ConfigItem<Boolean> PREVIEWS_REQUIRE_SHIFT = new ConfigItem<>(true);

    @Group(BACKPACKS)
    @Name("Use Backpack Colors")
    @Description("Should Backpack previews use the color of the Backpack?")
    public static final ConfigItem<Boolean> USE_BACKGROUND_COLORS = new ConfigItem<>(true);

    @Group(DEVELOPER)
    @Name("Debug")
    @Description("Enable debugging for Titan\nThis prints lots of information about the client")
    public static final ConfigItem<Boolean> DEBUG = new ConfigItem<>(false) {
        @Override
        public void onUpdate(Boolean newValue) {
            Titan.debug = newValue;
        }

        @Override
        public Boolean getValue() {
            return Titan.debug;
        }
    };

    @Group(DEVELOPER)
    @Name("Saturn Branch")
    @Description("Swap the branch Saturn is currently on\nThis will take some time to execute!")
    public static final ConfigItem<String> SATURN_BRANCH = new ConfigItem<>("titan") {
        @Override
        public void onUpdate(String newValue) {
            Saturn.getUpdater().branch(newValue);
        }

        @Override
        public String getValue() {
            return Saturn.getUpdater().branch();
        }
    };

}
