package dev.skysettings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Kleine Himmel-Vorschau fuer das Menue: zeigt naeherungsweise, wie der Himmel mit den aktuellen
 * Einstellungen aussieht (Uhrzeit, Farben, Wolken, Sterne, Nebel, Effekte, Block-Effekte).
 */
public final class SkyPreview {
	private SkyPreview() {
	}

	private static float smooth(double e0, double e1, double v) {
		double x = Math.max(0, Math.min(1, (v - e0) / (e1 - e0)));
		return (float) (x * x * (3 - 2 * x));
	}

	private static float rnd(int i, int salt) {
		return BlockSky.rnd(i, salt);
	}

	/** Uhrzeit (0 - 24 Uhr) der Welt, inklusive deiner eigenen Uhrzeit. */
	public static float currentHour() {
		MinecraftClient mc = MinecraftClient.getInstance();
		long tod = mc.world != null ? mc.world.getTimeOfDay() : 6000;
		return (float) ((((tod % 24000L) + 24000L) % 24000L) / 1000.0 + 6.0) % 24f;
	}

	public static void draw(DrawContext ctx, int x, int y, int w, int h) {
		SkyConfig c = SkyConfig.get();
		Effects.Sample fx = Effects.sample(true);
		float hour = currentHour();
		double t = System.nanoTime() / 1_000_000_000.0;
		int hz = y + (int) (h * 0.72f);
		int skyH = hz - y;

		ctx.enableScissor(x, y, x + w, y + h);

		// Grundfarben wie im normalen Spiel, passend zur Uhrzeit
		double alt = Math.sin((hour - 6.0) * Math.PI / 12.0);
		float day = smooth(-0.12, 0.25, alt);
		int zenith = BlockSky.mix(0x05060F, 0x7BA4FF, day);
		int horizon = BlockSky.mix(0x0A0E1E, 0xBCD4FF, day);
		float dusk = Math.max(0f, 1f - (float) Math.abs(alt) / 0.3f);
		horizon = BlockSky.mix(horizon, 0xFF9A50, dusk * 0.55f);

		// Deine Einstellungen anwenden
		int zen = 0xFFFFFF & SkyEffects.preview(0xFF000000 | zenith, c.skyTint, c.skyBrightness, fx);
		int hor = 0xFFFFFF & SkyEffects.preview(0xFF000000 | horizon, c.fogTint, c.skyBrightness, fx);
		ctx.fillGradient(x, y, x + w, hz, 0xFF000000 | zen, 0xFF000000 | hor);

		// Daemmerungsglanz am Horizont
		if (dusk > 0.01f) {
			int dawn = 0xFFFFFF & SkyEffects.preview(0xFFFF8A3C, c.dawnTint, 1f, fx);
			ctx.fillGradient(x, hz - (int) (skyH * 0.4f), x + w, hz, BlockSky.col(dawn, 0f), BlockSky.col(dawn, 0.5f * dusk));
		}

		// Sterne
		float night = 1f - day;
		if (night > 0.05f) {
			float mul = c.starBrightness * fx.starMul() * (1f - 0.8f * fx.flash());
			for (int i = 0; i < 45; i++) {
				int sx = x + (int) (rnd(i, 201) * (w - 2));
				int sy = y + (int) (rnd(i, 202) * (skyH - 8));
				float tw = 0.6f + 0.4f * (float) Math.sin(t * (0.8 + rnd(i, 203) * 2) + i);
				ctx.fill(sx, sy, sx + 2, sy + 2, BlockSky.col(0xFFFFFF, Math.min(1f, night * mul * tw)));
			}
		}

		// Sonne und Mond (eckig wie in Minecraft)
		drawBody(ctx, x, y, w, skyH, hz, (hour - 6f) / 12f, (float) Math.sin((hour - 6.0) * Math.PI / 12.0), 0xFFE97A, 12);
		drawBody(ctx, x, y, w, skyH, hz, (hour - 18f) / 12f, (float) Math.sin((hour - 18.0) * Math.PI / 12.0), 0xE0E6F0, 10);

		// Nebelband am Horizont
		float band = skyH * (0.12f + 0.12f * Math.min(3f, c.fogDensity));
		ctx.fillGradient(x, hz - (int) band, x + w, hz, BlockSky.col(hor, 0f), BlockSky.col(hor, 0.9f));

		// Wolken (eckig, wandern langsam)
		int cs = Math.max(3, w / 36);
		int cloudBase = 0xFFFFFF & BlockSky.mix(0x2C3046, 0xFFFFFF, day);
		int cloudCol = 0xFFFFFF & SkyEffects.preview(0xFF000000 | cloudBase, c.cloudTint, c.cloudBrightness, fx);
		float cloudAlpha = Math.min(1f, c.cloudOpacity * 0.92f);
		float yOffset = (c.cloudHeightOffset + fx.cloudDy()) * 0.15f;
		for (int i = 0; i < 6; i++) {
			int cw = 4 + (int) (rnd(i, 211) * 4);
			int cx = x + (int) (((rnd(i, 212) * (w + 60) + t * 5) % (w + 60))) - 30;
			int cy = y + (int) (skyH * (0.14f + 0.4f * rnd(i, 213)) - yOffset);
			for (int row = 0; row < 3; row++) {
				int inset = row == 1 ? 0 : 1;
				ctx.fill(cx + inset * cs, cy + row * cs, cx + (cw - inset) * cs, cy + (row + 1) * cs, BlockSky.col(cloudCol, cloudAlpha));
			}
		}

		// Boden: Huegel und Baeume als Silhouette
		int ground = BlockSky.mix(0x0A140A, 0x2F5A2A, day);
		ground = 0xFFFFFF & SkyEffects.preview(0xFF000000 | ground, new SkyConfig.Tint(), 1f, Effects.Sample.NEUTRAL);
		ctx.fill(x, hz, x + w, y + h, 0xFF000000 | ground);
		int gs = Math.max(3, w / 40);
		for (int cx = x, ci = 0; cx < x + w; cx += gs, ci++) {
			int hh = (int) (rnd(ci / 3, 221) * 2.5f + (rnd(ci, 222) > 0.85f ? 2 : 0));
			if (hh > 0) {
				ctx.fill(cx, hz - hh * gs, cx + gs, hz, 0xFF000000 | ground);
			}
		}

		// Block-Effekte im Himmel
		if (c.previewFx) BlockSky.render(ctx, x, y, w, h, hz, w / Math.max(1f, ctx.getScaledWindowWidth()));

		// Blitzlicht ueber dem ganzen Himmel
		if (fx.flash() > 0.02f) {
			ctx.fill(x, y, x + w, hz, BlockSky.col(0xE8F0FF, 0.35f * fx.flash()));
		}

		ctx.disableScissor();
	}

	private static void drawBody(DrawContext ctx, int x, int y, int w, int skyH, int hz, float frac, float alt, int rgb, int size) {
		float f = frac - (float) Math.floor(frac / 2f) * 2f; // 0 - 2
		if (f < 0f || f > 1f || alt < -0.05f) {
			return;
		}
		int bx = x + (int) (w * (0.08f + 0.84f * f)) - size / 2;
		int by = hz - (int) (Math.max(0f, alt) * skyH * 0.85f) - size / 2;
		ctx.fill(bx - 2, by - 2, bx + size + 2, by + size + 2, BlockSky.col(rgb, 0.25f));
		ctx.fill(bx, by, bx + size, by + size, 0xFF000000 | rgb);
	}
}
