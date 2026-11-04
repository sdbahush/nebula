package dev.nebula.feature;

public class ToggleFeature extends Feature {
    public ToggleFeature(String name, String description) {
        super(name, description, Category.VISUAL);
        setEnabledSilently(true);
    }
}
