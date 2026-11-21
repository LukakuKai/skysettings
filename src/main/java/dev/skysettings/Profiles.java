package dev.skysettings;

import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Eigene gespeicherte Himmel-Profile (config/skysettings_profiles.json). */
public final class Profiles {
	private static final Type TYPE = new TypeToken<LinkedHashMap<String, SkyConfig>>() {
	}.getType();
	private static Map<String, SkyConfig> map = new LinkedHashMap<>();

	private Profiles() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("skysettings_profiles.json");
	}

	public static void load() {
		Path file = file();
		if (!Files.exists(file)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(file)) {
			Map<String, SkyConfig> loaded = SkyConfig.GSON.fromJson(reader, TYPE);
			if (loaded != null) {
				map = loaded;
			}
		} catch (Exception e) {
			SkySettingsClient.LOGGER.warn("Konnte Profile nicht lesen", e);
		}
	}

	private static void save() {
		try (Writer writer = Files.newBufferedWriter(file())) {
			SkyConfig.GSON.toJson(map, TYPE, writer);
		} catch (Exception e) {
			SkySettingsClient.LOGGER.warn("Konnte Profile nicht speichern", e);
		}
	}

	public static List<String> names() {
		return new ArrayList<>(map.keySet());
	}

	/** Speichert die aktuellen Himmel-Einstellungen unter diesem Namen (ueberschreibt gleichnamige). */
	public static void put(String name) {
		SkyConfig snapshot = SkyConfig.GSON.fromJson(SkyConfig.GSON.toJson(SkyConfig.get()), SkyConfig.class);
		map.put(name, snapshot);
		save();
	}

	public static void apply(String name) {
		SkyConfig p = map.get(name);
		if (p != null) {
			SkyConfig.get().applySky(p);
		}
	}

	public static void delete(String name) {
		map.remove(name);
		save();
	}
}
