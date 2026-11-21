package dev.skysettings;

import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;

/**
 * Veraendert die visuellen Environment-Attribute (Himmel, Wolken, Nebel),
 * die der Renderer in Minecraft 1.21.11 abfragt.
 */
public final class SkyEffects {
	private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);

	private SkyEffects() {
	}

	/** Beim Betreten von getAttributeValue aufrufen. */
	public static void enter() {
		DEPTH.get()[0]++;
	}

	/** Beim Verlassen aufrufen. Gibt true zurueck, wenn es der aeusserste Aufruf war. */
	public static boolean exit() {
		int[] depth = DEPTH.get();
		depth[0] = Math.max(0, depth[0] - 1);
		return depth[0] == 0;
	}

	public static Object apply(EnvironmentAttribute<?> attribute, Object value) {
		SkyConfig c = SkyConfig.get();
		if (!c.enabled || value == null) {
			return value;
		}

		if (attribute == EnvironmentAttributes.SKY_COLOR_VISUAL) {
			if (value instanceof Integer color) {
				return color(color, c.skyTint, c.skyBrightness, c.saturation);
			}
		} else if (attribute == EnvironmentAttributes.FOG_COLOR_VISUAL) {
			if (value instanceof Integer color) {
				return color(color, c.fogTint, c.skyBrightness, c.saturation);
			}
		} else if (attribute == EnvironmentAttributes.STAR_BRIGHTNESS_VISUAL) {
			if (value instanceof Float f) {
				return clamp01(f * c.starBrightness);
			}
		} else if (attribute == EnvironmentAttributes.CLOUD_COLOR_VISUAL) {
			if (value instanceof Integer color) {
				return scaleAlpha(color(color, c.cloudTint, c.cloudBrightness, c.saturation), c.cloudOpacity);
			}
		} else if (attribute == EnvironmentAttributes.CLOUD_HEIGHT_VISUAL) {
			if (value instanceof Float f) {
				return f + c.cloudHeightOffset;
			}
		} else if (attribute == EnvironmentAttributes.FOG_START_DISTANCE_VISUAL
				|| attribute == EnvironmentAttributes.FOG_END_DISTANCE_VISUAL
				|| attribute == EnvironmentAttributes.SKY_FOG_END_DISTANCE_VISUAL
				|| attribute == EnvironmentAttributes.CLOUD_FOG_END_DISTANCE_VISUAL) {
			if (value instanceof Float f) {
				// Mehr Dichte = Nebel beginnt/endet naeher. 0 = Nebel (fast) aus.
				float factor = c.fogDensity <= 0.01f ? 100f : 1f / c.fogDensity;
				return f * factor;
			}
		}
		return value;
	}

	/** Mischt die eigene Farbe hinein, dann Saettigung, dann Helligkeit. */
	private static int color(int argb, SkyConfig.Tint t, float brightness, float saturation) {
		double r = (argb >> 16) & 0xFF;
		double g = (argb >> 8) & 0xFF;
		double b = argb & 0xFF;
		float s = t.strength;
		if (s > 0f) {
			r += (t.r * 255.0 - r) * s;
			g += (t.g * 255.0 - g) * s;
			b += (t.b * 255.0 - b) * s;
		}
		double lum = 0.299 * r + 0.587 * g + 0.114 * b;
		r = lum + (r - lum) * saturation;
		g = lum + (g - lum) * saturation;
		b = lum + (b - lum) * saturation;
		int a = argb & 0xFF000000;
		return a | (clampByte(r * brightness) << 16) | (clampByte(g * brightness) << 8) | clampByte(b * brightness);
	}

	private static float clamp01(float v) {
		return Math.max(0f, Math.min(1f, v));
	}

	private static int scaleAlpha(int argb, float factor) {
		int a = clampByte(((argb >>> 24) & 0xFF) * factor);
		return (a << 24) | (argb & 0x00FFFFFF);
	}

	private static int clampByte(double v) {
		return (int) Math.max(0, Math.min(255, Math.round(v)));
	}
}
