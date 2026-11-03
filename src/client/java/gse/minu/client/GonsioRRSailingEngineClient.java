package gse.minu.client;

import gse.minu.registry.BoatRegistry;
import gse.minu.workshop.SailingCraftingStationRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class GonsioRRSailingEngineClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(BoatRegistry.F18, F18Renderer::new);
        MenuScreens.register(SailingCraftingStationRegistry.MENU, SailingCraftingScreen::new);
        F18KeyBindings.register();
        F18InputHandler.register();
        F18HelmInput.register();
        WindHudState.register();
        SailingHUD.register();
    }
}
