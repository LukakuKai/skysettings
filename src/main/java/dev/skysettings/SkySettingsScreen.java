package dev.skysettings;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class SkySettingsScreen extends Screen {
	private enum Page {
		ALLGEMEIN("Allg."), ZEIT("Zeit"), HIMMEL("Himmel"), WOLKEN("Wolken"), NEBEL("Nebel"), DESIGN("Design");

		final String label;

		Page(String label) {
			this.label = label;
		}
	}

	private static Page page = Page.ALLGEMEIN;

	private static final int PANEL_X = 12;
	private static final int PANEL_W = 300;
	private static final int PAD = 14;

	private final Screen parent;
	private int panelY;
	private int panelH;

	public SkySettingsScreen(Screen parent) {
		super(Text.literal("Sky Settings"));
		this.parent = parent;
	}

	// ------------------------------------------------------------------ Aufbau

	@Override
	protected void init() {
		SkyConfig c = SkyConfig.get();
		panelY = 12;
		panelH = height - 24;

		int x = PANEL_X + PAD;
		int w = PANEL_W - 2 * PAD;

		// Tabs
		Page[] pages = Page.values();
		int tabGap = 3;
		int tabW = (w - tabGap * (pages.length - 1)) / pages.length;
		int tabY = panelY + 34;
		for (int i = 0; i < pages.length; i++) {
			Page p = pages[i];
			addDrawableChild(new TabButton(textRenderer, x + i * (tabW + tabGap), tabY, tabW, 18, p.label, p == page, () -> {
				page = p;
				clearAndInit();
			}));
		}

		int top = tabY + 28;
		int bottom = panelY + panelH - 40;
		int rows = rowsOf(page);
		int rowH = Math.max(22, Math.min(30, (bottom - top) / Math.max(1, rows)));
		int y = top;

		switch (page) {
			case ALLGEMEIN -> {
				addDrawableChild(new Toggle(textRenderer, x, y, w, rowH, "Sky Settings aktiv", () -> c.enabled, v -> c.enabled = v));
				y += rowH;
				y = slider(x, y, w, rowH, "Helligkeit", 0f, 2f, c.skyBrightness, v -> c.skyBrightness = v, SkySettingsScreen::percent);
				y = slider(x, y, w, rowH, "Sättigung", 0f, 2f, c.saturation, v -> c.saturation = v, SkySettingsScreen::percent);
				y = slider(x, y, w, rowH, "Sterne", 0f, 1f, c.starBrightness, v -> c.starBrightness = v, SkySettingsScreen::percent);
			}
			case ZEIT -> {
				addDrawableChild(new Toggle(textRenderer, x, y, w, rowH, "Eigene Uhrzeit", () -> c.customTime, v -> c.customTime = v));
				y += rowH;
				y = slider(x, y, w, rowH, "Uhrzeit", 0f, 24f, c.clockHours, v -> c.clockHours = v, SkySettingsScreen::clock);
			}
			case HIMMEL -> tint(x, y, w, rowH, c.skyTint);
			case WOLKEN -> {
				y = tint(x, y, w, rowH, c.cloudTint);
				y = slider(x, y, w, rowH, "Deckkraft", 0f, 1f, c.cloudOpacity, v -> c.cloudOpacity = v, SkySettingsScreen::percent);
				y = slider(x, y, w, rowH, "Helligkeit", 0f, 2f, c.cloudBrightness, v -> c.cloudBrightness = v, SkySettingsScreen::percent);
				y = slider(x, y, w, rowH, "Höhe", -64f, 128f, c.cloudHeightOffset, v -> c.cloudHeightOffset = v, SkySettingsScreen::blocks);
			}
			case NEBEL -> {
				y = tint(x, y, w, rowH, c.fogTint);
				y = slider(x, y, w, rowH, "Dichte", 0f, 3f, c.fogDensity, v -> c.fogDensity = v, SkySettingsScreen::percent);
			}
			case DESIGN -> {
				addDrawableChild(new Swatches(textRenderer, x, y, w, rowH));
				y += rowH;
				addDrawableChild(new Action(textRenderer, x, y + 1, w, rowH - 4, () -> "Stil: " + Theme.STYLE_NAMES[c.style], false, () -> {
					c.style = (c.style + 1) % Theme.STYLE_NAMES.length;
				}));
				y += rowH;
				addDrawableChild(new Toggle(textRenderer, x, y, w, rowH, "Runde Ecken", () -> c.rounded, v -> c.rounded = v));
				y += rowH;
				y = slider(x, y, w, rowH, "Akzent Rot", 0f, 1f, c.accentR, v -> c.accentR = v, SkySettingsScreen::byte255);
				y = slider(x, y, w, rowH, "Akzent Grün", 0f, 1f, c.accentG, v -> c.accentG = v, SkySettingsScreen::byte255);
				y = slider(x, y, w, rowH, "Akzent Blau", 0f, 1f, c.accentB, v -> c.accentB = v, SkySettingsScreen::byte255);
				y = slider(x, y, w, rowH, "Menü-Deckkraft", 0.3f, 1f, c.panelOpacity, v -> c.panelOpacity = v, SkySettingsScreen::percent);
			}
		}

		// Fusszeile
		int by = panelY + panelH - 30;
		int half = (w - 6) / 2;
		boolean design = page == Page.DESIGN;
		addDrawableChild(new Action(textRenderer, x, by, half, 20,
				() -> design ? "Design zurücksetzen" : "Himmel zurücksetzen", false, () -> {
					if (design) {
						c.resetTheme();
					} else {
						c.reset();
					}
					clearAndInit();
				}));
		addDrawableChild(new Action(textRenderer, x + half + 6, by, half, 20, () -> "Fertig", true, this::close));
	}

	private static int rowsOf(Page p) {
		return switch (p) {
			case ALLGEMEIN -> 4;
			case ZEIT -> 2;
			case HIMMEL -> 4;
			case WOLKEN -> 7;
			case NEBEL -> 5;
			case DESIGN -> 8;
		};
	}

	private int slider(int x, int y, int w, int rowH, String label, float min, float max, float cur,
			Consumer<Float> setter, Function<Float, String> fmt) {
		addDrawableChild(new ThemedSlider(textRenderer, x, y, w, rowH, label, min, max, cur, setter, fmt));
		return y + rowH;
	}

	private int tint(int x, int y, int w, int rowH, SkyConfig.Tint t) {
		y = slider(x, y, w, rowH, "Farbstärke (0 = Vanilla)", 0f, 1f, t.strength, v -> t.strength = v, SkySettingsScreen::percent);
		y = slider(x, y, w, rowH, "Rot", 0f, 1f, t.r, v -> t.r = v, SkySettingsScreen::byte255);
		y = slider(x, y, w, rowH, "Grün", 0f, 1f, t.g, v -> t.g = v, SkySettingsScreen::byte255);
		y = slider(x, y, w, rowH, "Blau", 0f, 1f, t.b, v -> t.b = v, SkySettingsScreen::byte255);
		return y;
	}

	private static String percent(float v) {
		return Math.round(v * 100f) + "%";
	}

	private static String byte255(float v) {
		return String.valueOf(Math.round(v * 255f));
	}

	private static String blocks(float v) {
		int r = Math.round(v);
		return (r > 0 ? "+" : "") + r + " Blöcke";
	}

	private static String clock(float h) {
		int total = Math.round(h * 60f) % (24 * 60);
		return String.format(Locale.ROOT, "%02d:%02d", total / 60, total % 60);
	}

	// ------------------------------------------------------------------ Zeichnen

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		// Bewusst leer: kein Weichzeichner, damit man den Himmel live beurteilen kann.
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float deltaTicks) {
		int r = Theme.radius() + 2;
		Theme.framed(ctx, PANEL_X, panelY, PANEL_W, panelH, Theme.panel(), Theme.panelBorder(), r);

		// Kopfzeile
		int accent = Theme.accent();
		Theme.rect(ctx, PANEL_X + PAD, panelY + 12, 4, 14, accent, Theme.radius() > 0 ? 2 : 0);
		ctx.drawText(textRenderer, "Sky Settings", PANEL_X + PAD + 10, panelY + 11, Theme.text(), false);
		ctx.drawText(textRenderer, "/sky", PANEL_X + PANEL_W - PAD - textRenderer.getWidth("/sky"), panelY + 12, Theme.muted(), false);
		ctx.fill(PANEL_X + PAD, panelY + 29, PANEL_X + PANEL_W - PAD, panelY + 30, Theme.argb(accent & 0xFFFFFF, 0.6f));

		super.render(ctx, mouseX, mouseY, deltaTicks);
	}

	@Override
	public void removed() {
		SkyConfig.save();
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	// ------------------------------------------------------------------ Widgets

	private abstract static class Base extends ClickableWidget {
		protected final TextRenderer font;

		Base(TextRenderer font, int x, int y, int w, int h) {
			super(x, y, w, h, Text.empty());
			this.font = font;
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {
			appendDefaultNarrations(builder);
		}
	}

	private static final class TabButton extends Base {
		private final String label;
		private final boolean selected;
		private final Runnable action;

		TabButton(TextRenderer font, int x, int y, int w, int h, String label, boolean selected, Runnable action) {
			super(font, x, y, w, h);
			this.label = label;
			this.selected = selected;
			this.action = action;
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			action.run();
		}

		@Override
		protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
			int bg = selected ? Theme.accent() : (isHovered() ? Theme.surfaceHover() : Theme.surface());
			Theme.rect(ctx, getX(), getY(), getWidth(), getHeight(), bg, Theme.radius());
			int col = selected ? Theme.onAccent() : Theme.text();
			ctx.drawText(font, label, getX() + (getWidth() - font.getWidth(label)) / 2, getY() + (getHeight() - 8) / 2, col, false);
		}
	}

	private static final class Toggle extends Base {
		private final String label;
		private final BooleanSupplier get;
		private final Consumer<Boolean> set;

		Toggle(TextRenderer font, int x, int y, int w, int h, String label, BooleanSupplier get, Consumer<Boolean> set) {
			super(font, x, y, w, h);
			this.label = label;
			this.get = get;
			this.set = set;
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			set.accept(!get.getAsBoolean());
		}

		@Override
		protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
			boolean on = get.getAsBoolean();
			int cy = getY() + getHeight() / 2;
			ctx.drawText(font, label, getX(), cy - 4, isHovered() ? Theme.accent() : Theme.text(), false);
			int sw = 28, sh = 12;
			int sx = getX() + getWidth() - sw;
			int sy = cy - sh / 2;
			Theme.rect(ctx, sx, sy, sw, sh, on ? Theme.accent() : Theme.track(), 6);
			int kx = on ? sx + sw - sh + 2 : sx + 2;
			Theme.rect(ctx, kx, sy + 2, sh - 4, sh - 4, 0xFFFFFFFF, 4);
		}
	}

	private static final class Action extends Base {
		private final java.util.function.Supplier<String> label;
		private final boolean primary;
		private final Runnable action;

		Action(TextRenderer font, int x, int y, int w, int h, java.util.function.Supplier<String> label, boolean primary, Runnable action) {
			super(font, x, y, w, h);
			this.label = label;
			this.primary = primary;
			this.action = action;
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			action.run();
		}

		@Override
		protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
			int bg = primary ? (isHovered() ? Theme.mix(Theme.accent(), 0xFFFFFFFF, 0.2f) : Theme.accent())
					: (isHovered() ? Theme.surfaceHover() : Theme.surface());
			Theme.rect(ctx, getX(), getY(), getWidth(), getHeight(), bg, Theme.radius());
			String text = label.get();
			int col = primary ? Theme.onAccent() : Theme.text();
			ctx.drawText(font, text, getX() + (getWidth() - font.getWidth(text)) / 2, getY() + (getHeight() - 8) / 2, col, false);
		}
	}

	/** Reihe mit fertigen Akzentfarben zum Anklicken. */
	private static final class Swatches extends Base {
		private static final int SIZE = 20;

		Swatches(TextRenderer font, int x, int y, int w, int h) {
			super(font, x, y, w, h);
		}

		private int gap() {
			int n = Theme.PRESETS.length;
			return Math.max(2, (getWidth() - n * SIZE) / (n - 1));
		}

		private int index(double mx, double my) {
			int sy = getY() + (getHeight() - SIZE) / 2;
			if (my < sy || my > sy + SIZE) {
				return -1;
			}
			for (int i = 0; i < Theme.PRESETS.length; i++) {
				int sx = getX() + i * (SIZE + gap());
				if (mx >= sx && mx <= sx + SIZE) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			int i = index(click.x(), click.y());
			if (i >= 0) {
				int rgb = Theme.PRESETS[i];
				SkyConfig c = SkyConfig.get();
				c.accentR = ((rgb >> 16) & 0xFF) / 255f;
				c.accentG = ((rgb >> 8) & 0xFF) / 255f;
				c.accentB = (rgb & 0xFF) / 255f;
				net.minecraft.client.MinecraftClient.getInstance().currentScreen.clearAndInit();
			}
		}

		@Override
		protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
			int sy = getY() + (getHeight() - SIZE) / 2;
			int current = Theme.accent() & 0xFFFFFF;
			for (int i = 0; i < Theme.PRESETS.length; i++) {
				int sx = getX() + i * (SIZE + gap());
				int rgb = Theme.PRESETS[i];
				boolean selected = (rgb & 0xFFFFFF) == current;
				if (selected || index(mx, my) == i) {
					Theme.rect(ctx, sx - 2, sy - 2, SIZE + 4, SIZE + 4, selected ? Theme.text() : Theme.muted(), Theme.radius() + 1);
				}
				Theme.rect(ctx, sx, sy, SIZE, SIZE, 0xFF000000 | rgb, Theme.radius());
			}
		}
	}

	private static final class ThemedSlider extends SliderWidget {
		private final TextRenderer font;
		private final String label;
		private final float min;
		private final float max;
		private final Consumer<Float> setter;
		private final Function<Float, String> formatter;

		ThemedSlider(TextRenderer font, int x, int y, int w, int h, String label, float min, float max, float initial,
				Consumer<Float> setter, Function<Float, String> formatter) {
			super(x, y, w, h, Text.empty(), (double) ((initial - min) / (max - min)));
			this.font = font;
			this.label = label;
			this.min = min;
			this.max = max;
			this.setter = setter;
			this.formatter = formatter;
		}

		private float current() {
			return (float) (min + (max - min) * value);
		}

		@Override
		protected void updateMessage() {
		}

		@Override
		protected void applyValue() {
			setter.accept(current());
		}

		@Override
		public void renderWidget(DrawContext ctx, int mx, int my, float delta) {
			if (label == null) {
				return;
			}
			int x = getX();
			int w = getWidth();
			int y = getY() + 2;
			ctx.drawText(font, label, x, y, isHovered() ? Theme.accent() : Theme.text(), false);
			String v = formatter.apply(current());
			ctx.drawText(font, v, x + w - font.getWidth(v), y, Theme.muted(), false);

			int ty = getY() + getHeight() - 8;
			Theme.rect(ctx, x, ty, w, 4, Theme.track(), 2);
			int fillW = Math.max(0, Math.round((float) (w * value)));
			if (fillW > 0) {
				Theme.rect(ctx, x, ty, fillW, 4, Theme.accent(), 2);
			}
			int kx = x + Math.max(0, Math.min(w - 8, fillW - 4));
			Theme.rect(ctx, kx, ty - 2, 8, 8, isHovered() ? 0xFFFFFFFF : Theme.mix(Theme.accent(), 0xFFFFFFFF, 0.55f), 4);
		}
	}
}
