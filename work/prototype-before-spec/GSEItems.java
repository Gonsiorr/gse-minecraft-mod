package gse.minu.registry;

import gse.minu.GonsioRRSailingEngine;
import gse.minu.item.F18CatamaranItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class GSEItems {
    public static final Item F18_CATAMARAN = Registry.register(
            BuiltInRegistries.ITEM,
            GonsioRRSailingEngine.id("f18_catamaran"),
            new F18CatamaranItem(new Item.Properties().stacksTo(1))
    );

    private GSEItems() {}

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(F18_CATAMARAN));
    }
}
