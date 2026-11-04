package dev.nebula.setting;

import java.util.Locale;

public class NumSetting extends Setting {
    public final double min, max, step;
    private double value;

    public NumSetting(String name, double min, double max, double step, double def) {
        super(name);
        this.min = min; this.max = max; this.step = step;
        set(def);
    }

    public double get() { return value; }

    public void set(double v) {
        v = Math.max(min, Math.min(max, v));
        v = Math.round((v - min) / step) * step + min;
        value = Math.max(min, Math.min(max, v));
    }

    public double fraction() { return (value - min) / (max - min); }

    public String display() {
        if (step >= 1) return String.valueOf((int) Math.round(value));
        return String.format(Locale.ROOT, step < 0.1 ? "%.2f" : "%.1f", value);
    }
}
