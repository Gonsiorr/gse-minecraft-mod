package gse.minu.workshop;

import gse.minu.GonsioRRSailingEngine;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.BlockPos;

public final class SailingCraftingStationRegistry {
    public static final Block STATION = Registry.register(BuiltInRegistries.BLOCK,
            GonsioRRSailingEngine.id("sailing_crafting_station"),
            new SailingCraftingStationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.0F)));
    public static final Item STATION_ITEM = Registry.register(BuiltInRegistries.ITEM,
            GonsioRRSailingEngine.id("sailing_crafting_station"), new BlockItem(STATION, new Item.Properties()));
    public static final MenuType<SailingCraftingMenu> MENU = Registry.register(BuiltInRegistries.MENU,
            GonsioRRSailingEngine.id("sailing_crafting_station"),
            new ExtendedScreenHandlerType<>((id, inventory, pos) ->
                    new SailingCraftingMenu(id, inventory,
                            net.minecraft.world.inventory.ContainerLevelAccess.create(inventory.player.level(), pos)),
                    BlockPos.STREAM_CODEC));

    private SailingCraftingStationRegistry() {}

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(STATION_ITEM));
    }
}
