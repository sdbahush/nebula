package dev.nebula.feature.finder;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

public class StashFinder extends FinderModule {
    public StashFinder() {
        super("Stash Finder", "Finds chunks packed with chests, barrels, shulkers.",
                "Min containers", 2, 40, 1, 6);
    }

    @Override
    protected Finding scan(MinecraftClient mc, Chunk chunk, int cx, int cz) {
        int count = 0;
        BlockPos first = null;
        for (BlockPos p : new ArrayList<>(chunk.getBlockEntityPositions())) {
            BlockEntity be = mc.world.getBlockEntity(p);
            if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity || be instanceof ShulkerBoxBlockEntity) {
                count++;
                if (first == null) first = p;
            }
        }
        return first == null ? null : new Finding(first, count);
    }
}
