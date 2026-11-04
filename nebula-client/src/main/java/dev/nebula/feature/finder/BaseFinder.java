package dev.nebula.feature.finder;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

/** Heuristic: scores player-style blocks (workstations, beds, glass, lamps...). Villages can trigger it too. */
public class BaseFinder extends FinderModule {
    public BaseFinder() {
        super("Base Finder", "Finds chunks full of player-built blocks.", "Min score", 4, 100, 1, 14);
    }

    private static int weight(BlockState st) {
        if (st.isAir()) return 0;
        Block b = st.getBlock();
        if (b == Blocks.CRAFTING_TABLE || b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE || b == Blocks.SMOKER
                || b == Blocks.ENCHANTING_TABLE || b == Blocks.BREWING_STAND || b == Blocks.ANVIL
                || b == Blocks.BEACON || b instanceof BedBlock) return 3;
        if (b == Blocks.NETHERITE_BLOCK || b == Blocks.DIAMOND_BLOCK || b == Blocks.IRON_BLOCK
                || b == Blocks.GOLD_BLOCK || b == Blocks.HOPPER) return 2;
        if (b == Blocks.GLASS || b == Blocks.GLASS_PANE || b == Blocks.LANTERN || b == Blocks.SOUL_LANTERN
                || b == Blocks.TORCH || b == Blocks.WALL_TORCH || b == Blocks.REDSTONE_LAMP
                || b == Blocks.DISPENSER || b == Blocks.DROPPER || b == Blocks.OBSERVER
                || b == Blocks.PISTON || b == Blocks.STICKY_PISTON || b == Blocks.REPEATER
                || b == Blocks.COMPARATOR || st.isIn(BlockTags.WOOL)) return 1;
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
        return first == null ? null : new Finding(first, score);
    }
}
