package dev.skysettings;

import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;

/**
 * Verändert die visuellen Environment-Attribute (Himmel, Wolken, Nebel),
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

	/** Beim Verlassen aufrufen. Gibt true zurück, wenn es der äußerste Aufruf war. */
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

		if (attribute == EnvironmentAttributes.SKY_COLOR_VISUAL || attribute == EnvironmentAttributes.FOG_COLOR_VISUAL) {
			if (value instanceof Integer color) {
				return scaleRgb(color, c.skyBrightness);
			}
		} else if (attribute == EnvironmentAttributes.STAR_BRIGHTNESS_VISUAL) {
			if (value instanceof Float f) {
				return clamp01(f * c.starBrightness);
			}
		} else if (attribute == EnvironmentAttributes.CLOUD_COLOR_VISUAL) {
			if (value instanceof Integer color) {
				return scaleAlpha(scaleRgb(color, c.cloudBrightness), c.cloudOpacity);
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
				// Mehr Dichte = Nebel beginnt/endet näher. 0 = Nebel (fast) aus.
				float factor = c.fogDensity <= 0.01f ? 100f : 1f / c.fogDensity;
				return f * factor;
			}
		}
		return value;
	}

	private static float clamp01(float v) {
		return Math.max(0f, Math.min(1f, v));
	}

	private static int scaleRgb(int argb, float factor) {
		int a = argb & 0xFF000000;
		int r = clampByte(Math.round(((argb >> 16) & 0xFF) * factor));
		int g = clampByte(Math.round(((argb >> 8) & 0xFF) * factor));
		int b = clampByte(Math.round((argb & 0xFF) * factor));
		return a | (r << 16) | (g << 8) | b;
	}

	private static int scaleAlpha(int argb, float factor) {
		int a = clampByte(Math.round(((argb >>> 24) & 0xFF) * factor));
		return (a << 24) | (argb & 0x00FFFFFF);
	}

	private static int clampByte(int v) {
		return Math.max(0, Math.min(255, v));
	}
}
