package dev.nebula.feature.finder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

/** Scores farmland, crops and clusters of farm animals. */
public class FarmFinder extends FinderModule {
    public FarmFinder() {
        super("Farm Finder", "Finds crop fields and animal pens.", "Min score", 10, 200, 5, 40);
    }

    private static int weight(BlockState st) {
        if (st.isAir()) return 0;
        Block b = st.getBlock();
        if (b == Blocks.FARMLAND) return 1;
        if (b instanceof CropBlock || b == Blocks.NETHER_WART || b == Blocks.SUGAR_CANE
                || b == Blocks.MELON || b == Blocks.PUMPKIN || b == Blocks.COCOA) return 1;
        return 0;
    }

    @Override
    protected Finding scan(MinecraftClient mc, Chunk chunk, int cx, int cz) {
        ChunkSection[] sections = chunk.getSectionArray();
        int minY = mc.world.getBottomY();
        int score = 0;
        BlockPos first = null;
        for (int i = 0; i < sections.length; i++) {
            ChunkSection s = sections[i];
            if (s == null || s.isEmpty()) continue;
            int baseY = minY + i * 16;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int w = weight(s.getBlockState(x, y, z));
                        if (w > 0) {
                            score += w;
                            if (first == null) first = new BlockPos(cx * 16 + x, baseY + y, cz * 16 + z);
                        }
                    }
                }
            }
        }
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof AnimalEntity && (e.getBlockX() >> 4) == cx && (e.getBlockZ() >> 4) == cz) {
                score += 2;
                if (first == null) first = e.getBlockPos();
            }
        }
        return first == null ? null : new Finding(first, score);
    }
}
