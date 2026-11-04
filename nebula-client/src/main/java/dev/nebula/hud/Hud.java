package dev.nebula.hud;

import dev.nebula.feature.Category;
import dev.nebula.feature.Feature;
import dev.nebula.feature.FeatureManager;
import dev.nebula.feature.finder.FinderModule;
import dev.nebula.feature.finder.Finding;
import dev.nebula.gui.Theme;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Hud {
    private static final String[] ARROWS = {"\u2191", "\u2197", "\u2192", "\u2198", "\u2193", "\u2199", "\u2190", "\u2196"};

    private Hud() {}

    public static void init() {
        HudElementRegistry.addLast(Identifier.of("nebula", "hud"), (ctx, tickCounter) -> render(ctx));
    }

    private static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;

        // Watermark
        if (FeatureManager.WATERMARK.isEnabled()) {
            String tail = " | " + mc.getCurrentFps() + " fps";
            int w = tr.getWidth("NEBULA") + tr.getWidth(tail) + 12;
            Theme.rrect(ctx, 4, 4, w, 15, 0xB0101018);
            ctx.fill(5, 4, 3 + w, 5, Theme.accent(0f));
            int gw = Theme.gradientText(ctx, tr, "NEBULA", 10, 8, 0f);
            ctx.drawText(tr, tail, 10 + gw, 8, Theme.DIM, true);
        }

        // Array list
        if (FeatureManager.ARRAYLIST.isEnabled()) {
            List<Feature> on = new ArrayList<>();
            for (Feature f : FeatureManager.ALL) {
                if (f.isEnabled() && f.category != Category.VISUAL) on.add(f);
            }
            on.sort(Comparator.comparingInt((Feature f) -> tr.getWidth(f.name)).reversed());
            int sw = ctx.getScaledWindowWidth();
            int y = 4;
            for (int i = 0; i < on.size(); i++) {
                Feature f = on.get(i);
                int w = tr.getWidth(f.name) + 8;
                int x = sw - w - 4;
                ctx.fill(x, y, sw - 1, y + 12, 0x90101018);
                ctx.fill(sw - 3, y, sw - 1, y + 12, Theme.accent(i * 0.18f));
                ctx.drawText(tr, f.name, x + 3, y + 2, Theme.accent(i * 0.18f), true);
                y += 12;
            }
        }

        // Finder results
        if (FeatureManager.FINDER_HUD.isEnabled()) {
            List<String> lines = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            for (Feature f : FeatureManager.ALL) {
                if (!(f instanceof FinderModule fm) || !fm.isEnabled()) continue;
                lines.add(fm.name + " (" + fm.results.size() + ")");
                colors.add(Theme.accent(lines.size() * 0.15f));
                for (Finding r : fm.nearest(mc, 4)) {
                    double dx = r.pos().getX() + 0.5 - mc.player.getX();
                    double dz = r.pos().getZ() + 0.5 - mc.player.getZ();
                    float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                    float rel = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
                    int idx = ((int) Math.round(rel / 45.0) + 8) % 8;
                    int dist = (int) Math.sqrt(dx * dx + dz * dz);
                    lines.add(" " + ARROWS[idx] + " " + dist + "m  " + r.pos().getX() + ", " + r.pos().getY()
                            + ", " + r.pos().getZ() + "  [" + r.score() + "]");
                    colors.add(Theme.TEXT);
                }
            }
            if (!lines.isEmpty()) {
                int maxW = 0;
                for (String s : lines) maxW = Math.max(maxW, tr.getWidth(s));
                int top = 24;
                Theme.rrect(ctx, 4, top, maxW + 10, lines.size() * 11 + 6, 0xA0101018);
                for (int i = 0; i < lines.size(); i++) {
                    ctx.drawText(tr, lines.get(i), 9, top + 4 + i * 11, colors.get(i), true);
                }
            }
        }
    }
}
