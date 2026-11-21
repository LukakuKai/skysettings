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

	/** Eigene Farbe, die in den normalen Wert hineingemischt wird. */
	public static class Tint {
		/** 0 = Vanilla, 1 = komplett eigene Farbe */
		public float strength = 0f;
		public float r = 1f, g = 1f, b = 1f;

		void clamp() {
			strength = c01(strength);
			r = c01(r);
			g = c01(g);
			b = c01(b);
		}
	}

	public boolean enabled = true;
	/** 0 - 2 (1 = Vanilla), gilt fuer Himmel- und Nebelfarbe */
	public float skyBrightness = 1.0f;
	/** 0 - 2 (1 = Vanilla), gilt fuer Himmel-, Wolken- und Nebelfarbe */
	public float saturation = 1.0f;
	/** 0 - 1 (1 = Vanilla) */
	public float starBrightness = 1.0f;
	/** Eigene Uhrzeit (nur Oberwelt, nur bei dir sichtbar) */
	public boolean customTime = false;
	/** 0 - 24 Uhr */
	public float clockHours = 12f;
	/** 0 - 1 (1 = Vanilla) */
	public float cloudOpacity = 1.0f;
	/** 0 - 2 (1 = Vanilla) */
	public float cloudBrightness = 1.0f;
	/** -64 - 128 Bloecke (0 = Vanilla) */
	public float cloudHeightOffset = 0.0f;
	/** 0 - 3 (1 = Vanilla, 0 = kein Nebel) */
	public float fogDensity = 1.0f;
	public Tint skyTint = new Tint();
	public Tint cloudTint = new Tint();
	public Tint fogTint = new Tint();

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
		saturation = d.saturation;
		starBrightness = d.starBrightness;
		customTime = d.customTime;
		clockHours = d.clockHours;
		cloudOpacity = d.cloudOpacity;
		cloudBrightness = d.cloudBrightness;
		cloudHeightOffset = d.cloudHeightOffset;
		fogDensity = d.fogDensity;
		skyTint = d.skyTint;
		cloudTint = d.cloudTint;
		fogTint = d.fogTint;
	}

	/** Minecraft-Tageszeit in Ticks: 0 = 6 Uhr, 6000 = 12 Uhr, 12000 = 18 Uhr, 18000 = 0 Uhr. */
	public long clockTicks() {
		float t = (((clockHours - 6f) % 24f) + 24f) % 24f * 1000f;
		return (long) t;
	}

	private void clamp() {
		skyBrightness = Math.max(0f, Math.min(2f, skyBrightness));
		saturation = Math.max(0f, Math.min(2f, saturation));
		starBrightness = c01(starBrightness);
		clockHours = Math.max(0f, Math.min(24f, clockHours));
		cloudOpacity = c01(cloudOpacity);
		cloudBrightness = Math.max(0f, Math.min(2f, cloudBrightness));
		cloudHeightOffset = Math.max(-64f, Math.min(128f, cloudHeightOffset));
		fogDensity = Math.max(0f, Math.min(3f, fogDensity));
		if (skyTint == null) skyTint = new Tint();
		if (cloudTint == null) cloudTint = new Tint();
		if (fogTint == null) fogTint = new Tint();
		skyTint.clamp();
		cloudTint.clamp();
		fogTint.clamp();
	}

	private static float c01(float v) {
		return Math.max(0f, Math.min(1f, v));
	}
}
