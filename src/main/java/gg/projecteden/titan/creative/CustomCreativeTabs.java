package gg.projecteden.titan.creative;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.utils.NexusAPI;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustomCreativeTabs {

    public static final Map<String, String> CATEGORY_PREFIXES = new HashMap<>();
    public static final Map<String, CreativeModeTab> GROUPS = new HashMap<>();

    public static void init(String title, CustomCreativeItem icon, CustomCreativeItem[] items) {
        String id = toId(title);
        ResourceKey<CreativeModeTab> CUSTOM_CREATIVE_TAB_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Titan.id(id));
        CreativeModeTab CUSTOM_CREATIVE_TAB = FabricCreativeModeTab.builder()
                .title(Component.literal(title))
                .icon(icon::getItemStack)
                .displayItems((params, output) -> {
                    for (CustomCreativeItem item : items)
                        if (icon.getItemStack() != null)
                            output.accept(item.getItemStack(), TabVisibility.PARENT_AND_SEARCH_TABS);
                })
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_CREATIVE_TAB_KEY, CUSTOM_CREATIVE_TAB);
        GROUPS.put(id, CUSTOM_CREATIVE_TAB);
    }

    public static void init() {
        CustomCreativeItem[] items = NexusAPI.getItems();

        CustomCreativeItem[] categories = NexusAPI.getCategories();
        for (int i = 0; i < categories.length; i++) {
            CATEGORY_PREFIXES.put(categories[i].category, String.valueOf((char) ('a' + (i % 26))));
            String id = toId(categories[i].category);
            CustomCreativeItem[] categoryItems = Arrays.stream(items).filter(item -> toId(item.category).equals(id)).toArray(CustomCreativeItem[]::new);
            CustomCreativeTabs.init(categories[i].category, categories[i], categoryItems);
        }
    }

    public static String toId(String title) {
        // We need this prefix because Fabric orders them alphabetically by namespacedkey
        return CATEGORY_PREFIXES.get(title) + "_" + title.toLowerCase().replace(" ", "_").replace(":", "");
    }

}
