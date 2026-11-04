package dev.nebula.feature.combat;

import dev.nebula.feature.Category;
import dev.nebula.feature.Feature;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.NumSetting;
import dev.nebula.util.Inv;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

public class StunSlam extends Feature {
    public final NumSetting delay = add(new NumSetting("Mace delay (ticks)", 1, 30, 1, 8));
    public final BoolSetting onlyFalling = add(new BoolSetting("Only while falling", true));

    private int stage; // 0 idle, 1 waiting for mace hit, 2 restoring slot
    private int age;
    private int prevSlot;
    private LivingEntity pending;

    public StunSlam() {
        super("Stun Slam", "Axe stuns the shield, then a mace slam.", Category.COMBAT);
    }

    @Override
    public void onDisable() { stage = 0; pending = null; }

    @Override
    public void onTick(MinecraftClient mc) {
        if (stage == 1) {
            age++;
            if (age >= delay.get()) {
                int mace = Inv.findHotbar(mc, s -> s.isOf(Items.MACE));
                if (mace >= 0 && pending != null && pending.isAlive() && mc.player.distanceTo(pending) <= 6.0f) {
                    Inv.select(mc, mace);
                    Inv.attack(mc, pending);
                }
                stage = 2;
                age = 0;
            }
            return;
        }
        if (stage == 2) {
            if (++age >= 2) {
                Inv.select(mc, prevSlot);
                stage = 0;
                pending = null;
            }
            return;
        }

        LivingEntity t = Inv.crosshairLiving(mc);
        if (t == null || !t.isBlocking()) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.95f) return;
        if (onlyFalling.get() && mc.player.fallDistance <= 1.5) return;

        int axe = Inv.findHotbar(mc, s -> s.isIn(ItemTags.AXES));
        int mace = Inv.findHotbar(mc, s -> s.isOf(Items.MACE));
        if (axe < 0 || mace < 0) return;

        prevSlot = Inv.selected(mc);
        Inv.select(mc, axe);
        Inv.attack(mc, t);
        pending = t;
        stage = 1;
        age = 0;
    }
}
