package dev.skysettings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class SkySettingsScreen extends Screen {
	private enum Page { ALLGEMEIN, ZEIT, HIMMEL, WOLKEN, NEBEL }

	private static Page page = Page.ALLGEMEIN;
	private static final int WIDTH = 250;
	private static final int HEIGHT = 20;
	private static final int GAP = 24;
	private static final int LEFT = 20;

	private final Screen parent;

	public SkySettingsScreen(Screen parent) {
		super(Text.literal("Sky Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		SkyConfig c = SkyConfig.get();
		int x = LEFT;
		int y = Math.max(30, height / 2 - 100);

		Page[] pages = Page.values();
		int tabW = WIDTH / pages.length;
		for (int i = 0; i < pages.length; i++) {
			Page p = pages[i];
			ButtonWidget tab = ButtonWidget.builder(Text.literal(tabName(p)), button -> {
				page = p;
				clearAndInit();
			}).dimensions(x + i * tabW, y, tabW - 2, HEIGHT).build();
			tab.active = p != page;
			addDrawableChild(tab);
		}
		y += GAP + 6;

		switch (page) {
			case ALLGEMEIN -> {
				addDrawableChild(toggle(x, y, "Sky Settings", () -> c.enabled, v -> c.enabled = v));
				y += GAP;
				addDrawableChild(slider(x, y, "Helligkeit", 0f, 2f, c.skyBrightness, v -> c.skyBrightness = v, SkySettingsScreen::percent));
				y += GAP;
				addDrawableChild(slider(x, y, "Sättigung", 0f, 2f, c.saturation, v -> c.saturation = v, SkySettingsScreen::percent));
				y += GAP;
				addDrawableChild(slider(x, y, "Sterne", 0f, 1f, c.starBrightness, v -> c.starBrightness = v, SkySettingsScreen::percent));
			}
			case ZEIT -> {
				addDrawableChild(toggle(x, y, "Eigene Uhrzeit", () -> c.customTime, v -> c.customTime = v));
				y += GAP;
				addDrawableChild(slider(x, y, "Uhrzeit", 0f, 24f, c.clockHours, v -> c.clockHours = v, SkySettingsScreen::clock));
			}
			case HIMMEL -> y = tint(x, y, c.skyTint);
			case WOLKEN -> {
				y = tint(x, y, c.cloudTint);
				addDrawableChild(slider(x, y, "Deckkraft", 0f, 1f, c.cloudOpacity, v -> c.cloudOpacity = v, SkySettingsScreen::percent));
				y += GAP;
				addDrawableChild(slider(x, y, "Helligkeit", 0f, 2f, c.cloudBrightness, v -> c.cloudBrightness = v, SkySettingsScreen::percent));
				y += GAP;
				addDrawableChild(slider(x, y, "Höhe", -64f, 128f, c.cloudHeightOffset, v -> c.cloudHeightOffset = v, SkySettingsScreen::blocks));
			}
			case NEBEL -> {
				y = tint(x, y, c.fogTint);
				addDrawableChild(slider(x, y, "Dichte", 0f, 3f, c.fogDensity, v -> c.fogDensity = v, SkySettingsScreen::percent));
			}
		}

		int bottom = Math.max(y + GAP + 40, height - 40);
		int half = (WIDTH - 4) / 2;
		addDrawableChild(ButtonWidget.builder(Text.literal("Zurücksetzen"), button -> {
			c.reset();
			clearAndInit();
		}).dimensions(x, bottom, half, HEIGHT).build());
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
				.dimensions(x + half + 4, bottom, half, HEIGHT).build());
	}

	private int tint(int x, int y, SkyConfig.Tint t) {
		addDrawableChild(slider(x, y, "Farbstärke (0 = Vanilla)", 0f, 1f, t.strength, v -> t.strength = v, SkySettingsScreen::percent));
		y += GAP;
		addDrawableChild(slider(x, y, "Rot", 0f, 1f, t.r, v -> t.r = v, SkySettingsScreen::byte255));
		y += GAP;
		addDrawableChild(slider(x, y, "Grün", 0f, 1f, t.g, v -> t.g = v, SkySettingsScreen::byte255));
		y += GAP;
		addDrawableChild(slider(x, y, "Blau", 0f, 1f, t.b, v -> t.b = v, SkySettingsScreen::byte255));
		return y + GAP;
	}

	private static String tabName(Page p) {
		return switch (p) {
			case ALLGEMEIN -> "Allg.";
			case ZEIT -> "Zeit";
			case HIMMEL -> "Himmel";
			case WOLKEN -> "Wolken";
			case NEBEL -> "Nebel";
		};
	}

	private ButtonWidget toggle(int x, int y, String label, BooleanSupplier get, Consumer<Boolean> set) {
		return ButtonWidget.builder(toggleText(label, get.getAsBoolean()), button -> {
			boolean value = !get.getAsBoolean();
			set.accept(value);
			button.setMessage(toggleText(label, value));
		}).dimensions(x, y, WIDTH, HEIGHT).build();
	}

	private static Text toggleText(String label, boolean on) {
		return Text.literal(label + ": " + (on ? "AN" : "AUS"));
	}

	private ConfigSlider slider(int x, int y, String label, float min, float max, float initial,
			Consumer<Float> setter, Function<Float, String> formatter) {
		return new ConfigSlider(x, y, label, min, max, initial, setter, formatter);
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

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		// Bewusst leer: kein Weichzeichner, damit man den Himmel live beurteilen kann.
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(textRenderer, title, LEFT + WIDTH / 2, Math.max(30, height / 2 - 100) - 16,
				0xFFFFFFFF);
	}

	@Override
	public void removed() {
		SkyConfig.save();
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	private static final class ConfigSlider extends SliderWidget {
		private final String label;
		private final float min;
		private final float max;
		private final Consumer<Float> setter;
		private final Function<Float, String> formatter;

		ConfigSlider(int x, int y, String label, float min, float max, float initial,
				Consumer<Float> setter, Function<Float, String> formatter) {
			super(x, y, WIDTH, HEIGHT, Text.empty(), (double) ((initial - min) / (max - min)));
			this.label = label;
			this.min = min;
			this.max = max;
			this.setter = setter;
			this.formatter = formatter;
			updateMessage();
		}

		private float current() {
			return (float) (min + (max - min) * value);
		}

		@Override
		protected void updateMessage() {
			if (label == null) {
				return;
			}
			setMessage(Text.literal(label + ": " + formatter.apply(current())));
		}

		@Override
		protected void applyValue() {
			setter.accept(current());
		}
	}
}
