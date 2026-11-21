package dev.skysettings;

import java.util.List;
import java.util.function.Consumer;

/** Fertige Himmel-Vorlagen. */
public final class Looks {
	public record Look(String name, Consumer<SkyConfig> build) {
	}

	public static final List<Look> ALL = List.of(
			new Look("Vanilla", c -> {
			}),
			new Look("Goldene Stunde", c -> {
				time(c, 17.3f);
				c.saturation = 1.2f;
				tint(c.skyTint, .35f, 0xFFB04A);
				tint(c.cloudTint, .5f, 0xFFC070);
				tint(c.fogTint, .45f, 0xFFB060);
				tint(c.dawnTint, .7f, 0xFF8A30);
			}),
			new Look("Sonnenuntergang", c -> {
				time(c, 18.0f);
				c.saturation = 1.25f;
				tint(c.skyTint, .55f, 0xFF5A30);
				tint(c.cloudTint, .6f, 0xFF7A5A);
				tint(c.fogTint, .55f, 0xFF6A40);
				tint(c.dawnTint, .9f, 0xFF4020);
			}),
			new Look("Vollmondnacht", c -> {
				time(c, 0.5f);
				c.moonPhase = 1;
				c.skyBrightness = 1.6f;
				tint(c.skyTint, .5f, 0x1A2860);
				tint(c.cloudTint, .4f, 0x6070B0);
				tint(c.fogTint, .5f, 0x1A2A60);
			}),
			new Look("Mitternachtsblau", c -> {
				time(c, 1.0f);
				c.skyBrightness = 1.4f;
				tint(c.skyTint, .8f, 0x0A1A60);
				tint(c.cloudTint, .5f, 0x203070);
				tint(c.fogTint, .7f, 0x0A1A55);
			}),
			new Look("Blutmond", c -> {
				time(c, 0.0f);
				c.moonPhase = 1;
				c.starBrightness = .6f;
				c.skyBrightness = 1.5f;
				tint(c.skyTint, .8f, 0x6A0000);
				tint(c.cloudTint, .6f, 0x802020);
				tint(c.fogTint, .7f, 0x500000);
			}),
			new Look("Alien-Lila", c -> {
				time(c, 12.0f);
				c.saturation = 1.3f;
				tint(c.skyTint, .75f, 0x9A30E0);
				tint(c.cloudTint, .6f, 0xFF80FF);
				tint(c.fogTint, .65f, 0x7A30C0);
			}),
			new Look("Rosa Traum", c -> {
				time(c, 15.0f);
				tint(c.skyTint, .6f, 0xFF9ACC);
				tint(c.cloudTint, .7f, 0xFFD0E8);
				tint(c.fogTint, .6f, 0xFFB0D8);
				tint(c.dawnTint, .6f, 0xFF80C0);
			}),
			new Look("Nebelmorgen", c -> {
				time(c, 7.0f);
				c.fogDensity = 2.5f;
				c.saturation = .5f;
				tint(c.skyTint, .5f, 0xCCD6E0);
				tint(c.cloudTint, .5f, 0xE0E4EA);
				tint(c.fogTint, .75f, 0xD8DEE6);
			}),
			new Look("Eiswelt", c -> {
				time(c, 12.0f);
				c.saturation = .7f;
				c.skyBrightness = 1.15f;
				tint(c.skyTint, .6f, 0x9AD8FF);
				tint(c.cloudTint, .5f, 0xEAF7FF);
				tint(c.fogTint, .7f, 0xCCEBFF);
			}),
			new Look("Giftgrün", c -> {
				time(c, 12.0f);
				c.saturation = 1.2f;
				tint(c.skyTint, .7f, 0x40E040);
				tint(c.cloudTint, .6f, 0xA0FF70);
				tint(c.fogTint, .6f, 0x50C040);
			}),
			new Look("Wüstenglut", c -> {
				time(c, 13.0f);
				c.saturation = 1.2f;
				c.skyBrightness = 1.1f;
				c.fogDensity = .8f;
				tint(c.skyTint, .5f, 0xFFC070);
				tint(c.cloudTint, .5f, 0xFFE0B0);
				tint(c.fogTint, .65f, 0xF0B060);
			}),
			new Look("Tiefsee", c -> {
				time(c, 11.0f);
				c.skyBrightness = .9f;
				c.fogDensity = 1.8f;
				tint(c.skyTint, .7f, 0x0A5A80);
				tint(c.cloudTint, .5f, 0x70A8C0);
				tint(c.fogTint, .8f, 0x084868);
			}),
			new Look("Schwarz-Weiß", c -> c.saturation = 0f)
	);

	private Looks() {
	}

	public static void apply(Look look) {
		SkyConfig tmp = new SkyConfig();
		look.build().accept(tmp);
		SkyConfig.get().applySky(tmp);
	}

	private static void time(SkyConfig c, float hours) {
		c.customTime = true;
		c.clockHours = hours;
		c.timeSpeed = 0f;
	}

	private static void tint(SkyConfig.Tint t, float strength, int rgb) {
		t.strength = strength;
		t.r = ((rgb >> 16) & 0xFF) / 255f;
		t.g = ((rgb >> 8) & 0xFF) / 255f;
		t.b = (rgb & 0xFF) / 255f;
	}
}
