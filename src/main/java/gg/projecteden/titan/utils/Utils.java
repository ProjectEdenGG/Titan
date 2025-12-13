package gg.projecteden.titan.utils;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import gg.projecteden.titan.Titan;
import joptsimple.internal.Strings;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class Utils {

	private static final SimpleDateFormat ISOFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	@SneakyThrows
	public static String bash(String command, File directory) {
		InputStream result = Runtime.getRuntime().exec(command, null, directory).getInputStream();
		StringBuilder builder = new StringBuilder();
		new Scanner(result).forEachRemaining(string -> builder.append(string).append(" "));
		return builder.toString().trim();
	}

	public static boolean isOnEden() {
		final ClientPacketListener handler = Minecraft.getInstance().getConnection();
		if (handler == null)
			return false;

		String address = handler.getConnection().getRemoteAddress().toString();
		if (address == null)
			return false;
        address = address.toLowerCase();

		return address.contains("projecteden.gg") || address.contains("148.113.216.194");
	}

	public static String camelCase(String text) {
		if (Strings.isNullOrEmpty(text))
			return text;

		return Arrays.stream(text.replaceAll("_", " ").split(" "))
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}

	public static <T> T getGitResponse(String get, Class<T> type) {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet request = new HttpGet("https://api.github.com/repos/ProjectEdenGG/" + get);
			request.addHeader("Accept", "application/vnd.github+json");
			request.addHeader("User-Agent", "Googlebot/2.1 (+http://www.google.com/bot.html)");
			CloseableHttpResponse response = client.execute(request);
			return new Gson().fromJson(EntityUtils.toString(response.getEntity()), type);
		} catch (Throwable ex) {
			Titan.log("An error occurred while checking git versioning. Rate limit reached?");
			ex.printStackTrace();
		}
		return null;
	}

	public static String getManifestAttribute(String attribute) {
		try {
			URL jarURL = Utils.class.getResource("/gg/projecteden/titan/Titan.class");
            if (jarURL == null)
				return "";
            JarURLConnection jurlConn = (JarURLConnection) jarURL.openConnection();
			Manifest manifest = jurlConn.getManifest();
			return manifest.getMainAttributes().getValue(attribute);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String ISODate(Date date) {
		return ISOFormatter.format(date);
	}

	public static Date ISODate(String date) {
		try { return ISOFormatter.parse(date); }
		catch (ParseException e) { e.printStackTrace(); }
		return null;
	}


	public static NonNullList<ItemStack> getStoredItems(RegistryAccess registryManager, ItemStack stack) {
		if (!stack.has(DataComponents.CUSTOM_DATA))
			return NonNullList.create();

		CompoundTag nbt = stack.get(DataComponents.CUSTOM_DATA).copyTag();

		if (nbt != null && nbt.contains("ProjectEden")) {
			CompoundTag projectEden = nbt.getCompound("ProjectEden").get();

			if (projectEden.contains("Items")) {
				NonNullList<ItemStack> items = NonNullList.create();
				Map<Integer, CompoundTag> slotMap = new HashMap<>();
				ListTag tagList = projectEden.getList("Items").get();
				final int count = tagList.size();

				for (int i = 0; i < count; i++) {
					int slot = tagList.getCompound(i).get().getByte("Slot").get();
					slotMap.put(slot, tagList.getCompound(i).get());
				}

				int maxSlots = slotMap.keySet().stream().max(Integer::compareTo).orElse(0);

				for (int i = 0; i <= maxSlots; i++)
					if (!slotMap.containsKey(i))
						items.add(ItemStack.EMPTY);
					else {
                        try {
                            ItemStack stack2 = ItemStack.CODEC.parse(registryManager.createSerializationContext(NbtOps.INSTANCE), slotMap.get(i)).getOrThrow();
                            items.add(stack2);
                        } catch (Throwable ex) {
                            Titan.log("Failed to load item: " + slotMap.get(i).toString());
                            ex.printStackTrace();
                        }
                    }

				return items;
			}
		}

		return NonNullList.create();
	}

	public static boolean isControlPressed() {
		var client = Minecraft.getInstance();
		if (client == null) return false;
		var window = client.getWindow();
		if (window == null) return false;
		return isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL);
	}

	public static boolean isShiftPressed() {
		var client = Minecraft.getInstance();
		if (client == null) return false;
		var window = client.getWindow();
		if (window == null) return false;
		return isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	public static boolean isKeyPressed(Window window, int... keys) {
		for (int key : keys)
			if (InputConstants.isKeyDown(window, key))
				return true;
		return false;
	}

}
