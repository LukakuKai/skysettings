package dev.skysettings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public class SkySettingsScreen extends Screen {
	private static final int WIDTH = 220;
	private static final int HEIGHT = 20;
	private static final int GAP = 24;

	private final Screen parent;

	public SkySettingsScreen(Screen parent) {
		super(Text.literal("Sky Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		SkyConfig c = SkyConfig.get();
		// Links ausgerichtet, damit der Himmel in der Mitte sichtbar bleibt.
		int x = 20;
		int y = Math.max(30, height / 2 - 100);

		addDrawableChild(ButtonWidget.builder(toggleText(c), button -> {
			c.enabled = !c.enabled;
			button.setMessage(toggleText(c));
		}).dimensions(x, y, WIDTH, HEIGHT).build());
		y += GAP;

		addDrawableChild(new ConfigSlider(x, y, "Himmelshelligkeit", 0f, 2f, c.skyBrightness,
				v -> c.skyBrightness = v, SkySettingsScreen::percent));
		y += GAP;
		addDrawableChild(new ConfigSlider(x, y, "Sternhelligkeit", 0f, 1f, c.starBrightness,
				v -> c.starBrightness = v, SkySettingsScreen::percent));
		y += GAP;
		addDrawableChild(new ConfigSlider(x, y, "Wolken-Dichte (Deckkraft)", 0f, 1f, c.cloudOpacity,
				v -> c.cloudOpacity = v, SkySettingsScreen::percent));
		y += GAP;
		addDrawableChild(new ConfigSlider(x, y, "Wolkenhelligkeit", 0f, 2f, c.cloudBrightness,
				v -> c.cloudBrightness = v, SkySettingsScreen::percent));
		y += GAP;
		addDrawableChild(new ConfigSlider(x, y, "Wolkenhöhe", -64f, 128f, c.cloudHeightOffset,
				v -> c.cloudHeightOffset = v, SkySettingsScreen::blocks));
		y += GAP;
		addDrawableChild(new ConfigSlider(x, y, "Nebeldichte", 0f, 3f, c.fogDensity,
				v -> c.fogDensity = v, SkySettingsScreen::percent));
		y += GAP + 6;

		int half = (WIDTH - 4) / 2;
		addDrawableChild(ButtonWidget.builder(Text.literal("Zurücksetzen"), button -> {
			c.reset();
			clearAndInit();
		}).dimensions(x, y, half, HEIGHT).build());
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
				.dimensions(x + half + 4, y, half, HEIGHT).build());
	}

	private static Text toggleText(SkyConfig c) {
		return Text.literal("Sky Settings: " + (c.enabled ? "AN" : "AUS"));
	}

	private static String percent(float v) {
		return Math.round(v * 100f) + "%";
	}

	private static String blocks(float v) {
		int r = Math.round(v);
		return (r > 0 ? "+" : "") + r + " Blöcke";
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		// Bewusst leer: kein Weichzeichner, damit man den Himmel live beurteilen kann.
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(textRenderer, title, 20 + WIDTH / 2, Math.max(30, height / 2 - 100) - 16,
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
			setMessage(Text.literal(label + ": " + formatter.apply(current())));
		}

		@Override
		protected void applyValue() {
			setter.accept(current());
		}
	}
}
