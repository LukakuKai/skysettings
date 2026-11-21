package de.skycontrol;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class SkyScreen extends Screen {
    private enum Page { ALLGEMEIN, ZEIT, HIMMEL, WOLKEN, NEBEL }

    private static Page page = Page.ALLGEMEIN;
    private static final int PANEL_W = 330;

    public SkyScreen() {
        super(Component.literal("SkyControl"));
    }

    @Override
    protected void init() {
        int x = 10;
        int y = 30;
        int w = PANEL_W - 20;

        // Tabs
        Page[] pages = Page.values();
        int tabW = w / pages.length;
        for (int i = 0; i < pages.length; i++) {
            Page p = pages[i];
            Button b = Button.builder(Component.literal(tabName(p)), btn -> {
                page = p;
                rebuildWidgets();
            }).bounds(x + i * tabW, y, tabW - 2, 20).build();
            b.active = (p != page);
            addRenderableWidget(b);
        }
        y += 32;

        SkyConfig.Data d = SkyConfig.D;
        switch (page) {
            case ALLGEMEIN -> {
                addRenderableWidget(toggle(x, y, w, "Mod aktiv", () -> d.enabled, v -> d.enabled = v));
                y += 24;
                addRenderableWidget(slider(x, y, w, "Helligkeit", 0f, 2f, d.brightness, v -> d.brightness = v, null));
                y += 24;
                addRenderableWidget(slider(x, y, w, "Sättigung", 0f, 2f, d.saturation, v -> d.saturation = v, null));
                y += 24;
                addRenderableWidget(slider(x, y, w, "Sterne", 0f, 3f, d.stars, v -> d.stars = v, null));
                y += 32;
                addRenderableWidget(Button.builder(Component.literal("Alles zurücksetzen"), btn -> {
                    SkyConfig.reset();
                    rebuildWidgets();
                }).bounds(x, y, w, 20).build());
            }
            case ZEIT -> {
                addRenderableWidget(toggle(x, y, w, "Eigene Uhrzeit", () -> d.customTime, v -> d.customTime = v));
                y += 24;
                addRenderableWidget(slider(x, y, w, "Uhrzeit", 0f, 24f, d.clockHours, v -> d.clockHours = v, SkyScreen::clock));
            }
            case HIMMEL -> tintWidgets(x, y, w, d.sky);
            case WOLKEN -> tintWidgets(x, y, w, d.clouds);
            case NEBEL -> tintWidgets(x, y, w, d.fog);
        }

        addRenderableWidget(Button.builder(Component.literal("Fertig"), btn -> onClose())
                .bounds(x, height - 30, w, 20).build());
    }

    private void tintWidgets(int x, int y, int w, SkyConfig.Tint t) {
        addRenderableWidget(slider(x, y, w, "Stärke (0 = Vanilla)", 0f, 1f, t.strength, v -> t.strength = v, null));
        y += 24;
        addRenderableWidget(slider(x, y, w, "Rot", 0f, 1f, t.r, v -> t.r = v, SkyScreen::byte255));
        y += 24;
        addRenderableWidget(slider(x, y, w, "Grün", 0f, 1f, t.g, v -> t.g = v, SkyScreen::byte255));
        y += 24;
        addRenderableWidget(slider(x, y, w, "Blau", 0f, 1f, t.b, v -> t.b = v, SkyScreen::byte255));
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

    private static String clock(float h) {
        int total = Math.round(h * 60f) % (24 * 60);
        return String.format(Locale.ROOT, "%02d:%02d", total / 60, total % 60);
    }

    private static String byte255(float v) {
        return String.valueOf(Math.round(v * 255f));
    }

    private Button toggle(int x, int y, int w, String label, java.util.function.BooleanSupplier get, java.util.function.Consumer<Boolean> set) {
        return Button.builder(toggleText(label, get.getAsBoolean()), btn -> {
            boolean nv = !get.getAsBoolean();
            set.accept(nv);
            btn.setMessage(toggleText(label, nv));
        }).bounds(x, y, w, 20).build();
    }

    private static Component toggleText(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "AN" : "AUS"));
    }

    private FloatSlider slider(int x, int y, int w, String label, float min, float max, float cur,
                               Consumer<Float> setter, Function<Float, String> fmt) {
        return new FloatSlider(x, y, w, label, min, max, cur, setter, fmt);
    }

    /** Hintergrund bewusst NICHT abdunkeln/verwischen, damit man den Himmel live sieht. */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, PANEL_W, height, 0xA0000000);
        g.drawString(font, title, 10, 12, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        SkyConfig.save();
    }

    private static class FloatSlider extends AbstractSliderButton {
        private final String label;
        private final float min, max;
        private final Consumer<Float> setter;
        private final Function<Float, String> fmt;

        FloatSlider(int x, int y, int w, String label, float min, float max, float cur,
                    Consumer<Float> setter, Function<Float, String> fmt) {
            super(x, y, w, 20, Component.empty(), (cur - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.fmt = fmt;
            updateMessage();
        }

        private float get() {
            return (float) (min + (max - min) * value);
        }

        @Override
        protected void updateMessage() {
            if (label == null) return; // wird evtl. schon vom Super-Konstruktor aufgerufen
            float v = get();
            String txt = fmt != null ? fmt.apply(v) : String.format(Locale.ROOT, "%.2f", v);
            setMessage(Component.literal(label + ": " + txt));
        }

        @Override
        protected void applyValue() {
            setter.accept(get());
        }
    }
}
