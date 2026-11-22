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
		/** Farbe wechselt automatisch durch alle Regenbogenfarben */
		public boolean rainbow = false;
		/** Versatz im Farbkreis (0 - 1), damit Himmel, Wolken und Nebel verschieden gefaerbt sind */
		public float hueOffset = 0f;

		public Tint() {
		}

		public Tint(float hueOffset) {
			this.hueOffset = hueOffset;
		}

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
			t.rainbow = rainbow;
			t.hueOffset = hueOffset;
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
	public Tint skyTint = new Tint(0f);
	public Tint cloudTint = new Tint(0.2f);
	public Tint fogTint = new Tint(0.1f);
	/** Farbe von Sonnenauf- und -untergang */
	public Tint dawnTint = new Tint(0.3f);
	/** Eigener Nebel-Beginn: Abstand in Bloecken, ab dem der Nebel anfaengt */
	public boolean customFogStart = false;
	/** 0 - 512 Bloecke */
	public float fogStartBlocks = 40f;
	/** Geschwindigkeit des Regenbogens, 0.1 - 10 */
	public float rainbowSpeed = 1.0f;
	/** Einstellung eines einzelnen Himmelseffekts */
	public static class Fx {
		public boolean on = false;
		/** 0 - 2 */
		public float strength = 1.0f;
		/** 0.3 - 3 */
		public float speed = 1.0f;

		Fx copy() {
			Fx f = new Fx();
			f.on = on;
			f.strength = strength;
			f.speed = speed;
			return f;
		}
	}

	/** Schalter fuer alle Effekte zusammen */
	public boolean effectsEnabled = true;
	/** Einstellungen je Effekt, Index = Effekt-Nummer (siehe Effects.NAMES, 0 ist unbenutzt) */
	public Fx[] fx = defaultFx();
	/** Durchschnittlicher Abstand der Blitze in Sekunden (2 - 30) */
	public float boltInterval = 7f;
	/** Maximale Entfernung der echten Blitze in Bloecken (20 - 150) */
	public float boltDistance = 85f;
	/** Name der zuletzt gewaehlten Vorlage (nur zur Anzeige) */
	public String activeLook = "";
	/** Name des zuletzt gewaehlten eigenen Profils (nur zur Anzeige) */
	public String activeProfile = "";
	/** Schalter fuer alle Block-Effekte zusammen */
	public boolean blockFxEnabled = true;
	/** Groesse der Bloecke in Bildschirm-Einheiten (3 - 16) */
	public float blockSize = 6f;
	/** Leistung: Bildrate der Block-Effekte (0 = unbegrenzt, sonst 5 - 120 Bilder/Sekunde) */
	public int fxFps = 30;
	/** Leistung: Detailstufe 0 = hoch, 1 = mittel, 2 = niedrig, 3 = sehr niedrig (groessere, weniger Bloecke) */
	public int fxQuality = 0;
	/** Leistung: Vorschau-Fenster rechts anzeigen */
	public boolean showPreview = true;
	/** Leistung: Block-Effekte auch in der Vorschau anzeigen */
	public boolean previewFx = true;
	/** Leistung: Block-Effekte nur zeigen, wenn man nicht im Menue/Inventar ist */
	public boolean fxOnlyInGame = false;
	/** Einstellungen je Block-Effekt, Index = Nummer (siehe BlockSky.NAMES, 0 ist unbenutzt) */
	public Fx[] bfx = defaultBfx();
	/** Echte (nur bei dir sichtbare) Blitze beim Gewitter-Effekt */
	public boolean lightningBolts = true;
	/** "de" oder "en" */
	public String lang = "de";

	// --- Design des Menues ---
	/** Index in Theme.STYLES */
	public int style = 0;
	public float accentR = 0.42f, accentG = 0.55f, accentB = 0.84f;
	/** 0.3 - 1 */
	public float panelOpacity = 1.0f;
	/** Runde Ecken im Menue (Standard: eckig, passend zum altmodischen Look) */
	public boolean roundCorners = false;
	public boolean animations = true;
	/** 0.5 - 1.5 (1 = normal), Helligkeit des Menues */
	public float menuBrightness = 1.0f;
	/** Index in Theme.WOODS (nur im Stil Holz) */
	public int woodType = 0;
	/** 0.2 - 3 */
	public float animSpeed = 1.0f;

	private static Fx[] defaultFx() {
		Fx[] a = new Fx[Effects.NAMES.length];
		for (int i = 0; i < a.length; i++) {
			a[i] = new Fx();
		}
		return a;
	}

	private static Fx[] defaultBfx() {
		Fx[] a = new Fx[BlockSky.NAMES.length];
		for (int i = 0; i < a.length; i++) {
			a[i] = new Fx();
		}
		return a;
	}

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
		roundCorners = d.roundCorners;
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
		customFogStart = o.customFogStart;
		fogStartBlocks = o.fogStartBlocks;
		rainbowSpeed = o.rainbowSpeed;
		lightningBolts = o.lightningBolts;
		blockSize = o.blockSize;
		bfx = defaultBfx();
		if (o.bfx != null) {
			for (int i = 0; i < bfx.length && i < o.bfx.length; i++) {
				if (o.bfx[i] != null) {
					bfx[i] = o.bfx[i].copy();
				}
			}
		}
		boltInterval = o.boltInterval;
		boltDistance = o.boltDistance;
		fx = defaultFx();
		if (o.fx != null) {
			for (int i = 0; i < fx.length && i < o.fx.length; i++) {
				if (o.fx[i] != null) {
					fx[i] = o.fx[i].copy();
				}
			}
		}
		skyTint = o.skyTint == null ? new Tint() : o.skyTint.copy();
		cloudTint = o.cloudTint == null ? new Tint() : o.cloudTint.copy();
		fogTint = o.fogTint == null ? new Tint() : o.fogTint.copy();
		dawnTint = o.dawnTint == null ? new Tint() : o.dawnTint.copy();
		restartClock();
	}

	/** Kurzer Vergleichsschluessel aller Himmel-Einstellungen (zum Erkennen, ob eine Vorlage noch genau passt). */
	public String skyKey() {
		StringBuilder k = new StringBuilder();
		float[] v = {skyBrightness, saturation, starBrightness, customTime ? 1 : 0, clockHours, timeSpeed, moonPhase,
				cloudOpacity, cloudBrightness, cloudHeightOffset, fogDensity, customFogStart ? 1 : 0, fogStartBlocks,
				rainbowSpeed, boltInterval, boltDistance, lightningBolts ? 1 : 0};
		for (float f : v) {
			k.append(Math.round(f * 100f)).append(',');
		}
		for (Tint t : new Tint[] {skyTint, cloudTint, fogTint, dawnTint}) {
			k.append(Math.round(t.strength * 100f)).append(',').append(Math.round(t.r * 100f)).append(',')
					.append(Math.round(t.g * 100f)).append(',').append(Math.round(t.b * 100f)).append(',')
					.append(t.rainbow ? 1 : 0).append(';');
		}
		for (Fx f : bfx) {
			k.append(f.on ? 1 : 0).append(Math.round(f.strength * 100f)).append(',').append(Math.round(f.speed * 100f)).append(';');
		}
		k.append(Math.round(blockSize * 100f));
		for (Fx f : fx) {
			k.append(f.on ? 1 : 0).append(Math.round(f.strength * 100f)).append(',').append(Math.round(f.speed * 100f)).append(';');
		}
		return k.toString();
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
		fogStartBlocks = Math.max(0f, Math.min(512f, fogStartBlocks));
		rainbowSpeed = Math.max(0.1f, Math.min(10f, rainbowSpeed));
		boltInterval = Math.max(2f, Math.min(30f, boltInterval));
		boltDistance = Math.max(20f, Math.min(150f, boltDistance));
		if (activeLook == null) activeLook = "";
		if (activeProfile == null) activeProfile = "";
		blockSize = Math.max(3f, Math.min(16f, blockSize));
		fxFps = fxFps <= 0 ? 0 : Math.max(5, Math.min(120, fxFps));
		fxQuality = Math.max(0, Math.min(3, fxQuality));
		Fx[] fixedB = defaultBfx();
		if (bfx != null) {
			for (int i = 0; i < fixedB.length && i < bfx.length; i++) {
				if (bfx[i] != null) {
					fixedB[i] = bfx[i];
					fixedB[i].strength = Math.max(0f, Math.min(2f, fixedB[i].strength));
					fixedB[i].speed = Math.max(0.3f, Math.min(3f, fixedB[i].speed));
				}
			}
		}
		bfx = fixedB;
		Fx[] fixed = defaultFx();
		if (fx != null) {
			for (int i = 0; i < fixed.length && i < fx.length; i++) {
				if (fx[i] != null) {
					fixed[i] = fx[i];
					fixed[i].strength = Math.max(0f, Math.min(2f, fixed[i].strength));
					fixed[i].speed = Math.max(0.3f, Math.min(3f, fixed[i].speed));
				}
			}
		}
		fx = fixed;
		if (!"en".equals(lang)) lang = "de";
		if (skyTint == null) skyTint = new Tint(0f);
		if (cloudTint == null) cloudTint = new Tint(0.2f);
		if (fogTint == null) fogTint = new Tint(0.1f);
		if (dawnTint == null) dawnTint = new Tint(0.3f);
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
