package dev.skysettings;

import net.minecraft.client.gui.DrawContext;

/** Farben und Zeichenhilfen fuer das Menue. Alles wird live aus der Config gelesen. */
public final class Theme {
	public static final String[] STYLE_NAMES = {"Dunkel", "Hell", "Glas", "Neon"};
	/** Blau, Tuerkis, Gruen, Gelb, Orange, Rot, Pink, Lila */
	public static final int[] PRESETS = {
			0x3D8BFF, 0x00BBF9, 0x4ADE80, 0xFEE440, 0xFF9F1C, 0xEF4444, 0xF15BB5, 0x9B5DE5
	};

	private Theme() {
	}

	private static SkyConfig c() {
		return SkyConfig.get();
	}

	public static int accent() {
		SkyConfig c = c();
		return 0xFF000000 | (b(c.accentR) << 16) | (b(c.accentG) << 8) | b(c.accentB);
	}

	public static int radius() {
		return c().rounded ? 4 : 0;
	}

	public static int panel() {
		int base = switch (c().style) {
			case 1 -> 0xF2F3F6;
			case 2 -> 0x0E1218;
			case 3 -> 0x07070D;
			default -> 0x14171E;
		};
		float a = switch (c().style) {
			case 2 -> 0.55f * c().panelOpacity;
			default -> c().panelOpacity;
		};
		return argb(base, a);
	}

	public static int surface() {
		return switch (c().style) {
			case 1 -> 0xFFFFFFFF;
			case 2 -> 0x38FFFFFF;
			case 3 -> 0xFF12121E;
			default -> 0xFF1F242E;
		};
	}

	public static int surfaceHover() {
		return mix(surface(), accent(), 0.25f);
	}

	public static int panelBorder() {
		return switch (c().style) {
			case 1 -> 0xFFD3D8E0;
			case 2 -> 0x50FFFFFF;
			case 3 -> accent();
			default -> 0xFF2C3340;
		};
	}

	public static int text() {
		return c().style == 1 ? 0xFF1B1F27 : 0xFFF1F3F8;
	}

	public static int muted() {
		return c().style == 1 ? 0xFF6B7280 : 0xFF9AA2B2;
	}

	/** Lesbare Textfarbe auf der Akzentfarbe. */
	public static int onAccent() {
		int a = accent();
		double lum = 0.299 * ((a >> 16) & 0xFF) + 0.587 * ((a >> 8) & 0xFF) + 0.114 * (a & 0xFF);
		return lum > 160 ? 0xFF10131A : 0xFFFFFFFF;
	}

	public static int track() {
		return c().style == 1 ? 0xFFD9DEE6 : 0xFF343B49;
	}

	public static int argb(int rgb, float alpha) {
		int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
		return (a << 24) | (rgb & 0xFFFFFF);
	}

	public static int mix(int c1, int c2, float t) {
		int a = lerp(c1 >>> 24, c2 >>> 24, t);
		int r = lerp((c1 >> 16) & 0xFF, (c2 >> 16) & 0xFF, t);
		int g = lerp((c1 >> 8) & 0xFF, (c2 >> 8) & 0xFF, t);
		int bl = lerp(c1 & 0xFF, c2 & 0xFF, t);
		return (a << 24) | (r << 16) | (g << 8) | bl;
	}

	private static int lerp(int a, int b, float t) {
		return Math.round(a + (b - a) * t);
	}

	private static int b(float v) {
		return Math.max(0, Math.min(255, Math.round(v * 255f)));
	}

	/** Gefuelltes Rechteck mit abgerundeten Ecken (radius 0 = eckig). */
	public static void rect(DrawContext ctx, int x, int y, int w, int h, int color, int radius) {
		int r = Math.min(radius, Math.min(w, h) / 2);
		if (r <= 0) {
			ctx.fill(x, y, x + w, y + h, color);
			return;
		}
		for (int i = 0; i < r; i++) {
			double dy = r - i - 0.5;
			int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
			ctx.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
			ctx.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
		}
		ctx.fill(x, y + r, x + w, y + h - r, color);
	}

	/** Rechteck mit 1 px Rahmen. */
	public static void framed(DrawContext ctx, int x, int y, int w, int h, int fill, int border, int radius) {
		rect(ctx, x, y, w, h, border, radius);
		rect(ctx, x + 1, y + 1, w - 2, h - 2, fill, Math.max(0, radius - 1));
	}
}
