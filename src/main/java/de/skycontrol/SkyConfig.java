package de.skycontrol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Files;
import java.nio.file.Path;

/** Einstellungen + Farbberechnung. Wird in config/skycontrol.json gespeichert. */
public final class SkyConfig {
    public static class Tint {
        public float strength = 0f; // 0 = Vanilla, 1 = komplett eigene Farbe
        public float r = 1f, g = 1f, b = 1f;
    }

    public static class Data {
        public boolean enabled = true;
        public float brightness = 1f;   // 0..2
        public float saturation = 1f;   // 0..2
        public float stars = 1f;        // 0..3 (0 = keine Sterne)
        public boolean customTime = false;
        public float clockHours = 12f;  // 0..24 Uhr
        public Tint sky = new Tint();
        public Tint clouds = new Tint();
        public Tint fog = new Tint();
    }

    public static Data D = new Data();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("skycontrol.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                Data d = GSON.fromJson(Files.readString(p), Data.class);
                if (d != null) D = d;
            }
        } catch (Exception e) {
            System.err.println("[SkyControl] Konnte Config nicht lesen: " + e);
        }
        if (D.sky == null) D.sky = new Tint();
        if (D.clouds == null) D.clouds = new Tint();
        if (D.fog == null) D.fog = new Tint();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(D));
        } catch (Exception e) {
            System.err.println("[SkyControl] Konnte Config nicht speichern: " + e);
        }
    }

    public static void reset() {
        D = new Data();
        save();
    }

    /** Minecraft-Tageszeit in Ticks: 0 = 6 Uhr, 6000 = 12 Uhr, 12000 = 18 Uhr, 18000 = 0 Uhr. */
    public static long ticks() {
        float t = (((D.clockHours - 6f) % 24f) + 24f) % 24f * 1000f;
        return (long) t;
    }

    /** Wendet Farbmischung, Saettigung und Helligkeit auf eine Farbe an. */
    public static Vec3 apply(Vec3 c, Tint t) {
        double r = c.x, g = c.y, b = c.z;
        float s = t.strength;
        if (s > 0f) {
            r += (t.r - r) * s;
            g += (t.g - g) * s;
            b += (t.b - b) * s;
        }
        double lum = 0.299 * r + 0.587 * g + 0.114 * b;
        double sat = D.saturation;
        r = lum + (r - lum) * sat;
        g = lum + (g - lum) * sat;
        b = lum + (b - lum) * sat;
        double br = D.brightness;
        r = clamp(r * br);
        g = clamp(g * br);
        b = clamp(b * br);
        return new Vec3(r, g, b);
    }

    private static double clamp(double v) {
        return v < 0 ? 0 : Math.min(v, 1);
    }
}
