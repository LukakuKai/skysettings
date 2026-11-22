package dev.skysettings;

import net.minecraft.client.gui.DrawContext;

/** Prozedurale Texturen fuer die Menue-Stile (Holzbretter, Stein, Pergament, Netherziegel). */
public final class Tex {
	private Tex() {
	}

	private static float rnd(int i, int salt) {
		int n = i * 374761393 + salt * 668265263;
		n = (n ^ (n >>> 13)) * 1274126177;
		n ^= n >>> 16;
		return (n & 0xFFFFFF) / 16777216f;
	}

	private static int a(int rgb, int alpha) {
		return (alpha << 24) | (rgb & 0xFFFFFF);
	}

	/** Zeichnet die Textur des aktuellen Stils in den Bereich (x, y, w, h). */
	public static void draw(DrawContext c, int x, int y, int w, int h) {
		switch (SkyConfig.get().style) {
			case 0 -> stone(c, x, y, w, h);
			case 1 -> planks(c, x, y, w, h);
			case 2 -> parchment(c, x, y, w, h);
			case 4 -> bricks(c, x, y, w, h);
			default -> {
			}
		}
	}

	/** Leichte Steinmaserung (Klassisch). */
	private static void stone(DrawContext c, int x, int y, int w, int h) {
		for (int i = 0; i < 200; i++) {
			int px = x + (int) (rnd(i, 21) * (w - 3));
			int py = y + (int) (rnd(i, 22) * (h - 3));
			int s = 2 + (int) (rnd(i, 23) * 2);
			c.fill(px, py, px + s, py + s, rnd(i, 24) > 0.5f ? 0x16FFFFFF : 0x14000000);
		}
	}

	/** Holzbretter mit Fugen, Maserung, Astloechern und Naegeln. */
	private static void planks(DrawContext c, int x, int y, int w, int h) {
		Theme.Wood wd = Theme.wood();
		int rowH = 17;
		int rows = h / rowH + 1;
		for (int r = 0; r < rows; r++) {
			int ry = y + r * rowH;
			int rh = Math.min(rowH, y + h - ry);
			if (rh <= 2) {
				break;
			}
			int px = x - (int) (rnd(r, 1) * 70);
			int plank = 0;
			while (px < x + w) {
				int seed = r * 131 + plank * 17;
				int len = 80 + (int) (rnd(seed, 2) * 110);
				int x1 = Math.max(px, x);
				int x2 = Math.min(px + len, x + w);
				if (x2 > x1) {
					float v = rnd(seed, 3);
					int tone = v > 0.5f ? wd.light() : wd.dark();
					int base = Theme.shade(Theme.mix(wd.base(), tone, 0.10f + 0.28f * Math.abs(v - 0.5f)));
					c.fill(x1, ry, x2, ry + rh, base);
					// Kanten: oben hell, unten dunkel
					c.fill(x1, ry, x2, ry + 1, Theme.shade(a(wd.light(), 0x90)));
					c.fill(x1, ry + rh - 1, x2, ry + rh, Theme.shade(a(wd.dark(), 0xC0)));
					// Stossfuge am Brettanfang
					if (px >= x) {
						c.fill(x1, ry, x1 + 1, ry + rh, Theme.shade(a(wd.dark(), 0xB0)));
						c.fill(x1 + 1, ry + 1, x1 + 2, ry + rh - 1, Theme.shade(a(wd.light(), 0x40)));
						// Naegel
						c.fill(x1 + 4, ry + 3, x1 + 6, ry + 5, Theme.shade(a(wd.dark(), 0xD0)));
						c.fill(x1 + 4, ry + rh - 6, x1 + 6, ry + rh - 4, Theme.shade(a(wd.dark(), 0xD0)));
					}
					// Maserung
					int grains = 4 + (int) (rnd(seed, 4) * 3);
					for (int g = 0; g < grains; g++) {
						int gy = ry + 2 + (int) (rnd(seed + g, 5) * (rh - 5));
						int gx = x1 + 3 + (int) (rnd(seed + g, 6) * Math.max(1, (x2 - x1) / 2));
						int gl = 14 + (int) (rnd(seed + g, 7) * Math.max(1, (x2 - x1) * 0.6f));
						int ge = Math.min(gx + gl, x2 - 1);
						if (ge > gx) {
							c.fill(gx, gy, ge, gy + 1, Theme.shade(a(wd.grain(), 0x78)));
						}
					}
					// Astloch
					if (rnd(seed, 8) > 0.78f && x2 - x1 > 40) {
						int kx = x1 + 12 + (int) (rnd(seed, 9) * (x2 - x1 - 28));
						int ky = ry + rh / 2 - 2;
						c.fill(kx - 1, ky - 1, kx + 7, ky + 5, Theme.shade(a(wd.grain(), 0x70)));
						c.fill(kx, ky, kx + 6, ky + 4, Theme.shade(a(wd.dark(), 0xB0)));
						c.fill(kx + 2, ky + 1, kx + 4, ky + 3, Theme.shade(a(0x000000, 0x80)));
					}
				}
				px += len;
				plank++;
			}
		}
	}

	/** Kleine Holzmaserung fuer Knoepfe (stabil, haengt nur von der Position ab). */
	public static void grain(DrawContext c, int x, int y, int w, int h) {
		Theme.Wood wd = Theme.wood();
		int seed = x * 7 + y * 13;
		for (int g = 0; g < 3; g++) {
			int gy = y + 2 + (int) (rnd(seed + g, 31) * Math.max(1, h - 4));
			int gx = x + 3 + (int) (rnd(seed + g, 32) * Math.max(1, w / 3));
			int ge = Math.min(gx + 10 + (int) (rnd(seed + g, 33) * w * 0.6f), x + w - 3);
			if (ge > gx) {
				c.fill(gx, gy, ge, gy + 1, Theme.shade(a(wd.grain(), 0x70)));
			}
		}
	}

	/** Papier mit dunklen Raendern, Flecken und Fasern (Pergament). */
	private static void parchment(DrawContext c, int x, int y, int w, int h) {
		c.fillGradient(x, y, x + w, y + 26, 0x55704A10, 0x00704A10);
		c.fillGradient(x, y + h - 26, x + w, y + h, 0x00704A10, 0x55704A10);
		for (int i = 0; i < 6; i++) {
			int s = 24 + (int) (rnd(i, 41) * 40);
			int px = x + (int) (rnd(i, 42) * (w - s));
			int py = y + (int) (rnd(i, 43) * (h - s));
			Theme.rect(c, px, py, s, s, 0x0C8A5A10, s / 2);
		}
		for (int i = 0; i < 70; i++) {
			int px = x + (int) (rnd(i, 44) * (w - 14));
			int py = y + (int) (rnd(i, 45) * (h - 2));
			c.fill(px, py, px + 4 + (int) (rnd(i, 46) * 10), py + 1, 0x16603A08);
		}
	}

	/** Netherziegel (Nether). */
	private static void bricks(DrawContext c, int x, int y, int w, int h) {
		int rowH = 14;
		int rows = h / rowH + 1;
		for (int r = 0; r < rows; r++) {
			int ry = y + r * rowH;
			int rh = Math.min(rowH, y + h - ry);
			if (rh <= 1) {
				break;
			}
			int off = (r % 2) * 14;
			for (int bx = x - off; bx < x + w; bx += 28) {
				int x1 = Math.max(bx, x);
				int x2 = Math.min(bx + 28, x + w);
				if (x2 <= x1) {
					continue;
				}
				float v = rnd(r * 53 + (bx - x), 51);
				c.fill(x1, ry, x2, ry + rh, v > 0.5f ? 0x1CFF5030 : 0x22000000);
				c.fill(x1, ry, x2, ry + 1, 0x70000000);
				c.fill(x1, ry, x1 + 1, ry + rh, 0x70000000);
				c.fill(x1 + 1, ry + 1, x2, ry + 2, 0x18FF8060);
			}
		}
	}
}
