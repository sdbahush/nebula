package dev.nebula.feature;

import dev.nebula.feature.combat.*;
import dev.nebula.feature.finder.*;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public final class FeatureManager {
    public static final List<Feature> ALL = new ArrayList<>();

    public static Hitboxes HITBOXES;
    public static ToggleFeature WATERMARK, ARRAYLIST, FINDER_HUD;

    private FeatureManager() {}

    public static void init() {
        ALL.clear();
        // Combat
        ALL.add(new Reach());
        HITBOXES = new Hitboxes();
        ALL.add(HITBOXES);
        ALL.add(new ShieldBreaker());
        ALL.add(new StunSlam());
        ALL.add(new AutoTntMinecart());
        // Finders
        ALL.add(new StashFinder());
        ALL.add(new BaseFinder());
        ALL.add(new FarmFinder());
        // Visual
        WATERMARK = new ToggleFeature("Watermark", "Client name and FPS in the corner.");
        ARRAYLIST = new ToggleFeature("Array List", "Lists enabled modules on the right.");
        FINDER_HUD = new ToggleFeature("Finder HUD", "Shows nearest finder results with arrows.");
        ALL.add(WATERMARK);
        ALL.add(ARRAYLIST);
        ALL.add(FINDER_HUD);
    }

    public static List<Feature> of(Category c) {
        List<Feature> out = new ArrayList<>();
        for (Feature f : ALL) if (f.category == c) out.add(f);
        return out;
    }

    /** Gameplay modules only run in singleplayer worlds. */
    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        boolean sp = mc.isInSingleplayer();
        for (Feature f : ALL) {
            if (!f.isEnabled()) continue;
            if (!sp && f.category != Category.VISUAL) continue;
            f.onTick(mc);
        }
    }
}
