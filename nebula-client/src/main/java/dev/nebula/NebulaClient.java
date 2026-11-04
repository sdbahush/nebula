package dev.nebula;

import dev.nebula.feature.FeatureManager;
import dev.nebula.gui.ClickGui;
import dev.nebula.hud.Hud;
import dev.nebula.util.Keys;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

public class NebulaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FeatureManager.init();
        Config.load();
        Hud.init();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null) return;
            if (mc.currentScreen == null && Keys.pressedOnce(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                mc.setScreen(new ClickGui());
            }
            FeatureManager.tick(mc);
        });
    }
}
