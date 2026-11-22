package dev.skysettings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

/** Animierte Himmelseffekte (Blitze, Wellen, Polarlicht ...). Alles wird aus der Zeit berechnet. */
public final class Effects {
	public static final String[] NAMES = {
			"Keiner", "Gewitter (Blitze)", "Wellen", "Polarlicht", "Neon-Flackern", "Atmen", "Herzschlag",
			"Funkeln", "Feuer", "Hitzeflimmern", "Windböen", "Farbdrift", "Stroboskop"
	};
	public static final int NONE = 0, STORM = 1, WAVES = 2, AURORA = 3, FLICKER = 4, PULSE = 5, HEARTBEAT = 6,
			TWINKLE = 7, FIRE = 8, SHIMMER = 9, GUST = 10, DRIFT = 11, STROBE = 12;

	/** Veraenderungen, die auf Himmel-, Wolken- und Nebelfarbe angewendet werden. */
	public record Sample(float bright, float flash, float dr, float dg, float db, float starMul, float cloudDy, float fogMul) {
		static final Sample NEUTRAL = new Sample(1f, 0f, 0f, 0f, 0f, 1f, 0f, 1f);
	}

	private static final double SLOT = 7.0;
	private static long lastBoltSlot = Long.MIN_VALUE;

	private Effects() {
	}

	private static float rnd(int i, int salt) {
		int n = i * 374761393 + salt * 668265263;
		n = (n ^ (n >>> 13)) * 1274126177;
		n ^= n >>> 16;
		return (n & 0xFFFFFF) / 16777216f;
	}

	private static double time(SkyConfig c) {
		return (System.nanoTime() / 1_000_000_000.0 * c.effectSpeed) % 100000.0;
	}

	private static float sin(double v) {
		return (float) Math.sin(v);
	}

	/** Blitz-Helligkeit (0 - 1) zum Zeitpunkt t. */
	private static double stormFlash(double t) {
		long slot = (long) Math.floor(t / SLOT);
		if (rnd((int) slot, 61) > 0.75f) {
			return 0;
		}
		double dt = t - slot * SLOT - rnd((int) slot, 62) * 4.5;
		if (dt < 0) {
			return 0;
		}
		double f = dt < 1.2 ? Math.exp(-dt * 9.0) : 0;
		double d2 = dt - 0.22;
		if (d2 >= 0 && d2 < 1.2) {
			f = Math.max(f, 0.8 * Math.exp(-d2 * 7.0));
		}
		return f;
	}

	public static Sample sample() {
		SkyConfig c = SkyConfig.get();
		if (!c.enabled || c.skyEffect == NONE || c.effectStrength <= 0.001f) {
			return Sample.NEUTRAL;
		}
		double t = time(c);
		float k = c.effectStrength;
		float bright = 1f, flash = 0f, dr = 0f, dg = 0f, db = 0f, star = 1f, cdy = 0f, fog = 1f;
		switch (c.skyEffect) {
			case STORM -> flash = (float) Math.min(1.0, stormFlash(t) * k);
			case WAVES -> {
				bright = 1f + 0.16f * k * sin(t * 0.9) + 0.05f * k * sin(t * 3.1 + 1.3);
				cdy = 3.5f * k * sin(t * 0.7 + 0.6);
				fog = 1f + 0.12f * k * sin(t * 0.5);
				db = 0.03f * k * sin(t * 0.9 + 1.0);
			}
			case AURORA -> {
				dr = 0.20f * k * (0.5f + 0.5f * sin(t * 0.5 + 2));
				dg = 0.22f * k * (0.5f + 0.5f * sin(t * 0.4));
				db = 0.25f * k * (0.5f + 0.5f * sin(t * 0.3 + 4));
				bright = 1f + 0.1f * k * sin(t * 0.9);
			}
			case FLICKER -> {
				float n = rnd((int) Math.floor(t * 18), 71);
				bright = 1f + k * 0.3f * (n * 2f - 1f);
				if (rnd((int) Math.floor(t * 2.5), 77) > 0.92f) {
					bright *= Math.max(0.1f, 1f - 0.6f * k);
				}
			}
			case PULSE -> bright = 1f + 0.16f * k * sin(t * 0.8);
			case HEARTBEAT -> {
				double m = t % 1.2;
				double b = Math.exp(-Math.pow((m - 0.12) * 14, 2)) + 0.65 * Math.exp(-Math.pow((m - 0.40) * 14, 2));
				bright = 1f + 0.45f * k * (float) b;
				dr = 0.06f * k * (float) b;
			}
			case TWINKLE -> {
				star = Math.max(0f, 1f + k * (0.45f * sin(t * 2.3) + 0.25f * sin(t * 5.7 + 1) + 0.15f * sin(t * 11.1)));
				bright = 1f + 0.04f * k * sin(t * 1.5);
			}
			case FIRE -> {
				bright = 1f + k * (0.18f * sin(t * 5.3) * sin(t * 2.1) + 0.08f * sin(t * 11.7));
				dr = 0.06f * k * (0.5f + 0.5f * sin(t * 3.7));
			}
			case SHIMMER -> {
				bright = 1f + 0.05f * k * sin(t * 9) + 0.03f * k * sin(t * 17);
				fog = 1f + 0.1f * k * sin(t * 1.7);
			}
			case GUST -> {
				fog = 1f + 0.4f * k * sin(t * 0.4) * sin(t * 0.17 + 1);
				bright = 1f - 0.05f * k * Math.abs(sin(t * 0.4));
			}
			case DRIFT -> {
				dr = 0.09f * k * sin(t * 0.35);
				dg = 0.09f * k * sin(t * 0.27 + 2);
				db = 0.09f * k * sin(t * 0.43 + 4);
			}
			case STROBE -> {
				float step = (float) ((t * 2.0) % 1.0) < 0.12f ? 1.6f : 0.85f;
				bright = 1f + (step - 1f) * k;
			}
			default -> {
			}
		}
		return new Sample(Math.max(0f, bright), Math.min(1f, flash), dr, dg, db, star, cdy, Math.max(0.2f, fog));
	}

	/** Wird jeden Client-Tick aufgerufen und laesst beim Gewitter-Effekt echte (nur bei dir sichtbare) Blitze einschlagen. */
	public static void tick(MinecraftClient mc) {
		SkyConfig c = SkyConfig.get();
		if (!c.enabled || c.skyEffect != STORM || !c.lightningBolts || c.effectStrength <= 0.001f
				|| mc.world == null || mc.player == null || mc.world.getRegistryKey() != World.OVERWORLD) {
			return;
		}
		try {
			double t = time(c);
			long slot = (long) Math.floor(t / SLOT);
			if (slot == lastBoltSlot || rnd((int) slot, 61) > 0.75f) {
				return;
			}
			double dt = t - slot * SLOT - rnd((int) slot, 62) * 4.5;
			if (dt < 0 || dt > 0.6) {
				return;
			}
			lastBoltSlot = slot;
			double ang = Math.random() * Math.PI * 2;
			double dist = 25 + Math.random() * 60;
			double x = mc.player.getX() + Math.cos(ang) * dist;
			double z = mc.player.getZ() + Math.sin(ang) * dist;
			int y = mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING, (int) x, (int) z);
			LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
			bolt.refreshPositionAfterTeleport(x, y, z);
			bolt.setCosmetic(true);
			mc.world.addEntity(bolt);
		} catch (Throwable e) {
			SkySettingsClient.LOGGER.warn("Konnte keinen Blitz erzeugen", e);
			lastBoltSlot = Long.MAX_VALUE; // nicht endlos wiederholen
		}
	}
}
