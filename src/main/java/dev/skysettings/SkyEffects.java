package dev.skysettings;

import net.minecraft.world.MoonPhase;
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
		Effects.Sample fx = Effects.sample();

		if (attribute == EnvironmentAttributes.SKY_COLOR_VISUAL) {
			if (value instanceof Integer color) {
				return color(color, c.skyTint, c.skyBrightness, c.saturation, fx);
			}
		} else if (attribute == EnvironmentAttributes.FOG_COLOR_VISUAL) {
			if (value instanceof Integer color) {
				return color(color, c.fogTint, c.skyBrightness, c.saturation, fx);
			}
		} else if (attribute == EnvironmentAttributes.SUNRISE_SUNSET_COLOR_VISUAL) {
			if (value instanceof Integer color) {
				return color(color, c.dawnTint, 1f, c.saturation, fx);
			}
		} else if (attribute == EnvironmentAttributes.MOON_PHASE_VISUAL) {
			if (value instanceof MoonPhase && c.moonPhase > 0) {
				MoonPhase[] phases = MoonPhase.values();
				return phases[(c.moonPhase - 1) % phases.length];
			}
		} else if (attribute == EnvironmentAttributes.STAR_BRIGHTNESS_VISUAL) {
			if (value instanceof Float f) {
				return clamp01(f * c.starBrightness * fx.starMul() * (1f - 0.8f * fx.flash()));
			}
		} else if (attribute == EnvironmentAttributes.CLOUD_COLOR_VISUAL) {
			if (value instanceof Integer color) {
				return scaleAlpha(color(color, c.cloudTint, c.cloudBrightness, c.saturation, fx), c.cloudOpacity);
			}
		} else if (attribute == EnvironmentAttributes.CLOUD_HEIGHT_VISUAL) {
			if (value instanceof Float f) {
				return f + c.cloudHeightOffset + fx.cloudDy();
			}
		} else if (attribute == EnvironmentAttributes.FOG_START_DISTANCE_VISUAL) {
			if (value instanceof Float f) {
				if (c.customFogStart) {
					return c.fogStartBlocks;
				}
				float factor = (c.fogDensity <= 0.01f ? 100f : 1f / c.fogDensity) * fx.fogMul();
				return f * factor;
			}
		} else if (attribute == EnvironmentAttributes.FOG_END_DISTANCE_VISUAL) {
			if (value instanceof Float f) {
				float factor = (c.fogDensity <= 0.01f ? 100f : 1f / c.fogDensity) * fx.fogMul();
				float end = f * factor;
				// Das Ende darf nie vor dem eigenen Beginn liegen
				return c.customFogStart ? Math.max(end, c.fogStartBlocks + 16f) : end;
			}
		} else if (attribute == EnvironmentAttributes.SKY_FOG_END_DISTANCE_VISUAL
				|| attribute == EnvironmentAttributes.CLOUD_FOG_END_DISTANCE_VISUAL) {
			if (value instanceof Float f) {
				// Mehr Dichte = Nebel beginnt/endet naeher. 0 = Nebel (fast) aus.
				float factor = (c.fogDensity <= 0.01f ? 100f : 1f / c.fogDensity) * fx.fogMul();
				return f * factor;
			}
		}
		return value;
	}

	/** Fuer die Menue-Vorschau: wendet Farbmischung, Saettigung, Helligkeit und Effekte auf eine Farbe an. */
	public static int preview(int argb, SkyConfig.Tint t, float brightness, Effects.Sample fx) {
		return color(argb, t, brightness, SkyConfig.get().saturation, fx);
	}

	/** Mischt die eigene Farbe hinein, dann Saettigung, dann Helligkeit. */
	private static int color(int argb, SkyConfig.Tint t, float brightness, float saturation, Effects.Sample fx) {
		double r = (argb >> 16) & 0xFF;
		double g = (argb >> 8) & 0xFF;
		double b = argb & 0xFF;
		float s = t.strength;
		if (s > 0f) {
			float tr = t.r, tg = t.g, tb = t.b;
			if (t.rainbow) {
				double time = System.nanoTime() / 1_000_000_000.0;
				float hue = (float) (((time * 0.05 * SkyConfig.get().rainbowSpeed + t.hueOffset) % 1.0 + 1.0) % 1.0);
				tr = clamp01(Math.abs(hue * 6f - 3f) - 1f);
				tg = clamp01(2f - Math.abs(hue * 6f - 2f));
				tb = clamp01(2f - Math.abs(hue * 6f - 4f));
			}
			r += (tr * 255.0 - r) * s;
			g += (tg * 255.0 - g) * s;
			b += (tb * 255.0 - b) * s;
		}
		double lum = 0.299 * r + 0.587 * g + 0.114 * b;
		r = lum + (r - lum) * saturation;
		g = lum + (g - lum) * saturation;
		b = lum + (b - lum) * saturation;
		r += fx.dr() * 255.0;
		g += fx.dg() * 255.0;
		b += fx.db() * 255.0;
		brightness *= fx.bright();
		r *= brightness;
		g *= brightness;
		b *= brightness;
		float fl = fx.flash();
		if (fl > 0f) {
			r += (217.0 - r) * fl;
			g += (229.0 - g) * fl;
			b += (255.0 - b) * fl;
		}
		int a = argb & 0xFF000000;
		brightness = 1f;
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
