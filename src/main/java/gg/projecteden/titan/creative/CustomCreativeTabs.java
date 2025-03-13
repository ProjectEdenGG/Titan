package gg.projecteden.titan.creative;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.utils.NexusAPI;
import gg.projecteden.titan.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroup.DisplayContext;
import net.minecraft.item.ItemGroup.Row;
import net.minecraft.item.ItemGroup.StackVisibility;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustomCreativeTabs {

    public static final Map<String, String> CATEGORY_PREFIXES = new HashMap<>();
    public static final Map<String, ItemGroup> GROUPS = new HashMap<>();
    public static final Map<String, CustomCreativeItem[]> ITEMS = new HashMap<>();

    public static void init(String title, CustomCreativeItem icon, int index) {
        String id = toId(title);
        ItemGroup group = register(id, new ItemGroup.Builder(index % 10 < 5 ? Row.TOP : Row.BOTTOM, index % 5)
                .displayName(Text.literal(title))
                .icon(icon::getItemStack)
                .entries(((displayContext, entries) -> {
                    if (!ITEMS.containsKey(id) || ITEMS.get(id) == null)
                        return;
                    for (CustomCreativeItem item : ITEMS.get(id))
                        if (icon.getItemStack() != null)
                            entries.add(item.getItemStack(), StackVisibility.PARENT_AND_SEARCH_TABS);
                }))
                .build());

        GROUPS.put(id, group);
    }

    public static <T extends ItemGroup> T register(String name, T itemGroup) {
        return Registry.register(Registries.ITEM_GROUP, Titan.id(name), itemGroup);
    }

    public static void update() {
        ITEMS.clear();
        CustomCreativeItem[] items = NexusAPI.getItems();

        for (String id : GROUPS.keySet()) {
            ITEMS.put(id, Arrays.stream(items).filter(item -> toId(item.category).equals(id)).toArray(CustomCreativeItem[]::new));

            ItemGroup group = GROUPS.get(id);
            group.updateEntries(new DisplayContext(FeatureSet.of(FeatureFlags.VANILLA), true, null));
        }
    }

    public static void updateAsync() {
        new Thread(CustomCreativeTabs::update).start();
    }

    public static void clear() {
        ITEMS.clear();

        for (String id : GROUPS.keySet()) {
            ItemGroup group = GROUPS.get(id);
            group.updateEntries(new DisplayContext(FeatureSet.of(FeatureFlags.VANILLA), true, null));
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
