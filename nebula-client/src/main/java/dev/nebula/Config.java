package dev.nebula;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.nebula.feature.Feature;
import dev.nebula.feature.FeatureManager;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.NumSetting;
import dev.nebula.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private Config() {}

    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("nebula.json"); }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Feature f : FeatureManager.ALL) {
                JsonObject o = new JsonObject();
                if (f.savesEnabled()) o.addProperty("enabled", f.isEnabled());
                for (Setting s : f.settings) {
                    if (s instanceof BoolSetting b) o.addProperty(s.name, b.get());
                    else if (s instanceof NumSetting n) o.addProperty(s.name, n.get());
                }
                root.add(f.name, o);
            }
            Files.writeString(file(), GSON.toJson(root));
        } catch (Exception ignored) {
        }
    }

    public static void load() {
        try {
            Path p = file();
            if (!Files.exists(p)) return;
            JsonObject root = GSON.fromJson(Files.readString(p), JsonObject.class);
            if (root == null) return;
            for (Feature f : FeatureManager.ALL) {
                if (!root.has(f.name)) continue;
                JsonObject o = root.getAsJsonObject(f.name);
                if (f.savesEnabled() && o.has("enabled")) f.setEnabledSilently(o.get("enabled").getAsBoolean());
                for (Setting s : f.settings) {
                    JsonElement e = o.get(s.name);
                    if (e == null) continue;
                    if (s instanceof BoolSetting b) b.set(e.getAsBoolean());
                    else if (s instanceof NumSetting n) n.set(e.getAsDouble());
                }
            }
        } catch (Exception ignored) {
        }
    }
}
