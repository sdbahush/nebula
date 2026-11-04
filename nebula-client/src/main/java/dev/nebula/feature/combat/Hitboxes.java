package dev.nebula.feature.combat;

import dev.nebula.feature.Category;
import dev.nebula.feature.Feature;
import dev.nebula.setting.NumSetting;

public class Hitboxes extends Feature {
    public final NumSetting expand = add(new NumSetting("Expand", 0.0, 1.0, 0.05, 0.3));

    public Hitboxes() {
        super("Hitboxes", "Makes mobs and players easier to hit.", Category.COMBAT);
    }
}
