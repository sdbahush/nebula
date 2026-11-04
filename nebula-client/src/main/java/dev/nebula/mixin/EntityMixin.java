package dev.nebula.mixin;

import dev.nebula.feature.FeatureManager;
import dev.nebula.feature.combat.Hitboxes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void nebula$hitboxMargin(CallbackInfoReturnable<Float> cir) {
        Hitboxes h = FeatureManager.HITBOXES;
        if (h == null || !h.isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        Entity self = (Entity) (Object) this;
        if (mc.player == null || self == mc.player || !(self instanceof LivingEntity) || !mc.isInSingleplayer()) return;
        cir.setReturnValue((float) h.expand.get());
    }
}
