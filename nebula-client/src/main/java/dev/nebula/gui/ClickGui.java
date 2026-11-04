package dev.nebula.gui;

import dev.nebula.Config;
import dev.nebula.feature.Category;
import dev.nebula.feature.Feature;
import dev.nebula.feature.FeatureManager;
import dev.nebula.feature.finder.FinderModule;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.NumSetting;
import dev.nebula.setting.Setting;
import dev.nebula.util.Keys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Immediate-mode GUI. Mouse input is polled with GLFW inside render(), which keeps
 * this class independent of Minecraft's input-event signatures.
 */
public class ClickGui extends Screen {
    private static Category tab = Category.COMBAT;
    private static Feature selected;

    private boolean lastDown = true; // ignore a button already held when opening
    private Setting dragging;
    private int dragX, dragW;

    public ClickGui() {
        super(Text.literal("Nebula"));
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void removed() { Config.save(); }

    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        long handle = mc.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean click = down && !lastDown;
        lastDown = down;
        if (!down) dragging = null;
        if (Keys.pressedOnce(GLFW.GLFW_KEY_RIGHT_SHIFT)) { close(); return; }

        final int W = 500, H = 300;
        int x = (this.width - W) / 2, y = (this.height - H) / 2;

        ctx.fill(0, 0, this.width, this.height, 0x90000000);
        Theme.rrect(ctx, x - 1, y - 1, W + 2, H + 2, 0x55B26BFF);
        Theme.rrect(ctx, x, y, W, H, Theme.BG);
        for (int i = 0; i < W - 2; i++) ctx.fill(x + 1 + i, y, x + 2 + i, y + 2, Theme.accent(i / (float) W));

        int tw = Theme.gradientText(ctx, tr, "NEBULA", x + 14, y + 12, 0f);
        ctx.drawText(tr, "singleplayer client", x + 20 + tw, y + 12, Theme.DIM, false);
        String hint = "Right Shift / Esc to close";
        ctx.drawText(tr, hint, x + W - 14 - tr.getWidth(hint), y + 12, Theme.DIM, false);

        // ---- tabs ----
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int tx = x + 14 + i * 86, ty = y + 34;
            boolean sel = cats[i] == tab;
            boolean hov = in(mouseX, mouseY, tx, ty, 80, 18);
            Theme.rrect(ctx, tx, ty, 80, 18, sel || hov ? Theme.PANEL_HOVER : Theme.PANEL);
            String label = cats[i].label;
            ctx.drawText(tr, label, tx + (80 - tr.getWidth(label)) / 2, ty + 5, sel ? Theme.TEXT : Theme.DIM, false);
            if (sel) ctx.fill(tx + 8, ty + 16, tx + 72, ty + 18, Theme.accent(i * 0.3f));
            if (click && hov) { tab = cats[i]; selected = null; }
        }

        // ---- module list ----
        List<Feature> list = FeatureManager.of(tab);
        if (selected == null || selected.category != tab) selected = list.isEmpty() ? null : list.get(0);

        int mx = x + 14, mw = 224;
        for (int i = 0; i < list.size(); i++) {
            Feature f = list.get(i);
            int my = y + 62 + i * 30;
            boolean hov = in(mouseX, mouseY, mx, my, mw, 26);
            Theme.rrect(ctx, mx, my, mw, 26, hov ? Theme.PANEL_HOVER : Theme.PANEL);
            if (f == selected) Theme.outline(ctx, mx, my, mw, 26, Theme.accent(i * 0.2f));
            ctx.fill(mx + 4, my + 5, mx + 6, my + 21, f.isEnabled() ? Theme.accent(i * 0.2f) : Theme.OFF);
            ctx.drawText(tr, f.name, mx + 12, my + 4, Theme.TEXT, false);
            ctx.drawText(tr, tr.trimToWidth(f.description, mw - 66), mx + 12, my + 14, Theme.DIM, false);
            int pillX = mx + mw - 36, pillY = my + 7;
            Theme.pill(ctx, pillX, pillY, f.isEnabled(), i * 0.2f);
            if (click && hov) {
                if (in(mouseX, mouseY, pillX - 2, pillY - 2, 32, 16)) f.toggle();
                else selected = f;
            }
        }

        // ---- settings panel ----
        int px = x + 254, py = y + 62, pw = W - 254 - 14, ph = H - 62 - 14;
        Theme.rrect(ctx, px, py, pw, ph, Theme.PANEL);
        if (selected != null) {
            Theme.gradientText(ctx, tr, selected.name, px + 10, py + 8, 0.5f);
            ctx.drawText(tr, tr.trimToWidth(selected.description, pw - 20), px + 10, py + 20, Theme.DIM, false);
            ctx.fill(px + 10, py + 33, px + pw - 10, py + 34, Theme.OFF);

            int sy = py + 42;
            if (selected.settings.isEmpty()) {
                ctx.drawText(tr, "No settings.", px + 10, sy, Theme.DIM, false);
            }
            for (Setting s : selected.settings) {
                if (s instanceof BoolSetting b) {
                    ctx.drawText(tr, b.name, px + 10, sy + 3, Theme.TEXT, false);
                    Theme.pill(ctx, px + pw - 40, sy + 1, b.get(), 0.4f);
                    if (click && in(mouseX, mouseY, px + 10, sy - 2, pw - 20, 18)) b.toggle();
                    sy += 22;
                } else if (s instanceof NumSetting n) {
                    ctx.drawText(tr, n.name, px + 10, sy, Theme.TEXT, false);
                    String v = n.display();
                    ctx.drawText(tr, v, px + pw - 10 - tr.getWidth(v), sy, Theme.accent(0.2f), false);
                    int tx = px + 10, tW = pw - 20, ty = sy + 15;
                    ctx.fill(tx, ty, tx + tW, ty + 4, Theme.OFF);
                    int fillW = (int) (tW * n.fraction());
                    ctx.fill(tx, ty, tx + fillW, ty + 4, Theme.accent(0.1f));
                    ctx.fill(tx + fillW - 2, ty - 2, tx + fillW + 2, ty + 6, 0xFFFFFFFF);
                    if (click && in(mouseX, mouseY, tx - 3, ty - 5, tW + 6, 14)) {
                        dragging = n; dragX = tx; dragW = tW;
                    }
                    sy += 32;
                }
            }

            if (selected instanceof FinderModule fm) {
                String info = "Found: " + fm.results.size() + "   Scanned chunks: " + fm.scannedCount();
                ctx.drawText(tr, info, px + 10, py + ph - 14, Theme.DIM, false);
            }
        }

        if (dragging instanceof NumSetting n && down) {
            double t = Math.max(0.0, Math.min(1.0, (mouseX - dragX) / (double) dragW));
            n.set(n.min + (n.max - n.min) * t);
        }
    }
}
