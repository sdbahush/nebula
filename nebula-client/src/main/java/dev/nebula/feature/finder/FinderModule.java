package dev.nebula.feature.finder;

import dev.nebula.feature.Category;
import dev.nebula.feature.Feature;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.NumSetting;
import dev.nebula.util.Chat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FinderModule extends Feature {
    private static final int RESCAN_TICKS = 2400; // 2 minutes

    public final Map<Long, Finding> results = new HashMap<>();
    private final Map<Long, Integer> scanned = new HashMap<>();
    public final NumSetting threshold;
    public final NumSetting speed;
    public final BoolSetting alerts;
    private Object lastWorld;
    private int ticks;

    protected FinderModule(String name, String desc, String thresholdLabel,
                           double min, double max, double step, double def) {
        super(name, desc, Category.FINDERS);
        threshold = add(new NumSetting(thresholdLabel, min, max, step, def));
        speed = add(new NumSetting("Chunks per tick", 1, 6, 1, 1));
        alerts = add(new BoolSetting("Chat alerts", true));
    }

    protected abstract Finding scan(MinecraftClient mc, Chunk chunk, int cx, int cz);

    public int scannedCount() { return scanned.size(); }

    public void clear() {
        results.clear();
        scanned.clear();
    }

    @Override public void onEnable() { clear(); }
    @Override public void onDisable() { clear(); }

    @Override
    public void onTick(MinecraftClient mc) {
        ticks++;
        if (mc.world != lastWorld) {
            clear();
            lastWorld = mc.world;
        }
        int vd = mc.options.getClampedViewDistance();
        int pcx = mc.player.getBlockX() >> 4;
        int pcz = mc.player.getBlockZ() >> 4;
        int budget = (int) speed.get();

        for (int r = 0; r <= vd && budget > 0; r++) {
            for (int dx = -r; dx <= r && budget > 0; dx++) {
                for (int dz = -r; dz <= r && budget > 0; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    int cx = pcx + dx, cz = pcz + dz;
                    long key = (((long) cx) << 32) ^ (cz & 0xffffffffL);
                    Integer last = scanned.get(key);
                    if (last != null && ticks - last < RESCAN_TICKS) continue;

                    Chunk chunk = mc.world.getChunk(cx, cz, ChunkStatus.FULL, false);
                    if (chunk == null) continue;
                    scanned.put(key, ticks);
                    budget--;

                    Finding f = scan(mc, chunk, cx, cz);
                    Finding old = results.get(key);
                    if (f != null && f.score() >= threshold.get()) {
                        results.put(key, f);
                        if (old == null && alerts.get()) {
                            BlockPos p = f.pos();
                            Chat.send(mc, name + ": found something at " + p.getX() + " " + p.getY() + " "
                                    + p.getZ() + " (score " + f.score() + ")");
                        }
                    } else {
                        results.remove(key);
                    }
                }
            }
        }
    }

    public List<Finding> nearest(MinecraftClient mc, int n) {
        List<Finding> list = new ArrayList<>(results.values());
        if (mc.player == null) return list;
        double px = mc.player.getX(), pz = mc.player.getZ();
        list.sort(Comparator.comparingDouble(f -> {
            double dx = f.pos().getX() - px, dz = f.pos().getZ() - pz;
            return dx * dx + dz * dz;
        }));
        return list.size() > n ? list.subList(0, n) : list;
    }
}
