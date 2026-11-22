package dev.skysettings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.World;

/**
 * Sichtbare Block-Effekte im Himmel (Wellen, Feuer, Blitze, Polarlicht ...). Sie bestehen aus eckigen Bloecken,
 * werden ueber den Himmelsbereich des Bildschirms gezeichnet und aendern sich nur bei dir.
 */
public final class BlockSky {
	public static final String[] NAMES = {
			"Keiner", "Wellen", "Feuer", "Blitze", "Polarlicht", "Schnee", "Sternenfunkeln", "Matrix-Regen",
			"Regenbogen-Bogen", "Glut", "Nebelschwaden", "Disco-Felder", "Neon-Raster", "Blasen"
	};
	public static final int WAVES = 1, FIRE = 2, BOLTS = 3, AURORA = 4, SNOW = 5, STARS = 6, MATRIX = 7, RAINBOW = 8,
			EMBERS = 9, MIST = 10, DISCO = 11, GRID = 12, BUBBLES = 13;
	private static final int[] RAINBOW_COLORS = {0xFF3B30, 0xFF9500, 0xFFE600, 0x34C759, 0x32ADE6, 0x5856D6, 0xAF52DE};

	private BlockSky() {
	}

	// ------------------------------------------------------------------ Hilfen

	static float rnd(int i, int salt) {
		int n = i * 374761393 + salt * 668265263;
		n = (n ^ (n >>> 13)) * 1274126177;
		n ^= n >>> 16;
		return (n & 0xFFFFFF) / 16777216f;
	}

	static int col(int rgb, float alpha) {
		int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
		return (a << 24) | (rgb & 0xFFFFFF);
	}

	static int mix(int c1, int c2, float t) {
		int r = Math.round(((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t);
		int g = Math.round(((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t);
		int b = Math.round((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t);
		return (r << 16) | (g << 8) | b;
	}

	private static void cell(DrawContext c, int x, int y, int s, int color) {
		c.fill(x, y, x + s, y + s, color);
	}

	private static boolean anyActive(SkyConfig c) {
		for (int i = 1; i < NAMES.length; i++) {
			if (c.bfx[i].on && c.bfx[i].strength > 0.001f) {
				return true;
			}
		}
		return false;
	}

	// ------------------------------------------------------------------ HUD

	/** Zeichnet die Effekte ueber den Himmelsbereich des Spielbildschirms. */
	private static double yawPx = 0;

	public static void renderHud(DrawContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		SkyConfig c = SkyConfig.get();
		if (mc.world == null || mc.player == null || !c.enabled || !c.blockFxEnabled || !anyActive(c)
				|| mc.world.getRegistryKey() != World.OVERWORLD || (c.fxOnlyInGame && mc.currentScreen != null)) {
			return;
		}
		int w = ctx.getScaledWindowWidth();
		int h = ctx.getScaledWindowHeight();
		double fov = mc.options.getFov().getValue();
		double pitch = Math.toRadians(mc.player.getPitch());
		double hz = h / 2.0 - Math.tan(pitch) / Math.tan(Math.toRadians(fov) / 2.0) * (h / 2.0);
		int horizon = (int) Math.max(0, Math.min(h, hz));
		if (horizon < 8) {
			return; // Blick nach unten: kein Himmel sichtbar
		}
		yawPx = mc.player.getYaw() * (w / Math.max(30.0, fov * w / (double) h));
		render(ctx, 0, 0, w, h, horizon, 1f);
	}

	/**
	 * Zeichnet alle eingeschalteten Block-Effekte in den Himmelsbereich (x0, y0) bis (x0 + w, hz).
	 *
	 * @param scale Verkleinerungsfaktor gegenueber dem Spielbildschirm (fuer die Vorschau)
	 */
	public static void render(DrawContext ctx, int x0, int y0, int w, int h, int hz, float scale) {
		if (scale < 0.999f) {
			yawPx = 0;
		}
		SkyConfig c = SkyConfig.get();
		if (!c.blockFxEnabled) { // Mod-Schalter wird in renderHud geprueft; die Vorschau zeigt immer
			return;
		}
		float[] qmul = {1f, 1.5f, 2f, 3f};
		int s = Math.max(2, Math.round(c.blockSize * scale * qmul[Math.max(0, Math.min(3, c.fxQuality))]));
		double now = System.nanoTime() / 1_000_000_000.0;
		if (c.fxFps > 0) {
			now = Math.floor(now * c.fxFps) / c.fxFps; // Animation nur so oft aktualisieren, wie eingestellt
		}
		for (int id = 1; id < NAMES.length; id++) {
			SkyConfig.Fx f = c.bfx[id];
			if (!f.on || f.strength <= 0.001f) {
				continue;
			}
			double t = (now * f.speed) % 100000.0;
			float k = Math.min(2f, f.strength);
			switch (id) {
				case WAVES -> waves(ctx, x0, y0, w, hz, s, t, k);
				case FIRE -> fire(ctx, x0, y0, w, hz, s, t, k);
				case BOLTS -> bolts(ctx, x0, y0, w, hz, s, t, k);
				case AURORA -> aurora(ctx, x0, y0, w, hz, s, t, k);
				case SNOW -> snow(ctx, x0, y0, w, hz, s, t, k);
				case STARS -> stars(ctx, x0, y0, w, hz, s, t, k);
				case MATRIX -> matrix(ctx, x0, y0, w, hz, s, t, k);
				case RAINBOW -> rainbow(ctx, x0, y0, w, hz, s, t, k);
				case EMBERS -> embers(ctx, x0, y0, w, hz, s, t, k);
				case MIST -> mist(ctx, x0, y0, w, hz, s, t, k);
				case DISCO -> disco(ctx, x0, y0, w, hz, s, t, k);
				case GRID -> grid(ctx, x0, y0, w, hz, s, t, k);
				case BUBBLES -> bubbles(ctx, x0, y0, w, hz, s, t, k);
				default -> {
				}
			}
		}
	}

	// ------------------------------------------------------------------ Effekte

	private static void waves(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		// Dezent: nur ein schmales Band ueber dem Horizont, durchsichtig, nach oben ausblendend
		float base = Math.min(0.5f, 0.07f + 0.09f * k);
		int bands = 4;
		int shift = (int) Math.round(yawPx / s);
		for (int b = 0; b < bands; b++) {
			float yb = hz - skyH * (0.10f + b * 0.09f);
			int rgb = mix(0x2A5CC8, 0x7FEBFF, b / (float) (bands - 1));
			float a = base * (1f - b * 0.18f);
			for (int cx = x0, ci = 0; cx < x0 + w; cx += s, ci++) {
				int wi = ci + shift;
				double wy = yb + Math.sin(wi * 0.28 + t * 0.9 + b * 1.3) * s * 1.2 + Math.sin(wi * 0.11 - t * 0.5) * s * 0.6;
				int gy = y0 + Math.round((float) ((wy - y0) / s)) * s;
				if (gy < y0 || gy + s > hz) {
					continue;
				}
				cell(c, cx, gy, s, col(rgb, a));
			}
		}
	}

	private static void fire(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		int maxCells = Math.max(2, (int) (skyH * 0.6f / s));
		for (int cx = x0, ci = 0; cx < x0 + w; cx += s, ci++) {
			double n = 0.5 + 0.5 * Math.sin(t * 3.2 + ci * 0.9) * Math.sin(t * 1.7 + ci * 0.37);
			int hc = (int) (maxCells * (0.2 + 0.8 * Math.max(0, Math.min(1, n * (0.6 + 0.4 * k)))));
			for (int j = 0; j < hc; j++) {
				int y = hz - (j + 1) * s;
				if (y < y0) {
					break;
				}
				if (rnd(ci * 131 + j, (int) (t * 9)) < 0.12f) {
					continue;
				}
				float f = 1f - (float) j / Math.max(1, hc);
				int rgb = f > 0.7f ? 0xFFE040 : f > 0.4f ? 0xFF8020 : 0xD02010;
				cell(c, cx, y, s, col(rgb, Math.min(1f, (0.2f + 0.5f * f) * (0.6f + 0.4f * k))));
			}
		}
	}

	private static void bolts(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		SkyConfig cfg = SkyConfig.get();
		double interval = cfg.boltInterval;
		float flash = (float) Math.min(1.0, Effects.stormFlash(t, interval) * k);
		if (flash < 0.03f) {
			return;
		}
		int slot = (int) Math.floor(t / interval);
		c.fill(x0, y0, x0 + w, hz, col(0xE8F0FF, 0.22f * flash));
		float a = Math.min(1f, flash * 1.3f);
		int x = x0 + (int) ((0.15f + 0.7f * rnd(slot, 63)) * w);
		int row = 0;
		for (int y = y0; y + s <= hz; y += s, row++) {
			x += (int) ((rnd(slot * 7 + row, 64) - 0.5f) * 2.4f * s);
			x = Math.max(x0, Math.min(x0 + w - s, x));
			cell(c, x - s, y, s, col(0x6FA0FF, a * 0.35f));
			cell(c, x + s, y, s, col(0x6FA0FF, a * 0.35f));
			cell(c, x, y, s, col(0xF4F8FF, a));
			if (rnd(slot * 13 + row, 65) > 0.9f) {
				int dir = rnd(slot * 17 + row, 66) > 0.5f ? 1 : -1;
				int bx = x;
				int len = 3 + (int) (rnd(slot * 19 + row, 67) * 4);
				for (int q = 1; q <= len && y + q * s + s <= hz; q++) {
					bx += dir * s;
					if (bx < x0 || bx + s > x0 + w) {
						break;
					}
					cell(c, bx, y + q * s, s, col(0xDCE8FF, a * (1f - q / (len + 1f))));
				}
			}
		}
	}

	private static void aurora(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		for (int cx = x0, ci = 0; cx < x0 + w; cx += s, ci++) {
			double top = y0 + skyH * (0.08 + 0.1 * (0.5 + 0.5 * Math.sin(ci * 0.12 + t * 0.5)));
			double hgt = skyH * 0.45 * (0.35 + 0.65 * (0.5 + 0.5 * Math.sin(ci * 0.07 - t * 0.4 + 1)));
			int cells = (int) (hgt / s);
			for (int j = 0; j < cells; j++) {
				int y = ((int) (top - y0) / s) * s + y0 + j * s;
				if (y + s > hz) {
					break;
				}
				float f = j / (float) Math.max(1, cells);
				int rgb = f < 0.5f ? mix(0x30FF90, 0x30D0FF, f * 2f) : mix(0x30D0FF, 0x9060FF, (f - 0.5f) * 2f);
				float flick = 0.85f + 0.15f * (float) Math.sin(t * 2 + ci);
				cell(c, cx, y, s, col(rgb, Math.min(1f, (0.45f * (1f - f) + 0.12f) * flick * (0.6f + 0.4f * k))));
			}
		}
	}

	private static void snow(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		int n = (int) (60 + 70 * k);
		for (int i = 0; i < n; i++) {
			float x = x0 + rnd(i, 71) * w + (float) Math.sin(t * 0.8 + i) * s;
			float y = y0 + (float) ((rnd(i, 72) * skyH + t * (10 + rnd(i, 73) * 25)) % skyH);
			int size = Math.max(2, s / 2 + (rnd(i, 74) > 0.7f ? s / 2 : 0));
			if (y + size > hz) {
				continue;
			}
			cell(c, (int) x, (int) y, size, col(0xFFFFFF, 0.55f + 0.35f * rnd(i, 75)));
		}
	}

	private static void stars(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		int n = (int) (45 + 25 * k);
		for (int i = 0; i < n; i++) {
			int x = x0 + (int) (rnd(i, 81) * (w - s));
			int y = y0 + (int) (rnd(i, 82) * (skyH - s));
			if (y + s > hz) {
				continue;
			}
			float tw = 0.5f + 0.5f * (float) Math.sin(t * (1.0 + rnd(i, 83) * 2.5) + i);
			int rgb = rnd(i, 84) > 0.6f ? 0xFFF3B0 : rnd(i, 84) > 0.3f ? 0xFFFFFF : 0xB8D0FF;
			float a = 0.25f + 0.7f * tw;
			cell(c, x, y, s / 2 + 1, col(rgb, a));
			if (rnd(i, 85) > 0.75f && tw > 0.6f) {
				int m = s / 2 + 1;
				cell(c, x - m, y, m, col(rgb, a * 0.5f));
				cell(c, x + m, y, m, col(rgb, a * 0.5f));
				cell(c, x, y - m, m, col(rgb, a * 0.5f));
				cell(c, x, y + m, m, col(rgb, a * 0.5f));
			}
		}
	}

	private static void matrix(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		int step = s + s / 2;
		for (int cx = x0, ci = 0; cx < x0 + w; cx += step, ci++) {
			double speed = 25 + rnd(ci, 91) * 55;
			double head = (t * speed + rnd(ci, 92) * skyH * 2) % (skyH + 8 * s) - 2 * s;
			for (int q = 0; q < 9; q++) {
				int y = y0 + ((int) head / s) * s - q * s;
				if (y < y0 || y + s > hz) {
					continue;
				}
				float f = 1f - q / 9f;
				cell(c, cx, y, s - 1, col(q == 0 ? 0xCFFFD8 : 0x30FF6A, Math.min(1f, f * 0.6f * (0.6f + 0.4f * k))));
			}
		}
	}

	private static void rainbow(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		float cx = x0 + w / 2f;
		float cy = hz + skyH * 0.55f;
		float rOut = skyH * 1.3f;
		float rIn = rOut - RAINBOW_COLORS.length * s;
		for (int y = y0; y + s <= hz; y += s) {
			for (int x = x0; x + s <= x0 + w; x += s) {
				double d = Math.hypot(x + s / 2.0 - cx, y + s / 2.0 - cy);
				int idx = (int) Math.floor((d - rIn) / s);
				if (idx < 0 || idx >= RAINBOW_COLORS.length) {
					continue;
				}
				float shimmer = 0.85f + 0.15f * (float) Math.sin(t * 1.5 + x * 0.05);
				cell(c, x, y, s, col(RAINBOW_COLORS[RAINBOW_COLORS.length - 1 - idx], Math.min(1f, 0.5f * shimmer * (0.6f + 0.4f * k))));
			}
		}
	}

	private static void embers(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		int n = (int) (50 + 50 * k);
		for (int i = 0; i < n; i++) {
			float frac = (float) ((rnd(i, 101) + t * (0.04 + rnd(i, 102) * 0.06)) % 1.0);
			int y = hz - (int) (frac * skyH);
			float x = x0 + rnd(i, 103) * w + (float) Math.sin(t * 1.3 + i) * s;
			float flick = 0.6f + 0.4f * (float) Math.sin(t * 8 + i * 3);
			int size = Math.max(2, s / 2 + (rnd(i, 104) > 0.8f ? s / 2 : 0));
			if (y < y0 || y + size > hz) {
				continue;
			}
			cell(c, (int) x, y, size, col(rnd(i, 105) > 0.5f ? 0xFF8A1A : 0xFF3A10, (1f - frac) * 0.9f * flick));
		}
	}

	private static void mist(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		for (int i = 0; i < 10; i++) {
			int bx = x0 + (int) ((rnd(i, 111) * w + t * (5 + rnd(i, 112) * 9)) % (w + 80)) - 40;
			int by = y0 + (int) (skyH * (0.15f + 0.7f * rnd(i, 113)));
			for (int q = 0; q < 9; q++) {
				int ox = (int) ((rnd(i * 9 + q, 114) * 5f - 1f) * s * 1.4f);
				int oy = (int) ((rnd(i * 9 + q, 115) * 2f - 1f) * s * 1.2f);
				int x = bx + ox;
				int y = by + oy;
				if (x < x0 || x + 2 * s > x0 + w || y < y0 || y + s > hz) {
					continue;
				}
				c.fill(x, y, x + 2 * s, y + s, col(0xE8ECF0, Math.min(1f, 0.22f * (0.6f + 0.4f * k))));
			}
		}
	}

	private static void disco(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int beat = (int) (t * 3);
		int cell = s * 2;
		for (int y = y0, r = 0; y + cell <= hz; y += cell, r++) {
			for (int x = x0, q = 0; x + cell <= x0 + w; x += cell, q++) {
				if (rnd(q * 97 + r, beat) > 0.78f) {
					int rgb = RAINBOW_COLORS[(int) (rnd(q * 31 + r, beat + 5) * RAINBOW_COLORS.length)];
					c.fill(x, y, x + cell, y + cell, col(rgb, Math.min(1f, 0.32f * (0.6f + 0.4f * k))));
				}
			}
		}
	}

	private static void grid(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int step = s * 4;
		float a = Math.min(1f, 0.28f * (0.6f + 0.4f * k));
		for (int x = x0; x < x0 + w; x += step) {
			c.fill(x, y0, x + 1, hz, col(0xFF2BD6, a));
		}
		for (int y = y0; y < hz; y += step) {
			c.fill(x0, y, x0 + w, y + 1, col(0x00E5FF, a));
		}
		int rows = Math.max(1, (hz - y0) / s);
		int sweep = (int) ((t * 6) % rows);
		int y = y0 + sweep * s;
		if (y + s <= hz) {
			c.fill(x0, y, x0 + w, y + s, col(0xFF2BD6, a * 1.4f));
		}
	}

	private static void bubbles(DrawContext c, int x0, int y0, int w, int hz, int s, double t, float k) {
		int skyH = hz - y0;
		int n = (int) (14 + 10 * k);
		for (int i = 0; i < n; i++) {
			int size = s + (int) (rnd(i, 121) * s * 1.5f);
			float frac = (float) ((rnd(i, 122) + t * (0.03 + rnd(i, 123) * 0.04)) % 1.0);
			int y = hz - size - (int) (frac * (skyH - size));
			float x = x0 + rnd(i, 124) * (w - size) + (float) Math.sin(t * 0.7 + i) * s;
			if (y < y0 || y + size > hz) {
				continue;
			}
			int rgb = mix(0x40FF90, 0xFF70D0, rnd(i, 125));
			int xi = (int) x;
			c.fill(xi, y, xi + size, y + size, col(rgb, 0.18f));
			c.fill(xi, y, xi + size, y + 1, col(rgb, 0.55f));
			c.fill(xi, y + size - 1, xi + size, y + size, col(rgb, 0.55f));
			c.fill(xi, y, xi + 1, y + size, col(rgb, 0.55f));
			c.fill(xi + size - 1, y, xi + size, y + size, col(rgb, 0.55f));
		}
	}
}
