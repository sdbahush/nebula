package dev.nebula.feature.combat;

import dev.nebula.feature.Category;
import dev.nebula.feature.Feature;
import dev.nebula.setting.NumSetting;
import dev.nebula.util.Chat;
import dev.nebula.util.Inv;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Aim at the ground and enable: places a rail, a TNT minecart, then lights it.
 * Needs a rail, a TNT minecart and flint & steel (or fire charge) in the hotbar.
 */
public class AutoTntMinecart extends Feature {
    public final NumSetting stepDelay = add(new NumSetting("Step delay (ticks)", 1, 10, 1, 3));

    private int stage, timer, tries, prevSlot, railSlot, tntSlot, igniteSlot;
    private BlockPos rail;

    public AutoTntMinecart() {
        super("TNT Minecart", "Places and lights a TNT minecart where you aim.", Category.COMBAT);
        saveEnabled = false;
    }

    @Override
    public void onEnable() {
        stage = 0; timer = 0; tries = 0; rail = null;
    }

    private void fail(MinecraftClient mc, String msg) {
        Chat.send(mc, msg);
        if (mc.player != null && stage > 0) Inv.select(mc, prevSlot);
        setEnabled(false);
    }

    private void use(MinecraftClient mc, BlockHitResult hit) {
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (timer > 0) { timer--; return; }
        int delay = (int) stepDelay.get();

        switch (stage) {
            case 0 -> {
                if (!(mc.crosshairTarget instanceof BlockHitResult)) { fail(mc, "Aim at a block first."); return; }
                BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
                if (hit.getType() != HitResult.Type.BLOCK) { fail(mc, "Aim at a block first."); return; }

                railSlot = Inv.findHotbar(mc, s -> s.isIn(ItemTags.RAILS));
                tntSlot = Inv.findHotbar(mc, s -> s.isOf(Items.TNT_MINECART));
                igniteSlot = Inv.findHotbar(mc, s -> s.isOf(Items.FLINT_AND_STEEL) || s.isOf(Items.FIRE_CHARGE));

                BlockState aimed = mc.world.getBlockState(hit.getBlockPos());
                boolean aimedAtRail = aimed.isIn(BlockTags.RAILS);
                if ((railSlot < 0 && !aimedAtRail) || tntSlot < 0 || igniteSlot < 0) {
                    fail(mc, "Need a rail, TNT minecart and flint & steel (or fire charge) in your hotbar.");
                    return;
                }
                prevSlot = Inv.selected(mc);
                if (aimedAtRail) {
                    rail = hit.getBlockPos();
                    stage = 2;
                } else {
                    rail = hit.getBlockPos().offset(hit.getSide());
                    Inv.select(mc, railSlot);
                    use(mc, hit);
                    stage = 1;
                }
                tries = 0;
                timer = delay;
            }
            case 1 -> {
                if (!mc.world.getBlockState(rail).isIn(BlockTags.RAILS)) {
                    if (++tries > 10) { fail(mc, "Couldn't place the rail there."); return; }
                    timer = 1;
                    return;
                }
                stage = 2;
            }
            case 2 -> {
                Inv.select(mc, tntSlot);
                Vec3d p = new Vec3d(rail.getX() + 0.5, rail.getY() + 0.1, rail.getZ() + 0.5);
                use(mc, new BlockHitResult(p, Direction.UP, rail, false));
                stage = 3;
                tries = 0;
                timer = delay;
            }
            case 3 -> {
                Entity cart = null;
                Vec3d center = Vec3d.ofCenter(rail);
                for (Entity e : mc.world.getEntities()) {
                    if (e.getType() == EntityType.TNT_MINECART && e.squaredDistanceTo(center) < 4.0) {
                        cart = e;
                        break;
                    }
                }
                if (cart == null) {
                    if (++tries > 10) { fail(mc, "The minecart didn't spawn."); return; }
                    timer = 1;
                    return;
                }
                Inv.select(mc, igniteSlot);
                mc.interactionManager.interactEntity(mc.player, cart, Hand.MAIN_HAND);
                mc.player.swingHand(Hand.MAIN_HAND);
                stage = 4;
                timer = delay;
            }
            default -> {
                Inv.select(mc, prevSlot);
                Chat.send(mc, "TNT minecart lit. Back away!");
                setEnabled(false);
            }
        }
    }
}
