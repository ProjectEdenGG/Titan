package gg.projecteden.titan.creative;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@NoArgsConstructor
@AllArgsConstructor
public class CustomCreativeItem {

    String category;
    String item;

    public ItemStack getItemStack() {
        NbtCompound nbt = getNbt();
        if (nbt == null)
            return null;

        if (!nbt.contains("id"))
            return null;

        Item item = Registries.ITEM.get(Identifier.of(nbt.getString("id").get()));
        ItemStack itemStack = new ItemStack(item);

        if (nbt.contains("count"))
            itemStack.setCount(nbt.getInt("count").get());

        if (nbt.contains("components")) {
            NbtCompound components = nbt.getCompound("components").get();
            parseComponents(components, itemStack);
        }

        return itemStack;
    }

    private NbtCompound getNbt() {
        try {
            return StringNbtReader.readCompound(item);
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
    private void parseComponents(NbtCompound components, ItemStack itemStack) {
        if (components.contains("minecraft:custom_data")) {
            NbtCompound customData = components.getCompound("minecraft:custom_data").get();
            itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
        }

        if (components.contains("minecraft:custom_name")) {
            String customName = components.getString("minecraft:custom_name").get();
            itemStack.set(DataComponentTypes.CUSTOM_NAME, parseText(customName));
        }

        if (components.contains("minecraft:lore")) {
            NbtList lore = components.getList("minecraft:lore").get();
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(lore.stream().map(line -> parseText(line.asString().get())).toList()));
        }

        if (components.contains("minecraft:item_model")) {
            String itemModel = components.getString("minecraft:item_model").get();
            itemStack.set(DataComponentTypes.ITEM_MODEL, Identifier.of(itemModel));
        }

        if (components.contains("minecraft:dyed_color")) {
            NbtCompound dyedColor = components.getCompound("minecraft:dyed_color").get();
            int color = dyedColor.getInt("rgb").get();

            itemStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color));
        }
    }

    public static Text parseText(String jsonString) {
        if (jsonString.equals("\"\""))
            return Text.empty();

        JsonElement element = JsonParser.parseString(jsonString);

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return Text.literal(element.getAsString());
        }

        JsonObject json = element.getAsJsonObject();
        String baseText = json.has("text") ? json.get("text").getAsString() : "";
        MutableText result = Text.literal(baseText);

        if (json.has("extra") && json.get("extra").isJsonArray()) {
            JsonArray extras = json.getAsJsonArray("extra");
            for (JsonElement extraElement : extras) {
                JsonObject obj = extraElement.getAsJsonObject();
                String extraText = obj.get("text").getAsString();

                Style style = Style.EMPTY;
                if (obj.has("bold")) style = style.withBold(obj.get("bold").getAsBoolean());
                if (obj.has("italic")) style = style.withItalic(obj.get("italic").getAsBoolean());
                if (obj.has("underlined")) style = style.withUnderline(obj.get("underlined").getAsBoolean());
                if (obj.has("strikethrough")) style = style.withStrikethrough(obj.get("strikethrough").getAsBoolean());
                if (obj.has("obfuscated")) style = style.withObfuscated(obj.get("obfuscated").getAsBoolean());
                if (obj.has("color")) {
                    String color = obj.get("color").getAsString();
                    if (color.startsWith("#")) {
                        int rgb = Integer.parseInt(color.substring(1), 16);
                        style = style.withColor(rgb);
                    }
                    else {
                        Formatting formatting = Formatting.byName(color);
                        if (formatting != null) style = style.withColor(formatting);
                    }
                }

                result.append(Text.literal(extraText).setStyle(style));
            }
        }

        return result;
    }

}
