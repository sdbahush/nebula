package dev.nebula.util;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public final class Keys {
    private static final Map<Integer, Boolean> LAST = new HashMap<>();
    private Keys() {}

    /** True once each time the key goes from up to down. */
    public static boolean pressedOnce(int key) {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
        boolean was = LAST.getOrDefault(key, false);
        LAST.put(key, down);
        return down && !was;
    }
}
