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
	static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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

		Tint copy() {
			Tint t = new Tint();
			t.strength = strength;
			t.r = r;
			t.g = g;
			t.b = b;
			return t;
		}
	}

	// --- Himmel ---
	public boolean enabled = true;
	/** 0 - 2 (1 = Vanilla), gilt fuer Himmel- und Nebelfarbe */
	public float skyBrightness = 1.0f;
	/** 0 - 2 (1 = Vanilla), gilt fuer Himmel-, Wolken- und Nebelfarbe */
	public float saturation = 1.0f;
	/** 0 - 1 (1 = Vanilla) */
	public float starBrightness = 1.0f;
	/** Eigene Uhrzeit (nur Oberwelt, nur bei dir sichtbar) */
	public boolean customTime = false;
	/** 0 - 24 Uhr (Startzeit) */
	public float clockHours = 12f;
	/** 0 = Uhrzeit steht still, 1 = normale Spielgeschwindigkeit */
	public float timeSpeed = 0f;
	/** 0 = automatisch, 1 - 8 = feste Mondphase */
	public int moonPhase = 0;
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
	/** Farbe von Sonnenauf- und -untergang */
	public Tint dawnTint = new Tint();

	// --- Design des Menues ---
	/** Index in Theme.STYLES */
	public int style = 0;
	public float accentR = 0.42f, accentG = 0.55f, accentB = 0.84f;
	/** 0.3 - 1 */
	public float panelOpacity = 1.0f;
	public boolean rounded = true;
	public boolean animations = true;
	/** 0.5 - 1.5 (1 = normal), Helligkeit des Menues */
	public float menuBrightness = 1.0f;
	/** Index in Theme.WOODS (nur im Stil Holz) */
	public int woodType = 0;
	/** 0.2 - 3 */
	public float animSpeed = 1.0f;

	private transient long anchorNanos = System.nanoTime();

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

	public void resetTheme() {
		SkyConfig d = new SkyConfig();
		style = d.style;
		accentR = d.accentR;
		accentG = d.accentG;
		accentB = d.accentB;
		panelOpacity = d.panelOpacity;
		rounded = d.rounded;
		animations = d.animations;
		menuBrightness = d.menuBrightness;
		woodType = d.woodType;
		animSpeed = d.animSpeed;
	}

	/** Setzt nur die Himmel-Einstellungen zurueck, nicht das Menue-Design. */
	public void reset() {
		applySky(new SkyConfig());
	}

	/** Uebernimmt alle Himmel-Einstellungen (nicht das Menue-Design) aus einer anderen Config. */
	public void applySky(SkyConfig o) {
		skyBrightness = o.skyBrightness;
		saturation = o.saturation;
		starBrightness = o.starBrightness;
		customTime = o.customTime;
		clockHours = o.clockHours;
		timeSpeed = o.timeSpeed;
		moonPhase = o.moonPhase;
		cloudOpacity = o.cloudOpacity;
		cloudBrightness = o.cloudBrightness;
		cloudHeightOffset = o.cloudHeightOffset;
		fogDensity = o.fogDensity;
		skyTint = o.skyTint == null ? new Tint() : o.skyTint.copy();
		cloudTint = o.cloudTint == null ? new Tint() : o.cloudTint.copy();
		fogTint = o.fogTint == null ? new Tint() : o.fogTint.copy();
		dawnTint = o.dawnTint == null ? new Tint() : o.dawnTint.copy();
		restartClock();
	}

	/** Startet die laufende Uhr neu (nach Aenderung von Uhrzeit oder Tempo). */
	public void restartClock() {
		anchorNanos = System.nanoTime();
	}

	/** Minecraft-Tageszeit in Ticks: 0 = 6 Uhr, 6000 = 12 Uhr, 12000 = 18 Uhr, 18000 = 0 Uhr. */
	public long clockTicks() {
		double t = (clockHours - 6.0) * 1000.0;
		if (timeSpeed > 0.001f) {
			t += (System.nanoTime() - anchorNanos) / 50_000_000.0 * timeSpeed;
		}
		t = ((t % 24000.0) + 24000.0) % 24000.0;
		return (long) t;
	}

	private void clamp() {
		skyBrightness = Math.max(0f, Math.min(2f, skyBrightness));
		saturation = Math.max(0f, Math.min(2f, saturation));
		starBrightness = c01(starBrightness);
		clockHours = Math.max(0f, Math.min(24f, clockHours));
		timeSpeed = Math.max(0f, Math.min(60f, timeSpeed));
		moonPhase = Math.max(0, Math.min(8, moonPhase));
		cloudOpacity = c01(cloudOpacity);
		cloudBrightness = Math.max(0f, Math.min(2f, cloudBrightness));
		cloudHeightOffset = Math.max(-64f, Math.min(128f, cloudHeightOffset));
		fogDensity = Math.max(0f, Math.min(3f, fogDensity));
		if (skyTint == null) skyTint = new Tint();
		if (cloudTint == null) cloudTint = new Tint();
		if (fogTint == null) fogTint = new Tint();
		if (dawnTint == null) dawnTint = new Tint();
		skyTint.clamp();
		cloudTint.clamp();
		fogTint.clamp();
		dawnTint.clamp();
		style = Math.max(0, Math.min(Theme.STYLES.length - 1, style));
		accentR = c01(accentR);
		accentG = c01(accentG);
		accentB = c01(accentB);
		panelOpacity = Math.max(0.3f, Math.min(1f, panelOpacity));
		animSpeed = Math.max(0.2f, Math.min(3f, animSpeed));
		menuBrightness = Math.max(0.5f, Math.min(1.5f, menuBrightness));
		woodType = Math.max(0, Math.min(Theme.WOODS.length - 1, woodType));
	}

	private static float c01(float v) {
		return Math.max(0f, Math.min(1f, v));
	}
}
