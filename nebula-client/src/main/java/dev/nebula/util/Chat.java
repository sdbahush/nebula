package dev.nebula.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class Chat {
    private Chat() {}
    public static void send(MinecraftClient mc, String msg) {
        if (mc.inGameHud == null) return;
        mc.inGameHud.getChatHud().addMessage(Text.literal("\u00a7d[Nebula] \u00a7f" + msg));
    }
}
