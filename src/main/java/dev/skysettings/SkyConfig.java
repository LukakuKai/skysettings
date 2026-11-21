package dev.skysettings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Speichert alle Einstellungen in config/skysettings.json. */
public final class SkyConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static SkyConfig instance = new SkyConfig();

	public boolean enabled = true;
	/** 0.0 - 2.0 (1.0 = Vanilla) */
	public float skyBrightness = 1.0f;
	/** 0.0 - 1.0 (1.0 = Vanilla) */
	public float starBrightness = 1.0f;
	/** 0.0 - 1.0 (1.0 = Vanilla) */
	public float cloudOpacity = 1.0f;
	/** 0.0 - 2.0 (1.0 = Vanilla) */
	public float cloudBrightness = 1.0f;
	/** -64 - 128 Blöcke (0 = Vanilla) */
	public float cloudHeightOffset = 0.0f;
	/** 0.0 - 3.0 (1.0 = Vanilla, 0 = kein Nebel) */
	public float fogDensity = 1.0f;

	public static SkyConfig get() {
		return instance;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("skysettings.json");
	}

	public static void load() {
		Path file = file();
		if (!Files.exists(file)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(file)) {
			SkyConfig loaded = GSON.fromJson(reader, SkyConfig.class);
			if (loaded != null) {
				loaded.clamp();
				instance = loaded;
			}
		} catch (Exception e) {
			SkySettingsClient.LOGGER.warn("Konnte skysettings.json nicht lesen, nutze Standardwerte", e);
		}
	}

	public static void save() {
		try (Writer writer = Files.newBufferedWriter(file())) {
			GSON.toJson(instance, writer);
		} catch (IOException e) {
			SkySettingsClient.LOGGER.warn("Konnte skysettings.json nicht speichern", e);
		}
	}

	public void reset() {
		SkyConfig d = new SkyConfig();
		enabled = d.enabled;
		skyBrightness = d.skyBrightness;
		starBrightness = d.starBrightness;
		cloudOpacity = d.cloudOpacity;
		cloudBrightness = d.cloudBrightness;
		cloudHeightOffset = d.cloudHeightOffset;
		fogDensity = d.fogDensity;
	}

	private void clamp() {
		skyBrightness = Math.max(0f, Math.min(2f, skyBrightness));
		starBrightness = Math.max(0f, Math.min(1f, starBrightness));
		cloudOpacity = Math.max(0f, Math.min(1f, cloudOpacity));
		cloudBrightness = Math.max(0f, Math.min(2f, cloudBrightness));
		cloudHeightOffset = Math.max(-64f, Math.min(128f, cloudHeightOffset));
		fogDensity = Math.max(0f, Math.min(3f, fogDensity));
	}
}
