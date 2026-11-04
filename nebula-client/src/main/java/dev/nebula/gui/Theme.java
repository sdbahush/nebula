package dev.nebula.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class Theme {
    public static final int BG = 0xF2101018;
    public static final int PANEL = 0xFF181826;
    public static final int PANEL_HOVER = 0xFF222238;
    public static final int OFF = 0xFF34344A;
    public static final int TEXT = 0xFFE8E8F5;
    public static final int DIM = 0xFF8A8AA5;
    private static final int A = 0xFFB26BFF; // violet
    private static final int B = 0xFF4DE3FF; // cyan

    private Theme() {}

    public static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + ((((b >> 16) & 255) - ((a >> 16) & 255)) * t));
        int g = (int) (((a >> 8) & 255) + ((((b >> 8) & 255) - ((a >> 8) & 255)) * t));
        int bl = (int) ((a & 255) + (((b & 255) - (a & 255)) * t));
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    /** Animated violet-to-cyan gradient colour. */
    public static int accent(float phase) {
        double t = (Math.sin(System.currentTimeMillis() / 700.0 + phase * 3.0) + 1.0) / 2.0;
        return lerp(A, B, (float) t);
    }

    public static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x + 1, y, x + w - 1, y + h, col);
        c.fill(x, y + 1, x + 1, y + h - 1, col);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, col);
    }

    public static void outline(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x + 1, y, x + w - 1, y + 1, col);
        c.fill(x + 1, y + h - 1, x + w - 1, y + h, col);
        c.fill(x, y + 1, x + 1, y + h - 1, col);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, col);
    }

    public static int gradientText(DrawContext c, TextRenderer tr, String s, int x, int y, float phase) {
        int cx = 0;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            c.drawText(tr, ch, x + cx, y, accent(phase + i * 0.12f), true);
            cx += tr.getWidth(ch);
        }
        return cx;
    }

    public static void pill(DrawContext c, int x, int y, boolean on, float phase) {
        rrect(c, x, y, 28, 12, on ? accent(phase) : OFF);
        int kx = on ? x + 18 : x + 2;
        c.fill(kx, y + 2, kx + 8, y + 10, 0xFFFFFFFF);
    }
}
