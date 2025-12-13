package gg.projecteden.titan.creative;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;

@NoArgsConstructor
@AllArgsConstructor
public class CustomCreativeItem {

    String category;
    String item;

    public ItemStack getItemStack() {
        CompoundTag nbt = getNbt();
        if (nbt == null)
            return null;

        if (!nbt.contains("id"))
            return null;

        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(nbt.getString("id").get()));
        ItemStack itemStack = new ItemStack(item);

        if (nbt.contains("count"))
            itemStack.setCount(nbt.getInt("count").get());

        if (nbt.contains("components")) {
            CompoundTag components = nbt.getCompound("components").get();
            parseComponents(components, itemStack);
        }

        return itemStack;
    }

    private CompoundTag getNbt() {
        try {
            return TagParser.parseCompoundFully(item);
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    /**
     * custom_data
     * custom_name
     * lore
     * item_model
     * dyed_color
     */
    private void parseComponents(CompoundTag components, ItemStack itemStack) {
        if (components.contains("minecraft:custom_data")) {
            CompoundTag customData = components.getCompound("minecraft:custom_data").get();
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
        }

        if (components.contains("minecraft:custom_name")) {
            CompoundTag customName = components.getCompound("minecraft:custom_name").get();
            itemStack.set(DataComponents.CUSTOM_NAME, parseText(customName));
        }

        if (components.contains("minecraft:lore")) {
            ListTag lore = components.getList("minecraft:lore").get();
            itemStack.set(DataComponents.LORE, new ItemLore(lore.stream().map(line -> parseText(line)).toList()));
        }

        if (components.contains("minecraft:item_model")) {
            String itemModel = components.getString("minecraft:item_model").get();
            itemStack.set(DataComponents.ITEM_MODEL, Identifier.parse(itemModel));
        }

        if (components.contains("minecraft:dyed_color")) {
            int dyedColor = components.getInt("minecraft:dyed_color").get();
            itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(dyedColor));
        }
    }

    public static Component parseText(Tag tag) {
        return parseText(tag.toString());
    }

    public static Component parseText(String jsonString) {
        if (jsonString.equals("\"\""))
            return Component.empty();

        JsonElement element = JsonParser.parseString(jsonString);

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return Component.literal(element.getAsString());
        }

        JsonObject json = element.getAsJsonObject();
        String baseText = json.has("text") ? json.get("text").getAsString() : "";
        MutableComponent result = Component.literal(baseText);

        if (json.has("extra") && json.get("extra").isJsonArray()) {
            JsonArray extras = json.getAsJsonArray("extra");
            for (JsonElement extraElement : extras) {
                JsonObject obj = extraElement.getAsJsonObject();
                String extraText = obj.get("text").getAsString();

                Style style = Style.EMPTY;
                if (obj.has("bold")) style = style.withBold(obj.get("bold").getAsBoolean());
                if (obj.has("italic")) style = style.withItalic(obj.get("italic").getAsBoolean());
                if (obj.has("underlined")) style = style.withUnderlined(obj.get("underlined").getAsBoolean());
                if (obj.has("strikethrough")) style = style.withStrikethrough(obj.get("strikethrough").getAsBoolean());
                if (obj.has("obfuscated")) style = style.withObfuscated(obj.get("obfuscated").getAsBoolean());
                if (obj.has("color")) {
                    String color = obj.get("color").getAsString();
                    if (color.startsWith("#")) {
                        int rgb = Integer.parseInt(color.substring(1), 16);
                        style = style.withColor(rgb);
                    }
                    else {
                        ChatFormatting formatting = ChatFormatting.getByName(color);
                        if (formatting != null) style = style.withColor(formatting);
                    }
                }

                result.append(Component.literal(extraText).setStyle(style));
            }
        }

        return result;
    }

}
