package dev.nebula.feature;

public enum Category {
    COMBAT("Combat"), FINDERS("Finders"), VISUAL("Visual");
    public final String label;
    Category(String label) { this.label = label; }
}
