package dev.skysettings;

import net.minecraft.client.gui.DrawContext;

/** Farben, Stile und Zeichenhilfen fuer das Menue. Alles wird live aus der Config gelesen. */
public final class Theme {
	/**
	 * @param bevel eckiger "Altmodisch"-Look mit hellem/dunklem Rand statt flacher Flaechen
	 * @param anim  Nummer der Hintergrund-Animation (siehe Anim)
	 */
	public record Style(String name, int panel, int surface, int text, int muted, int track, int border,
			int hi, int lo, boolean bevel, int anim, int accent) {
	}

	public static final Style[] STYLES = {
			new Style("Klassisch", 0xFFC6C6C6, 0xFF8B8B8B, 0xFF2B2B2B, 0xFF555555, 0xFF3A3A3A, 0xFF000000,
					0xFFFFFFFF, 0xFF555555, true, 0, 0xFF5B8DEF),
			new Style("Holz", 0xFF7A5230, 0xFF9B6E41, 0xFFFBEED6, 0xFFD9BD92, 0xFF3B2614, 0xFF241608,
					0xFFA97C4D, 0xFF4A301B, true, 9, 0xFFE8A93A),
			new Style("Pergament", 0xFFE9D8A6, 0xFFD6C187, 0xFF3B2A12, 0xFF7A6232, 0xFFB8A36A, 0xFF5E4418,
					0xFFF8EDC9, 0xFFB09450, true, 1, 0xFF9B3030),
			new Style("Terminal", 0xFF030A04, 0xFF0B1A0D, 0xFF39FF6A, 0xFF1FAA45, 0xFF0F2A14, 0xFF1F7A2E,
					0, 0, false, 2, 0xFF39FF6A),
			new Style("Nether", 0xFF2A0F10, 0xFF4A1C1E, 0xFFFFDDB8, 0xFFC99478, 0xFF1A0707, 0xFF0A0202,
					0xFF6A2A2C, 0xFF140606, true, 3, 0xFFFF7A1F),
			new Style("Ende", 0xFF0D0A1A, 0xFF1B1530, 0xFFEAE2FF, 0xFF9A8CCB, 0xFF2A2145, 0xFF6B4FBF,
					0, 0, false, 4, 0xFFB06CFF),
			new Style("Dunkel", 0xFF14171E, 0xFF1F242E, 0xFFF1F3F8, 0xFF9AA2B2, 0xFF343B49, 0xFF2C3340,
					0, 0, false, 5, 0xFF3D8BFF),
			new Style("Hell", 0xFFF2F3F6, 0xFFFFFFFF, 0xFF1B1F27, 0xFF6B7280, 0xFFD9DEE6, 0xFFD3D8E0,
					0, 0, false, 1, 0xFF2F7DF6),
			new Style("Glas", 0x8C0E1218, 0x38FFFFFF, 0xFFF4F6FA, 0xFFB5BCCB, 0xFF343B49, 0x50FFFFFF,
					0, 0, false, 6, 0xFF4FD1C5),
			new Style("Neon", 0xFF07070D, 0xFF12121E, 0xFFFFFFFF, 0xFFA9A9C8, 0xFF2A2A40, 0xFFFF2BD6,
					0, 0, false, 7, 0xFFFF2BD6)
	};

	public static final int WOOD_INDEX = 1;

	/** Holzsorte: Grundfarbe, Maserung, hell, dunkel */
	public record Wood(String name, int base, int grain, int light, int dark) {
	}

	public static final Wood[] WOODS = {
			new Wood("Eiche", 0xFFB08A57, 0xFF8C6A3E, 0xFFC9A56F, 0xFF6E5230),
			new Wood("Fichte", 0xFF7A5A34, 0xFF5E4224, 0xFF93703F, 0xFF45301A),
			new Wood("Birke", 0xFFD3C28B, 0xFFB5A06A, 0xFFE6DAA8, 0xFF9A8856),
			new Wood("Dschungel", 0xFFB07A55, 0xFF8A5A3C, 0xFFC8926A, 0xFF6A4630),
			new Wood("Akazie", 0xFFB86A3A, 0xFF92502A, 0xFFD08350, 0xFF6E3C1E),
			new Wood("Schwarzeiche", 0xFF4A3721, 0xFF35261A, 0xFF5C452B, 0xFF261A10),
			new Wood("Mangrove", 0xFF7A3A3A, 0xFF5E2B2B, 0xFF934848, 0xFF452020),
			new Wood("Kirsche", 0xFFE0B4B0, 0xFFC49A96, 0xFFF0CCC8, 0xFFA27A78),
			new Wood("Bambus", 0xFFC9B04D, 0xFFA8902F, 0xFFDCC768, 0xFF8A7424),
			new Wood("Karmesin", 0xFF7A3350, 0xFF5C2540, 0xFF934466, 0xFF451830),
			new Wood("Wirrwarr", 0xFF2E6F70, 0xFF1F5556, 0xFF3E8A8B, 0xFF143C3D)
	};

	/** Blau, Tuerkis, Gruen, Gelb, Orange, Rot, Pink, Lila */
	public static final int[] PRESETS = {
			0x3D8BFF, 0x00BBF9, 0x4ADE80, 0xFEE440, 0xFF9F1C, 0xEF4444, 0xF15BB5, 0x9B5DE5
	};

	private Theme() {
	}

	private static SkyConfig c() {
		return SkyConfig.get();
	}

	public static boolean isWood() {
		return c().style == WOOD_INDEX;
	}

	public static Wood wood() {
		return WOODS[Math.max(0, Math.min(WOODS.length - 1, c().woodType))];
	}

	public static Style style() {
		int i = Math.max(0, Math.min(STYLES.length - 1, c().style));
		if (i == WOOD_INDEX) {
			Wood w = wood();
			int lum = luminance(w.base());
			boolean lightWood = lum > 150;
			return new Style("Holz", w.base(), w.light(),
					lightWood ? 0xFF2A1A0A : 0xFFFBEED6, lightWood ? 0xFF6A5030 : 0xFFD9BD92,
					w.dark(), mix(w.dark(), 0xFF000000, 0.5f), mix(w.light(), 0xFFFFFFFF, 0.25f), w.dark(),
					true, 9, 0xFFE8A93A);
		}
		return STYLES[i];
	}

	static int luminance(int argb) {
		return (int) (0.299 * ((argb >> 16) & 0xFF) + 0.587 * ((argb >> 8) & 0xFF) + 0.114 * (argb & 0xFF));
	}

	/** Wendet die Menue-Helligkeit auf eine Farbe an (Alpha bleibt). */
	public static int shade(int argb) {
		float f = c().menuBrightness;
		if (Math.abs(f - 1f) < 0.005f) {
			return argb;
		}
		int a = argb >>> 24;
		int r = clampByte(((argb >> 16) & 0xFF) * f);
		int g = clampByte(((argb >> 8) & 0xFF) * f);
		int bl = clampByte((argb & 0xFF) * f);
		return (a << 24) | (r << 16) | (g << 8) | bl;
	}

	private static int clampByte(float v) {
		return Math.max(0, Math.min(255, Math.round(v)));
	}

	public static void selectStyle(int index) {
		SkyConfig c = c();
		c.style = index;
		int a = STYLES[index].accent();
		c.accentR = ((a >> 16) & 0xFF) / 255f;
		c.accentG = ((a >> 8) & 0xFF) / 255f;
		c.accentB = (a & 0xFF) / 255f;
	}

	public static int accent() {
		SkyConfig c = c();
		return 0xFF000000 | (b(c.accentR) << 16) | (b(c.accentG) << 8) | b(c.accentB);
	}

	public static int radius() {
		Style s = style();
		return (s.bevel() || !c().rounded) ? 0 : 4;
	}

	public static int panel() {
		int p = style().panel();
		int a = Math.round((p >>> 24) * c().panelOpacity);
		return shade((a << 24) | (p & 0xFFFFFF));
	}

	public static int surface() {
		return shade(style().surface());
	}

	public static int surfaceHover() {
		return mix(surface(), accent(), 0.3f);
	}

	public static int text() {
		return style().text();
	}

	public static int muted() {
		return style().muted();
	}

	public static int track() {
		return shade(style().track());
	}

	/** Lesbare Textfarbe auf der Akzentfarbe. */
	public static int onAccent() {
		int a = accent();
		double lum = 0.299 * ((a >> 16) & 0xFF) + 0.587 * ((a >> 8) & 0xFF) + 0.114 * (a & 0xFF);
		return lum > 150 ? 0xFF10131A : 0xFFFFFFFF;
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

	/** Knopf-/Flaechen-Optik: im Altmodisch-Stil mit Rand und Licht/Schatten, sonst flach und evtl. rund. */
	public static void box(DrawContext ctx, int x, int y, int w, int h, int fill) {
		Style s = style();
		if (s.bevel()) {
			int hi = shade(s.hi());
			int lo = shade(s.lo());
			ctx.fill(x, y, x + w, y + h, shade(s.border()));
			ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
			if (isWood()) {
				Tex.grain(ctx, x + 1, y + 1, w - 2, h - 2);
			}
			ctx.fill(x + 1, y + 1, x + w - 1, y + 2, hi);
			ctx.fill(x + 1, y + 1, x + 2, y + h - 1, hi);
			ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, lo);
			ctx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, lo);
		} else {
			rect(ctx, x, y, w, h, fill, radius());
		}
	}

	/** Das grosse Menue-Fenster. */
	public static void drawPanel(DrawContext ctx, int x, int y, int w, int h) {
		Style s = style();
		if (s.bevel()) {
			int hi = shade(s.hi());
			int lo = shade(s.lo());
			ctx.fill(x, y, x + w, y + h, shade(s.border()));
			ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, panel());
			Tex.draw(ctx, x + 1, y + 1, w - 2, h - 2);
			ctx.fill(x + 1, y + 1, x + w - 1, y + 3, hi);
			ctx.fill(x + 1, y + 1, x + 3, y + h - 1, hi);
			ctx.fill(x + 1, y + h - 3, x + w - 1, y + h - 1, lo);
			ctx.fill(x + w - 3, y + 1, x + w - 1, y + h - 1, lo);
		} else {
			int r = radius() + 2;
			rect(ctx, x, y, w, h, shade(s.border()), r);
			rect(ctx, x + 1, y + 1, w - 2, h - 2, panel(), Math.max(0, r - 1));
		}
	}
}
