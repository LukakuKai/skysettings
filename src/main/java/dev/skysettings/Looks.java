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
				effect(c, 5, 0.4f);
				time(c, 17.3f);
				c.saturation = 1.2f;
				tint(c.skyTint, .35f, 0xFFB04A);
				tint(c.cloudTint, .5f, 0xFFC070);
				tint(c.fogTint, .45f, 0xFFB060);
				tint(c.dawnTint, .7f, 0xFF8A30);
			}),
			new Look("Sonnenuntergang", c -> {
				effect(c, 11, 0.5f);
				time(c, 18.0f);
				c.saturation = 1.25f;
				tint(c.skyTint, .55f, 0xFF5A30);
				tint(c.cloudTint, .6f, 0xFF7A5A);
				tint(c.fogTint, .55f, 0xFF6A40);
				tint(c.dawnTint, .9f, 0xFF4020);
			}),
			new Look("Vollmondnacht", c -> {
				effect(c, 7, 0.8f);
				time(c, 0.5f);
				c.moonPhase = 1;
				c.skyBrightness = 1.6f;
				tint(c.skyTint, .5f, 0x1A2860);
				tint(c.cloudTint, .4f, 0x6070B0);
				tint(c.fogTint, .5f, 0x1A2A60);
			}),
			new Look("Mitternachtsblau", c -> {
				effect(c, 5, 0.5f);
				time(c, 1.0f);
				c.skyBrightness = 1.4f;
				tint(c.skyTint, .8f, 0x0A1A60);
				tint(c.cloudTint, .5f, 0x203070);
				tint(c.fogTint, .7f, 0x0A1A55);
			}),
			new Look("Blutmond", c -> {
				effect(c, 6, 1.0f);
				time(c, 0.0f);
				c.moonPhase = 1;
				c.starBrightness = .6f;
				c.skyBrightness = 1.5f;
				tint(c.skyTint, .8f, 0x6A0000);
				tint(c.cloudTint, .6f, 0x802020);
				tint(c.fogTint, .7f, 0x500000);
			}),
			new Look("Alien-Lila", c -> {
				effect(c, 11, 1.0f);
				time(c, 12.0f);
				c.saturation = 1.3f;
				tint(c.skyTint, .75f, 0x9A30E0);
				tint(c.cloudTint, .6f, 0xFF80FF);
				tint(c.fogTint, .65f, 0x7A30C0);
			}),
			new Look("Rosa Traum", c -> {
				effect(c, 5, 0.6f);
				time(c, 15.0f);
				tint(c.skyTint, .6f, 0xFF9ACC);
				tint(c.cloudTint, .7f, 0xFFD0E8);
				tint(c.fogTint, .6f, 0xFFB0D8);
				tint(c.dawnTint, .6f, 0xFF80C0);
			}),
			new Look("Nebelmorgen", c -> {
				effect(c, 10, 1.0f);
				time(c, 7.0f);
				c.fogDensity = 2.5f;
				c.saturation = .5f;
				tint(c.skyTint, .5f, 0xCCD6E0);
				tint(c.cloudTint, .5f, 0xE0E4EA);
				tint(c.fogTint, .75f, 0xD8DEE6);
			}),
			new Look("Eiswelt", c -> {
				effect(c, 7, 1.0f);
				time(c, 12.0f);
				c.saturation = .7f;
				c.skyBrightness = 1.15f;
				tint(c.skyTint, .6f, 0x9AD8FF);
				tint(c.cloudTint, .5f, 0xEAF7FF);
				tint(c.fogTint, .7f, 0xCCEBFF);
			}),
			new Look("Giftgrün", c -> {
				effect(c, 5, 1.0f);
				time(c, 12.0f);
				c.saturation = 1.2f;
				tint(c.skyTint, .7f, 0x40E040);
				tint(c.cloudTint, .6f, 0xA0FF70);
				tint(c.fogTint, .6f, 0x50C040);
			}),
			new Look("Wüstenglut", c -> {
				effect(c, 9, 1.0f);
				time(c, 13.0f);
				c.saturation = 1.2f;
				c.skyBrightness = 1.1f;
				c.fogDensity = .8f;
				tint(c.skyTint, .5f, 0xFFC070);
				tint(c.cloudTint, .5f, 0xFFE0B0);
				tint(c.fogTint, .65f, 0xF0B060);
			}),
			new Look("Tiefsee", c -> {
				effect(c, 2, 1.0f);
				time(c, 11.0f);
				c.skyBrightness = .9f;
				c.fogDensity = 1.8f;
				tint(c.skyTint, .7f, 0x0A5A80);
				tint(c.cloudTint, .5f, 0x70A8C0);
				tint(c.fogTint, .8f, 0x084868);
			}),
			new Look("Schwarz-Weiß", c -> {
				c.saturation = 0f;
				effect(c, 4, 0.5f);
			}),
			new Look("Regenbogen", c -> {
				effect(c, 7, 0.5f);
				time(c, 12.0f);
				c.rainbowSpeed = 1.2f;
				rainbow(c.skyTint, .75f);
				rainbow(c.cloudTint, .6f);
				rainbow(c.fogTint, .6f);
				rainbow(c.dawnTint, .8f);
			}),
			new Look("Disco", c -> {
				effect(c, 12, 1.0f);
				time(c, 0.0f);
				c.rainbowSpeed = 7f;
				c.skyBrightness = 1.5f;
				c.starBrightness = .3f;
				rainbow(c.skyTint, .9f);
				rainbow(c.cloudTint, .9f);
				rainbow(c.fogTint, .9f);
			}),
			new Look("Regenbogen-Nacht", c -> {
				effect(c, 7, 0.8f);
				time(c, 1.0f);
				c.rainbowSpeed = .6f;
				c.skyBrightness = 1.6f;
				rainbow(c.skyTint, .5f);
				rainbow(c.fogTint, .5f);
				rainbow(c.cloudTint, .4f);
			}),
			new Look("Aurora", c -> {
				effect(c, 3, 1.0f);
				time(c, 0.0f);
				c.skyBrightness = 1.8f;
				tint(c.skyTint, .35f, 0x20FFA0);
				tint(c.fogTint, .4f, 0x10A080);
				tint(c.cloudTint, .5f, 0x80FFD0);
			}),
			new Look("Cyberpunk", c -> {
				effect(c, 4, 1.0f);
				time(c, 22.0f);
				c.skyBrightness = 1.4f;
				c.saturation = 1.4f;
				tint(c.skyTint, .7f, 0xFF2BD6);
				tint(c.fogTint, .7f, 0x00E5FF);
				tint(c.cloudTint, .6f, 0xFF60E0);
			}),
			new Look("Galaxie", c -> {
				effect(c, 7, 1.0f);
				time(c, 1.0f);
				c.moonPhase = 5;
				c.skyBrightness = 1.7f;
				tint(c.skyTint, .7f, 0x2A0A60);
				tint(c.fogTint, .6f, 0x300A70);
				tint(c.cloudTint, .5f, 0x8040FF);
			}),
			new Look("Weltuntergang", c -> {
				effect(c, 8, 1.0f);
				time(c, 18.3f);
				c.skyBrightness = .9f;
				c.fogDensity = 2.2f;
				tint(c.skyTint, .8f, 0xFF3000);
				tint(c.fogTint, .8f, 0xB02000);
				tint(c.cloudTint, .7f, 0x601000);
				tint(c.dawnTint, 1f, 0xFF2000);
			}),
			new Look("Zuckerwatte", c -> {
				effect(c, 11, 0.6f);
				time(c, 14.0f);
				c.saturation = 1.1f;
				c.skyBrightness = 1.15f;
				tint(c.skyTint, .55f, 0xFFB0E0);
				tint(c.cloudTint, .8f, 0xB0F0FF);
				tint(c.fogTint, .6f, 0xFFD0F0);
			}),
			new Look("Sonnenaufgang", c -> {
				effect(c, 5, 0.5f);
				time(c, 6.2f);
				c.saturation = 1.2f;
				tint(c.skyTint, .45f, 0xFF9A60);
				tint(c.cloudTint, .5f, 0xFFB0A0);
				tint(c.fogTint, .5f, 0xFFA070);
				tint(c.dawnTint, .9f, 0xFF7030);
			}),
			new Look("Gewitter", c -> {
				effect(c, 1, 1.0f);
				time(c, 14.0f);
				c.skyBrightness = .55f;
				c.saturation = .3f;
				c.fogDensity = 1.8f;
				tint(c.skyTint, .6f, 0x404858);
				tint(c.fogTint, .7f, 0x505868);
				tint(c.cloudTint, .7f, 0x303640);
			}),
			new Look("Matrix", c -> {
				effect(c, 4, 0.7f);
				time(c, 0.0f);
				c.starBrightness = .2f;
				c.skyBrightness = 1.2f;
				tint(c.skyTint, .9f, 0x00FF40);
				tint(c.fogTint, .8f, 0x003810);
				tint(c.cloudTint, .7f, 0x20A040);
			}),
			new Look("Mars", c -> {
				effect(c, 10, 0.8f);
				time(c, 12.0f);
				c.saturation = 1.1f;
				c.fogDensity = 1.3f;
				tint(c.skyTint, .7f, 0xD0602A);
				tint(c.fogTint, .7f, 0xB05020);
				tint(c.cloudTint, .6f, 0xE08050);
			}),
			new Look("Ozeanabend", c -> {
				effect(c, 2, 0.6f);
				time(c, 19.0f);
				tint(c.skyTint, .5f, 0xE07060);
				tint(c.fogTint, .6f, 0x6080A0);
				tint(c.cloudTint, .5f, 0xF0A090);
				tint(c.dawnTint, .8f, 0xFF6A50);
			}),
			new Look("Neon-Nacht", c -> {
				effect(c, 4, 0.8f);
				time(c, 23.0f);
				c.skyBrightness = 1.5f;
				tint(c.skyTint, .8f, 0x4010A0);
				tint(c.fogTint, .7f, 0xFF10C0);
				tint(c.cloudTint, .6f, 0x10E0FF);
			})
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

	private static void effect(SkyConfig c, int id, float strength) {
		c.fx[id].on = true;
		c.fx[id].strength = strength;
		c.fx[id].speed = 1f;
		c.lightningBolts = true;
	}

	private static void rainbow(SkyConfig.Tint t, float strength) {
		t.rainbow = true;
		t.strength = strength;
	}

	private static void tint(SkyConfig.Tint t, float strength, int rgb) {
		t.strength = strength;
		t.r = ((rgb >> 16) & 0xFF) / 255f;
		t.g = ((rgb >> 8) & 0xFF) / 255f;
		t.b = (rgb & 0xFF) / 255f;
	}
}
