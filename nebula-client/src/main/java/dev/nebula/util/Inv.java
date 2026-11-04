package dev.nebula.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.function.Predicate;

public final class Inv {
    private Inv() {}

    public static int findHotbar(MinecraftClient mc, Predicate<ItemStack> p) {
        for (int i = 0; i < 9; i++) {
            if (p.test(mc.player.getInventory().getStack(i))) return i;
        }
        return -1;
    }

    public static int selected(MinecraftClient mc) { return mc.player.getInventory().getSelectedSlot(); }
    public static void select(MinecraftClient mc, int slot) { mc.player.getInventory().setSelectedSlot(slot); }

    public static LivingEntity crosshairLiving(MinecraftClient mc) {
        if (mc.currentScreen != null) return null;
        if (mc.crosshairTarget instanceof EntityHitResult r && r.getEntity() instanceof LivingEntity le) return le;
        return null;
    }

    public static void attack(MinecraftClient mc, LivingEntity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
