package dev.skysettings;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SkySettingsScreen extends Screen {
	private enum Page {
		ALLGEMEIN("Allg."), ZEIT("Zeit"), HIMMEL("Himmel"), WOLKEN("Wolken"), NEBEL("Nebel"), LOOKS("Looks"), EFFEKT("Effekt"), DESIGN("Design");

		final String label;

		Page(String label) {
			this.label = label;
		}
	}

	private static final String[] MOON_NAMES = {
			"Auto", "Vollmond", "Abnehm. 3/4", "Letztes Viertel", "Abnehm. Sichel", "Neumond", "Zunehm. Sichel",
			"Erstes Viertel", "Zunehm. 3/4"
	};
	private static final String BRAND = "Lukaku´s Sky settings";

	private static Page page = Page.ALLGEMEIN;
	private static int looksTab = 0;
	private static int scrollY = 0;
	private static String profileName = "";

	private static final int PANEL_X = 10;
	private static final int PANEL_W = 360;
	private static final int PAD = 14;
	private static final int ROW_SLIDER = 26;
	private static final int ROW_TOGGLE = 22;
	private static final int ROW_BTN = 23;
	private static final int ROW_HEADER = 18;

	private final Screen parent;
	private int panelY;
	private int panelH;
	private int viewTop;
	private int viewBottom;
	private int totalH;
	private final List<Placed> placed = new ArrayList<>();

	public SkySettingsScreen(Screen parent) {
		super(Text.literal("Sky Settings"));
		this.parent = parent;
	}

	// ------------------------------------------------------------------ Zeilen-Aufbau

	private interface Factory {
		ClickableWidget make(int x, int y, int w, int h);
	}

	private record Row(int h, List<Factory> cells, int[] weights) {
	}

	private record Placed(ClickableWidget widget, int baseY, int h) {
	}

	private static Row full(int h, Factory f) {
		return new Row(h, List.of(f), new int[] {1});
	}

	private static Row pair(int h, Factory a, Factory b) {
		if (b == null) {
			return new Row(h, List.of(a), new int[] {1, 1});
		}
		return new Row(h, List.of(a, b), new int[] {1, 1});
	}

	private static Row weighted(int h, int wa, int wb, Factory a, Factory b) {
		return new Row(h, List.of(a, b), new int[] {wa, wb});
	}

	private Factory sl(String label, float min, float max, float cur, Consumer<Float> set, Function<Float, String> fmt) {
		return (x, y, w, h) -> new ThemedSlider(textRenderer, x, y, w, h, label, min, max, cur, set, fmt);
	}

	private Factory tg(String label, BooleanSupplier get, Consumer<Boolean> set) {
		return (x, y, w, h) -> new Toggle(textRenderer, x, y, w, h, label, get, set);
	}

	private Factory btn(Supplier<String> label, boolean primary, Runnable action) {
		return (x, y, w, h) -> new Action(textRenderer, x, y + 1, w, h - 3, label, primary, action);
	}

	private Factory hdr(String text) {
		return (x, y, w, h) -> new Header(textRenderer, x, y, w, h, text);
	}

	private Factory note(String text) {
		return (x, y, w, h) -> new Note(textRenderer, x, y, w, h, text);
	}

	private void tintRows(List<Row> rows, SkyConfig.Tint t) {
		rows.add(pair(ROW_SLIDER, sl("Stärke", 0f, 1f, t.strength, v -> t.strength = v, SkySettingsScreen::percent),
				sl("Rot", 0f, 1f, t.r, v -> t.r = v, SkySettingsScreen::byte255)));
		rows.add(pair(ROW_SLIDER, sl("Grün", 0f, 1f, t.g, v -> t.g = v, SkySettingsScreen::byte255),
				sl("Blau", 0f, 1f, t.b, v -> t.b = v, SkySettingsScreen::byte255)));
		SkyConfig c = SkyConfig.get();
		rows.add(pair(ROW_TOGGLE, tg("Regenbogen", () -> t.rainbow, v -> {
			t.rainbow = v;
			if (v && t.strength < 0.05f) {
				t.strength = 0.7f;
			}
			clearAndInit();
		}), null));
		rows.add(full(ROW_SLIDER, sl("Regenbogen-Tempo", 0.1f, 10f, c.rainbowSpeed, v -> c.rainbowSpeed = v,
				v -> String.format(Locale.ROOT, "%.1fx", v))));
	}

	private List<Row> buildRows() {
		SkyConfig c = SkyConfig.get();
		List<Row> rows = new ArrayList<>();
		switch (page) {
			case ALLGEMEIN -> {
				rows.add(full(ROW_TOGGLE, tg("Sky Settings aktiv", () -> c.enabled, v -> c.enabled = v)));
				rows.add(pair(ROW_SLIDER, sl("Helligkeit", 0f, 2f, c.skyBrightness, v -> c.skyBrightness = v, SkySettingsScreen::percent),
						sl("Sättigung", 0f, 2f, c.saturation, v -> c.saturation = v, SkySettingsScreen::percent)));
				rows.add(full(ROW_SLIDER, sl("Sterne", 0f, 1f, c.starBrightness, v -> c.starBrightness = v, SkySettingsScreen::percent)));
				rows.add(full(ROW_TOGGLE, tg("Eigene Uhrzeit", () -> c.customTime, v -> {
					c.customTime = v;
					c.restartClock();
				})));
				rows.add(full(ROW_BTN, btn(() -> c.lang.equals("en") ? "Language: English" : "Sprache: Deutsch", false, () -> {
					c.lang = c.lang.equals("en") ? "de" : "en";
					clearAndInit();
				})));
				rows.add(full(ROW_HEADER, hdr("Tipp")));
				rows.add(full(ROW_HEADER, note("Fertige Himmel findest du unter \"Looks\".")));
				rows.add(full(ROW_HEADER, note("Alles ist nur bei dir sichtbar.")));
			}
			case ZEIT -> {
				rows.add(full(ROW_TOGGLE, tg("Eigene Uhrzeit", () -> c.customTime, v -> {
					c.customTime = v;
					c.restartClock();
				})));
				rows.add(full(ROW_SLIDER, sl("Uhrzeit", 0f, 24f, c.clockHours, v -> {
					c.clockHours = v;
					c.restartClock();
				}, SkySettingsScreen::clock)));
				rows.add(pair(ROW_BTN, btn(() -> "Morgen 06:00", false, () -> setHour(6f)), btn(() -> "Mittag 12:00", false, () -> setHour(12f))));
				rows.add(pair(ROW_BTN, btn(() -> "Abend 18:00", false, () -> setHour(18f)), btn(() -> "Mitternacht 00:00", false, () -> setHour(0f))));
				rows.add(full(ROW_SLIDER, sl("Tempo (0 = Uhrzeit steht still)", 0f, 30f, c.timeSpeed, v -> {
					c.timeSpeed = v;
					c.restartClock();
				}, v -> v < 0.05f ? Lang.tr("fest") : String.format(Locale.ROOT, "%.1fx", v))));
				rows.add(full(ROW_HEADER, hdr("Mond")));
				rows.add(full(ROW_SLIDER, sl("Mondphase", 0f, 8f, c.moonPhase, v -> c.moonPhase = Math.round(v),
						v -> Lang.tr(MOON_NAMES[Math.max(0, Math.min(8, Math.round(v)))]))));
			}
			case HIMMEL -> {
				rows.add(full(ROW_HEADER, hdr("Himmelsfarbe")));
				tintRows(rows, c.skyTint);
				rows.add(full(ROW_HEADER, hdr("Dämmerung (Sonnenauf-/untergang)")));
				tintRows(rows, c.dawnTint);
			}
			case WOLKEN -> {
				rows.add(full(ROW_HEADER, hdr("Wolkenfarbe")));
				tintRows(rows, c.cloudTint);
				rows.add(pair(ROW_SLIDER, sl("Deckkraft", 0f, 1f, c.cloudOpacity, v -> c.cloudOpacity = v, SkySettingsScreen::percent),
						sl("Helligkeit", 0f, 2f, c.cloudBrightness, v -> c.cloudBrightness = v, SkySettingsScreen::percent)));
				rows.add(full(ROW_SLIDER, sl("Höhe", -64f, 128f, c.cloudHeightOffset, v -> c.cloudHeightOffset = v, SkySettingsScreen::blocks)));
			}
			case NEBEL -> {
				rows.add(full(ROW_HEADER, hdr("Nebelfarbe")));
				tintRows(rows, c.fogTint);
				rows.add(full(ROW_SLIDER, sl("Dichte (0 = kein Nebel)", 0f, 3f, c.fogDensity, v -> c.fogDensity = v, SkySettingsScreen::percent)));
				rows.add(full(ROW_TOGGLE, tg("Eigener Nebel-Beginn", () -> c.customFogStart, v -> c.customFogStart = v)));
				rows.add(full(ROW_SLIDER, sl("Nebel beginnt ab", 0f, 512f, c.fogStartBlocks, v -> c.fogStartBlocks = Math.round(v),
						v -> Math.round(v) + " " + Lang.tr("Blöcke"))));
			}
			case LOOKS -> looksRows(rows);
			case EFFEKT -> {
				rows.add(full(ROW_TOGGLE, tg("Effekte aktiv", () -> c.effectsEnabled, v -> c.effectsEnabled = v)));
				rows.add(full(ROW_HEADER, hdr("Himmelseffekte")));
				for (int i = 1; i < Effects.NAMES.length; i++) {
					final int id = i;
					SkyConfig.Fx f = c.fx[id];
					rows.add(full(ROW_TOGGLE, tg(Effects.NAMES[id], () -> f.on, v -> {
						f.on = v;
						clearAndInit();
					})));
					if (f.on) {
						rows.add(pair(ROW_SLIDER,
								sl("Effekt-Stärke", 0f, 2f, f.strength, v -> f.strength = v, SkySettingsScreen::percent),
								sl("Effekt-Tempo", 0.3f, 3f, f.speed, v -> f.speed = v, SkySettingsScreen::percent)));
						if (id == Effects.STORM) {
							rows.add(full(ROW_TOGGLE, tg("Echte Blitze (Gewitter)", () -> c.lightningBolts, v -> c.lightningBolts = v)));
							rows.add(pair(ROW_SLIDER,
									sl("Blitz-Rhythmus", 2f, 30f, c.boltInterval, v -> c.boltInterval = Math.round(v),
											v -> Lang.tr("alle") + " " + Math.round(v) + " s"),
									sl("Blitz-Reichweite", 20f, 150f, c.boltDistance, v -> c.boltDistance = Math.round(v),
											v -> Math.round(v) + " " + Lang.tr("Blöcke"))));
						}
					}
				}
			}
			case DESIGN -> {
				rows.add(full(ROW_HEADER, hdr("Akzentfarbe")));
				rows.add(full(ROW_SLIDER, (x, y, w, h) -> new Swatches(textRenderer, x, y, w, h, this::clearAndInit)));
				rows.add(pair(ROW_SLIDER, sl("Rot", 0f, 1f, c.accentR, v -> c.accentR = v, SkySettingsScreen::byte255),
						sl("Grün", 0f, 1f, c.accentG, v -> c.accentG = v, SkySettingsScreen::byte255)));
				rows.add(pair(ROW_SLIDER, sl("Blau", 0f, 1f, c.accentB, v -> c.accentB = v, SkySettingsScreen::byte255),
						sl("Menü-Deckkraft", 0.3f, 1f, c.panelOpacity, v -> c.panelOpacity = v, SkySettingsScreen::percent)));
				rows.add(full(ROW_HEADER, hdr("Stil")));
				for (int i = 0; i < Theme.STYLES.length; i += 2) {
					final int a = i;
					final int b = i + 1;
					rows.add(pair(ROW_BTN,
							btn(() -> Theme.STYLES[a].name(), c.style == a, () -> {
								Theme.selectStyle(a);
								clearAndInit();
							}),
							b < Theme.STYLES.length ? btn(() -> Theme.STYLES[b].name(), c.style == b, () -> {
								Theme.selectStyle(b);
								clearAndInit();
							}) : null));
				}
				rows.add(pair(ROW_TOGGLE, tg("Runde Ecken", () -> c.roundCorners, v -> c.roundCorners = v),
						tg("Animationen", () -> c.animations, v -> c.animations = v)));
				if (Theme.isWood()) {
					rows.add(full(ROW_HEADER, hdr("Holzsorte")));
					for (int i = 0; i < Theme.WOODS.length; i += 2) {
						final int a = i;
						final int b = i + 1;
						rows.add(pair(ROW_BTN,
								btn(() -> Theme.WOODS[a].name(), c.woodType == a, () -> {
									c.woodType = a;
									clearAndInit();
								}),
								b < Theme.WOODS.length ? btn(() -> Theme.WOODS[b].name(), c.woodType == b, () -> {
									c.woodType = b;
									clearAndInit();
								}) : null));
					}
				}
				rows.add(pair(ROW_SLIDER, sl("Menü-Helligkeit", 0.5f, 1.5f, c.menuBrightness, v -> c.menuBrightness = v, SkySettingsScreen::percent),
						sl("Animations-Tempo", 0.2f, 3f, c.animSpeed, v -> c.animSpeed = v, SkySettingsScreen::percent)));
			}
		}
		return rows;
	}

	private void looksRows(List<Row> rows) {
		rows.add(pair(ROW_BTN, btn(() -> "Vorlagen", looksTab == 0, () -> {
			looksTab = 0;
			scrollY = 0;
			clearAndInit();
		}), btn(() -> "Meine Profile", looksTab == 1, () -> {
			looksTab = 1;
			scrollY = 0;
			clearAndInit();
		})));

		if (looksTab == 0) {
			rows.add(full(ROW_HEADER, hdr("Fertige Himmel – Klick zum Anwenden")));
			SkyConfig cfg = SkyConfig.get();
			String active = cfg.activeLook;
			Looks.Look activeLook = null;
			for (Looks.Look l : Looks.ALL) {
				if (l.name().equals(active)) {
					activeLook = l;
				}
			}
			final String status = activeLook == null ? "-" : Lang.tr(activeLook.name())
					+ (Looks.matches(activeLook) ? "" : " (" + Lang.tr("geändert") + ")");
			rows.add(full(ROW_HEADER, note(Lang.tr("Aktiv") + ": " + status)));
			List<Looks.Look> all = Looks.ALL;
			for (int i = 0; i < all.size(); i += 2) {
				Looks.Look a = all.get(i);
				Looks.Look b = i + 1 < all.size() ? all.get(i + 1) : null;
				rows.add(pair(ROW_BTN,
						btn(() -> (a.name().equals(active) ? "» " : "") + Lang.tr(a.name()), a.name().equals(active), () -> {
							Looks.apply(a);
							clearAndInit();
						}),
						b == null ? null : btn(() -> (b.name().equals(active) ? "» " : "") + Lang.tr(b.name()), b.name().equals(active), () -> {
							Looks.apply(b);
							clearAndInit();
						})));
			}
		} else {
			rows.add(full(ROW_HEADER, hdr("Aktuellen Himmel speichern")));
			rows.add(full(ROW_BTN, (x, y, w, h) -> {
				TextFieldWidget field = new TextFieldWidget(textRenderer, x, y + 1, w, h - 3, Text.literal("Name"));
				field.setMaxLength(24);
				field.setText(profileName);
				field.setPlaceholder(Text.literal("Profilname eingeben ..."));
				field.setChangedListener(s -> profileName = s);
				return field;
			}));
			rows.add(full(ROW_BTN, btn(() -> "Speichern", true, () -> {
				String name = profileName.trim();
				if (name.isEmpty()) {
					name = "Profil " + (Profiles.names().size() + 1);
				}
				Profiles.put(name);
				profileName = "";
				clearAndInit();
			})));
			rows.add(full(ROW_HEADER, hdr("Gespeichert – Klick zum Laden")));
			List<String> names = Profiles.names();
			if (names.isEmpty()) {
				rows.add(full(ROW_HEADER, note("Noch keine Profile gespeichert.")));
			}
			for (String name : names) {
				boolean isActive = name.equals(SkyConfig.get().activeProfile);
				rows.add(weighted(ROW_BTN, 3, 1,
						btn(() -> (isActive ? "» " : "") + name + (isActive && !Profiles.matches(name) ? " *" : ""), isActive, () -> {
							Profiles.apply(name);
							clearAndInit();
						}),
						btn(() -> "Löschen", false, () -> {
							Profiles.delete(name);
							clearAndInit();
						})));
			}
		}
	}

	private void setHour(float h) {
		SkyConfig c = SkyConfig.get();
		c.customTime = true;
		c.clockHours = h;
		c.restartClock();
		clearAndInit();
	}

	// ------------------------------------------------------------------ Aufbau

	@Override
	protected void init() {
		SkyConfig c = SkyConfig.get();
		placed.clear();
		panelY = 10;
		panelH = height - 20;

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
				scrollY = 0;
				clearAndInit();
			}));
		}

		viewTop = tabY + 26;
		viewBottom = panelY + panelH - 38;
		int rightGap = 8; // Platz fuer Scrollleiste
		int contentW = w - rightGap;

		List<Row> rows = buildRows();
		totalH = 0;
		for (Row r : rows) {
			totalH += r.h();
		}
		scrollY = Math.max(0, Math.min(scrollY, maxScroll()));

		int y = viewTop;
		for (Row r : rows) {
			int n = r.cells().size();
			int gap = 6;
			int cols = r.weights().length;
			int totalWeight = 0;
			for (int wt : r.weights()) {
				totalWeight += wt;
			}
			int avail = contentW - gap * (cols - 1);
			int cx = x;
			for (int i = 0; i < n; i++) {
				int cw = avail * r.weights()[i] / totalWeight;
				ClickableWidget widget = r.cells().get(i).make(cx, y, cw, r.h());
				placed.add(new Placed(widget, y, r.h()));
				addDrawableChild(widget);
				cx += cw + gap;
			}
			y += r.h();
		}
		applyScroll();

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

	private int maxScroll() {
		return Math.max(0, totalH - (viewBottom - viewTop));
	}

	private void applyScroll() {
		for (Placed p : placed) {
			int y = p.baseY() - scrollY;
			p.widget().setY(y);
			p.widget().visible = y >= viewTop && y + p.h() <= viewBottom;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (maxScroll() > 0) {
			scrollY = Math.max(0, Math.min(maxScroll(), scrollY - (int) (verticalAmount * 26)));
			applyScroll();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private static String percent(float v) {
		return Math.round(v * 100f) + "%";
	}

	private static String byte255(float v) {
		return String.valueOf(Math.round(v * 255f));
	}

	private static String blocks(float v) {
		int r = Math.round(v);
		return (r > 0 ? "+" : "") + r + " " + Lang.tr("Blöcke");
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
		SkyConfig c = SkyConfig.get();
		Theme.drawPanel(ctx, PANEL_X, panelY, PANEL_W, panelH);

		if (c.animations) {
			float t = (float) ((System.nanoTime() / 1_000_000_000.0 * c.animSpeed) % 10000.0);
			Anim.draw(ctx, PANEL_X, panelY, PANEL_W, panelH, Theme.style().anim(), t, Theme.accent());
		}

		// Kopfzeile
		int accent = Theme.accent();
		ctx.fill(PANEL_X + PAD, panelY + 12, PANEL_X + PAD + 4, panelY + 26, accent);
		ctx.drawText(textRenderer, BRAND, PANEL_X + (PANEL_W - textRenderer.getWidth(BRAND)) / 2, panelY + 15, Theme.text(), false);
		ctx.drawText(textRenderer, "/sky", PANEL_X + PANEL_W - PAD - textRenderer.getWidth("/sky"), panelY + 15, Theme.muted(), false);
		ctx.fill(PANEL_X + PAD, panelY + 29, PANEL_X + PANEL_W - PAD, panelY + 30, Theme.argb(accent & 0xFFFFFF, 0.7f));

		super.render(ctx, mouseX, mouseY, deltaTicks);

		// Scrollleiste
		if (maxScroll() > 0) {
			int sx = PANEL_X + PANEL_W - PAD + 1;
			int trackH = viewBottom - viewTop;
			int thumbH = Math.max(16, trackH * trackH / totalH);
			int thumbY = viewTop + (trackH - thumbH) * scrollY / maxScroll();
			ctx.fill(sx, viewTop, sx + 3, viewBottom, Theme.track());
			ctx.fill(sx, thumbY, sx + 3, thumbY + thumbH, accent);
		}

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

	private static final class Header extends Base {
		private final String text;

		Header(TextRenderer font, int x, int y, int w, int h, String text) {
			super(font, x, y, w, h);
			this.text = text;
			this.active = false;
		}

		@Override
		protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
			String text = Lang.tr(this.text);
			int tw = font.getWidth(text);
			ctx.drawText(font, text, getX(), getY() + 5, Theme.accent(), false);
			int lx = getX() + tw + 6;
			if (lx < getX() + getWidth()) {
				ctx.fill(lx, getY() + 9, getX() + getWidth(), getY() + 10, Theme.argb(Theme.accent() & 0xFFFFFF, 0.45f));
			}
		}
	}

	private static final class Note extends Base {
		private final String text;

		Note(TextRenderer font, int x, int y, int w, int h, String text) {
			super(font, x, y, w, h);
			this.text = text;
			this.active = false;
		}

		@Override
		protected void renderWidget(DrawContext ctx, int mx, int my, float delta) {
			ctx.drawText(font, Lang.tr(text), getX(), getY() + 5, Theme.muted(), false);
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
			Theme.box(ctx, getX(), getY(), getWidth(), getHeight(), bg);
			int col = selected ? Theme.onAccent() : Theme.text();
			String shown = Lang.tr(label);
			ctx.drawText(font, shown, getX() + (getWidth() - font.getWidth(shown)) / 2, getY() + (getHeight() - 8) / 2, col, false);
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
			ctx.drawText(font, Lang.tr(label), getX(), cy - 4, isHovered() ? Theme.accent() : Theme.text(), false);
			int sw = 26, sh = 12;
			int sx = getX() + getWidth() - sw;
			int sy = cy - sh / 2;
			Theme.box(ctx, sx, sy, sw, sh, on ? Theme.accent() : Theme.track());
			int kx = on ? sx + sw - sh + 1 : sx + 1;
			Theme.box(ctx, kx, sy + 1, sh - 2, sh - 2, on ? 0xFFFFFFFF : Theme.surface());
		}
	}

	private static final class Action extends Base {
		private final Supplier<String> label;
		private final boolean primary;
		private final Runnable action;

		Action(TextRenderer font, int x, int y, int w, int h, Supplier<String> label, boolean primary, Runnable action) {
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
			Theme.box(ctx, getX(), getY(), getWidth(), getHeight(), bg);
			String text = Lang.tr(label.get());
			int col = primary ? Theme.onAccent() : Theme.text();
			ctx.drawText(font, text, getX() + (getWidth() - font.getWidth(text)) / 2, getY() + (getHeight() - 8) / 2, col, false);
		}
	}

	/** Reihe mit fertigen Akzentfarben zum Anklicken. */
	private static final class Swatches extends Base {
		private static final int SIZE = 18;
		private final Runnable refresh;

		Swatches(TextRenderer font, int x, int y, int w, int h, Runnable refresh) {
			super(font, x, y, w, h);
			this.refresh = refresh;
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
				refresh.run();
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
					ctx.fill(sx - 2, sy - 2, sx + SIZE + 2, sy + SIZE + 2, selected ? Theme.text() : Theme.muted());
				}
				Theme.box(ctx, sx, sy, SIZE, SIZE, 0xFF000000 | rgb);
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
			String v = formatter.apply(current());
			int vw = font.getWidth(v);
			String text = Lang.tr(label);
			while (text.length() > 3 && font.getWidth(text) + vw + 8 > w) {
				text = text.substring(0, text.length() - 1);
			}
			ctx.drawText(font, text, x, y, isHovered() ? Theme.accent() : Theme.text(), false);
			ctx.drawText(font, v, x + w - vw, y, Theme.muted(), false);

			int ty = getY() + getHeight() - 9;
			Theme.box(ctx, x, ty, w, 5, Theme.track());
			int fillW = Math.max(0, Math.round((float) ((w - 2) * value)));
			if (fillW > 0) {
				ctx.fill(x + 1, ty + 1, x + 1 + fillW, ty + 4, Theme.accent());
			}
			int kx = x + Math.max(0, Math.min(w - 6, fillW - 2));
			Theme.box(ctx, kx, ty - 2, 6, 9, isHovered() ? 0xFFFFFFFF : Theme.mix(Theme.surface(), 0xFFFFFFFF, 0.5f));
		}
	}
}
