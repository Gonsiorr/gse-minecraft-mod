package gse.minu;

import gse.minu.item.F18Item;
import gse.minu.item.GSEItems;
import gse.minu.registry.BoatRegistry;
import gse.minu.network.GSENetworking;
import gse.minu.wind.WindManager;
import gse.minu.workshop.SailingCraftingStationRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GonsioRRSailingEngine implements ModInitializer {
    public static final String MOD_ID = "gonsiorr-sailing-engine";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BoatRegistry.register();
        F18Item.register();
        GSEItems.register();
        SailingCraftingStationRegistry.register();
        GSENetworking.register();
        WindManager.registerEvents();
        LOGGER.info("Gonsiorr Sailing Engine initialized");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
