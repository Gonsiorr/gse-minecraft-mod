package gse.minu.item;

import gse.minu.equipment.BoatEquipment;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

/** Forces GSE item registration and exposes starter entries to the creative inventory. */
public final class GSEItems {
    private GSEItems() {}

    public static void register() {
        // Force item registry entries to exist before Fabric freezes registries after initialization.
        F18MaterialItem materialItem = F18MaterialItem.INSTANCE;
        F18ComponentItem componentItem = F18ComponentItem.INSTANCE;
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(F18MaterialItem.create(F18MaterialItem.Material.FIBERGLASS, 1)));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(F18ComponentItem.create(BoatEquipment.Slot.HULL, 1)));
    }
}
