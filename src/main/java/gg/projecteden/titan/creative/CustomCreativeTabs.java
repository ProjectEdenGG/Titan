package gg.projecteden.titan.creative;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.utils.NexusAPI;
import gg.projecteden.titan.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Row;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustomCreativeTabs {

    public static final Map<String, String> CATEGORY_PREFIXES = new HashMap<>();
    public static final Map<String, CreativeModeTab> GROUPS = new HashMap<>();
    public static final Map<String, CustomCreativeItem[]> ITEMS = new HashMap<>();

    public static void init(String title, CustomCreativeItem icon, int index) {
        String id = toId(title);
        CreativeModeTab group = register(id, new CreativeModeTab.Builder(index % 10 < 5 ? Row.TOP : Row.BOTTOM, index % 5)
                .title(Component.literal(title))
                .icon(icon::getItemStack)
                .displayItems(((displayContext, entries) -> {
                    if (!ITEMS.containsKey(id) || ITEMS.get(id) == null)
                        return;
                    for (CustomCreativeItem item : ITEMS.get(id))
                        if (icon.getItemStack() != null)
                            entries.accept(item.getItemStack(), TabVisibility.PARENT_AND_SEARCH_TABS);
                }))
                .build());

        GROUPS.put(id, group);
    }

    public static <T extends CreativeModeTab> T register(String name, T itemGroup) {
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Titan.id(name), itemGroup);
    }

    public static void update() {
        ITEMS.clear();
        CustomCreativeItem[] items = NexusAPI.getItems();

        for (String id : GROUPS.keySet()) {
            ITEMS.put(id, Arrays.stream(items).filter(item -> toId(item.category).equals(id)).toArray(CustomCreativeItem[]::new));

            CreativeModeTab group = GROUPS.get(id);
            group.buildContents(new ItemDisplayParameters(FeatureFlagSet.of(FeatureFlags.VANILLA), true, null));
        }
    }

    public static void updateAsync() {
        new Thread(CustomCreativeTabs::update).start();
    }

    public static void clear() {
        ITEMS.clear();

        for (String id : GROUPS.keySet()) {
            CreativeModeTab group = GROUPS.get(id);
            group.buildContents(new ItemDisplayParameters(FeatureFlagSet.of(FeatureFlags.VANILLA), true, null));
        }
    }

    public static void init() {
        CustomCreativeItem[] categories = NexusAPI.getCategories();
        for (int i = 0; i < categories.length; i++) {
            CATEGORY_PREFIXES.put(categories[i].category, String.valueOf((char) ('a' + (i % 26))));
            CustomCreativeTabs.init(categories[i].category, categories[i], i);
        }

        update();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            updateAsync();
        });

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            if (!Utils.isOnEden())
                clear();
        }));
    }

    public static String toId(String title) {
        // We need this prefix because Fabric orders them alphabetically by namespacedkey
        return CATEGORY_PREFIXES.get(title) + "_" + title.toLowerCase().replace(" ", "_").replace(":", "");
    }

}
