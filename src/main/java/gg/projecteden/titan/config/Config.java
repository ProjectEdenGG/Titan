package gg.projecteden.titan.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gg.projecteden.titan.config.annotations.Disabled;
import gg.projecteden.titan.config.annotations.Group;
import gg.projecteden.titan.config.annotations.Name;
import gg.projecteden.titan.config.annotations.OldConfig;
import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.network.serverbound.TitanConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static gg.projecteden.titan.Titan.MOD_ID;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Config {

	public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public final static Path GAME_CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	public final static File CONFIG_FILE = new File(configDir(), MOD_ID + ".json");

	public static File configDir() {
		File mapConfigDir = GAME_CONFIG_DIR.toFile();
		if (!mapConfigDir.exists()) {
			mapConfigDir.mkdirs();
		}
		return mapConfigDir;
	}

	public static JsonObject getJsonObject(File jsonFile) {
		if (jsonFile.exists()) {
			JsonObject jsonObject = loadJson(jsonFile).getAsJsonObject();
			if (jsonObject == null) {
				return new JsonObject();
			}
			return jsonObject;
		}

		save();

		return getJsonObject(CONFIG_FILE);
	}

	public static JsonObject loadJson(File jsonFile) {
		if (jsonFile.exists()) {
			try (Reader reader = new FileReader(jsonFile)) {
				return loadJson(reader);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return new JsonObject();
	}

	public static JsonObject loadJson(Reader reader) {
		return GSON.fromJson(reader, JsonObject.class);
	}

	public static void storeJson(File jsonFile, JsonElement jsonObject) {
		try (FileWriter writer = new FileWriter(jsonFile)) {
			String json = GSON.toJson(jsonObject);
			writer.write(json);
			writer.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void save() {
		JsonObject jsonObject = new JsonObject();

		for (Field field : ConfigItem.getAllNonDeveloperOptions()) {
			try {
				if (field.isAnnotationPresent(Disabled.class))
					continue;

				ConfigItem item = (ConfigItem) field.get(null);
				String group = field.getAnnotation(Group.class).value();
				String name = field.getAnnotation(Name.class).config().isEmpty() ? field.getName() : field.getAnnotation(Name.class).config();

				name = name.toLowerCase().replace("_", "-").replace(" ", "-");

				if (!jsonObject.has(group))
					jsonObject.add(group, new JsonObject());

				JsonObject jsonGroup = jsonObject.getAsJsonObject(group);

				switch (item.getType()) {
                    case BOOLEAN -> jsonGroup.addProperty(name, (Boolean) item.getValue());
                    case ENUM -> jsonGroup.addProperty(name, ((Enum) item.getValue()).name().toLowerCase());
                    case INTEGER -> jsonGroup.addProperty(name, (Integer) item.getValue());
                    case DOUBLE -> jsonGroup.addProperty(name, (Double) item.getValue());
                    case STRING -> jsonGroup.addProperty(name, (String) item.getValue());
                }

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		storeJson(CONFIG_FILE, jsonObject);

		ServerClientMessaging.send(new TitanConfig());
	}

	public static void load() {
		JsonObject json = getJsonObject(CONFIG_FILE);
		for (Field field : ConfigItem.getAllNonDeveloperOptions()) {
			try {
				if (field.isAnnotationPresent(Disabled.class))
					continue;

				ConfigItem item = (ConfigItem) field.get(null);

				if (field.isAnnotationPresent(OldConfig.class)) {
					String group = field.getAnnotation(OldConfig.class).group();
					String name = field.getAnnotation(OldConfig.class).value();

					if (json.has(group)) {
						JsonObject jsonObject = json.getAsJsonObject(group);
						if (jsonObject.has(name)) {
							load(item, jsonObject, name);
							continue;
						}
					}
					else {
						if (json.has(name)) {
							load(item, json, name);
							continue;
						}
					}
				}

				String group = field.getAnnotation(Group.class).value();
				String name = field.getAnnotation(Name.class).config().isEmpty() ? field.getName() : field.getAnnotation(Name.class).config();
				name = name.toLowerCase().replace("_", "-").replace(" ", "-");
				if (json.has(group))
					load(item, json.getAsJsonObject(group), name);
				else
					load(item, json, name);


			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private static void load(ConfigItem item, JsonObject json, String path) {
		path = path.toLowerCase().replace("_", "-");

		if (!json.has(path))
			return;

		Object val = switch (item.getType()) {
            case BOOLEAN -> JsonHelper.getBoolean(json, path);
            case ENUM -> Enum.valueOf((Class) item.getValue().getClass(), JsonHelper.getString(json, path).toUpperCase());
            case INTEGER -> JsonHelper.getInt(json, path);
            case DOUBLE -> JsonHelper.getDouble(json, path);
            case STRING -> JsonHelper.getString(json, path);
            case UNKNOWN -> null;
        };

		item.setValue(val);
	}

}
