package dev.nebula.feature.combat;

import dev.nebula.feature.Category;
import dev.nebula.feature.Feature;
import dev.nebula.setting.NumSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class Reach extends Feature {
    public final NumSetting range = add(new NumSetting("Entity reach", 3.0, 6.0, 0.1, 4.5));

    public Reach() {
        super("Reach", "Extends how far you can hit entities.", Category.COMBAT);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        applyAll(mc, range.get());
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) applyAll(mc, 3.0);
    }

    private static void applyAll(MinecraftClient mc, double v) {
        apply(mc.player, v);
        MinecraftServer server = mc.getServer();
        if (server != null) {
            final UUID uuid = mc.player.getUuid();
            server.execute(() -> {
                ServerPlayerEntity sp = server.getPlayerManager().getPlayer(uuid);
                if (sp != null) apply(sp, v);
            });
        }
    }

    private static void apply(LivingEntity e, double v) {
        EntityAttributeInstance inst = e.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE);
        if (inst != null && inst.getBaseValue() != v) inst.setBaseValue(v);
    }
}
