package dev.skysettings;

import net.minecraft.client.gui.DrawContext;

/** Animierte Hintergruende fuer das Menue. Alles wird aus der Zeit berechnet, es gibt keinen Zustand. */
public final class Anim {
	private Anim() {
	}

	private static float rnd(int i, int salt) {
		int n = i * 374761393 + salt * 668265263;
		n = (n ^ (n >>> 13)) * 1274126177;
		n ^= n >>> 16;
		return (n & 0xFFFFFF) / 16777216f;
	}

	/** Zeichnet die Animation in das Rechteck (x, y, w, h). */
	public static void draw(DrawContext c, int x, int y, int w, int h, int anim, float t, int accent) {
		int ix = x + 3, iy = y + 3, iw = w - 6, ih = h - 6;
		int rgb = accent & 0xFFFFFF;
		switch (anim) {
			case 1 -> { // Staub / Glitzer
				for (int i = 0; i < 34; i++) {
					float px = ix + rnd(i, 1) * iw + (float) Math.sin(t * 0.4f + i) * 6f;
					float py = iy + ih - ((rnd(i, 2) * ih + t * (4f + rnd(i, 3) * 8f)) % ih);
					int a = 0x28 + (int) (0x30 * (0.5f + 0.5f * Math.sin(t * 1.3f + i)));
					int col = (a << 24) | (rgb != 0 ? rgb : 0x806020);
					c.fill(clampX(px, ix, iw), (int) py, clampX(px, ix, iw) + 2, (int) py + 2, col);
				}
			}
			case 2 -> { // Terminal: Matrix-Regen + Scanlines
				int cols = iw / 9;
				for (int i = 0; i < cols; i++) {
					float speed = 28f + rnd(i, 4) * 60f;
					float head = (t * speed + rnd(i, 5) * ih * 2f) % (ih + 70f) - 20f;
					for (int k = 0; k < 9; k++) {
						int py = iy + (int) head - k * 7;
						if (py < iy || py > iy + ih - 7) {
							continue;
						}
						float f = 1f - k / 9f;
						int a = (int) (255 * f * (k == 0 ? 0.55f : 0.28f));
						c.fill(ix + i * 9 + 2, py, ix + i * 9 + 7, py + 6, (a << 24) | 0x39FF6A);
					}
				}
				for (int ly = iy; ly < iy + ih; ly += 4) {
					c.fill(ix, ly, ix + iw, ly + 1, 0x14000000);
				}
			}
			case 3 -> { // Nether: aufsteigende Glut
				for (int i = 0; i < 44; i++) {
					float frac = (rnd(i, 6) + t * (0.03f + rnd(i, 7) * 0.05f)) % 1f;
					int py = iy + ih - (int) (frac * ih);
					float px = ix + rnd(i, 8) * iw + (float) Math.sin(t * 1.2f + i) * 7f;
					float flick = 0.6f + 0.4f * (float) Math.sin(t * 7f + i * 3f);
					int a = (int) (255 * (1f - frac) * 0.85f * flick);
					int col = (a << 24) | (rnd(i, 9) > 0.5f ? 0xFF8A1A : 0xFF3A10);
					int s = 1 + (int) (rnd(i, 10) * 2.5f);
					c.fill(clampX(px, ix, iw), py, clampX(px, ix, iw) + s, py + s, col);
				}
				c.fillGradient(ix, iy + ih - 40, ix + iw, iy + ih, 0x00FF5A10, 0x30FF5A10);
			}
			case 4 -> { // Ende: Sterne + Nebel
				float pulse = 0.5f + 0.5f * (float) Math.sin(t * 0.6f);
				c.fillGradient(ix, iy, ix + iw, iy + ih, 0x00000000, (int) (0x28 + 0x18 * pulse) << 24 | rgb);
				for (int i = 0; i < 60; i++) {
					int px = ix + (int) (rnd(i, 11) * iw);
					int py = iy + (int) (rnd(i, 12) * ih);
					float tw = 0.5f + 0.5f * (float) Math.sin(t * (0.8f + rnd(i, 13) * 1.6f) + i);
					int a = (int) (255 * (0.15f + 0.8f * tw));
					int s = rnd(i, 14) > 0.85f ? 2 : 1;
					c.fill(px, py, px + s, py + s, (a << 24) | (rnd(i, 15) > 0.5f ? 0xFFFFFF : 0xC8B4FF));
				}
			}
			case 5 -> { // Dunkel: wandernder Lichtschimmer
				float x0 = ix + (t * 38f) % (iw + 120f) - 60f;
				for (int k = -30; k <= 30; k += 2) {
					float f = 1f - Math.abs(k) / 30f;
					int px = (int) (x0 + k);
					if (px < ix || px > ix + iw - 2) {
						continue;
					}
					c.fill(px, iy, px + 2, iy + ih, ((int) (0x1C * f) << 24) | 0xFFFFFF);
				}
				c.fillGradient(ix, iy + ih - 50, ix + iw, iy + ih, 0x00000000, 0x22000000 | rgb);
			}
			case 6 -> { // Glas / Hell: aufsteigende Blasen
				for (int i = 0; i < 16; i++) {
					int size = 6 + (int) (rnd(i, 16) * 14);
					float frac = (rnd(i, 17) + t * (0.02f + rnd(i, 18) * 0.03f)) % 1f;
					int py = iy + ih - (int) (frac * (ih + size)) ;
					float px = ix + rnd(i, 19) * (iw - size) + (float) Math.sin(t * 0.7f + i) * 8f;
					int px2 = clampX(px, ix, iw - size);
					Theme.rect(c, px2, py, size, size, 0x16000000 | (i % 2 == 0 ? 0xFFFFFF : rgb), size / 2);
				}
			}
			case 7 -> { // Neon: Raster + Scanner
				for (int gx = ix; gx < ix + iw; gx += 24) {
					c.fill(gx, iy, gx + 1, iy + ih, 0x12000000 | rgb);
				}
				for (int gy = iy; gy < iy + ih; gy += 24) {
					c.fill(ix, gy, ix + iw, gy + 1, 0x12000000 | rgb);
				}
				int ly = iy + (int) ((t * 55f) % ih);
				c.fillGradient(ix, Math.max(iy, ly - 26), ix + iw, ly, 0x00000000 | rgb, 0x40000000 | rgb);
				c.fill(ix, ly, ix + iw, ly + 1, 0x90000000 | rgb);
			}
			case 9 -> { // Holz: warmer, flackernder Schein von unten
				float f = 0.5f + 0.5f * (float) Math.sin(t * 2.1f) * (0.6f + 0.4f * (float) Math.sin(t * 5.3f));
				c.fillGradient(ix, iy + ih / 3, ix + iw, iy + ih, 0x00FF9A30, ((int) (0x1A + 0x1C * f) << 24) | 0xFF9A30);
			}
			default -> {
			}
		}
	}

	private static int clampX(float v, int x, int w) {
		return Math.max(x, Math.min(x + w - 2, (int) v));
	}
}
