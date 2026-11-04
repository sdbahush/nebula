package dev.nebula.feature.combat;

import dev.nebula.feature.Category;
import dev.nebula.feature.Feature;
import dev.nebula.setting.BoolSetting;
import dev.nebula.util.Inv;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags;

public class ShieldBreaker extends Feature {
    public final BoolSetting swapBack = add(new BoolSetting("Swap back", true));
    private int restoreSlot = -1;
    private int restoreTimer;

    public ShieldBreaker() {
        super("Shield Breaker", "Auto-hits blocking targets with an axe.", Category.COMBAT);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (restoreSlot >= 0 && --restoreTimer <= 0) {
            Inv.select(mc, restoreSlot);
            restoreSlot = -1;
        }
        if (restoreSlot >= 0) return;

        LivingEntity t = Inv.crosshairLiving(mc);
        if (t == null || !t.isBlocking()) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.95f) return;

        int axe = Inv.findHotbar(mc, s -> s.isIn(ItemTags.AXES));
        if (axe < 0) return;

        int prev = Inv.selected(mc);
        if (prev != axe) Inv.select(mc, axe);
        Inv.attack(mc, t);
        if (swapBack.get() && prev != axe) {
            restoreSlot = prev;
            restoreTimer = 2;
        }
    }
}
