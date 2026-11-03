package gse.minu.workshop;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.inventory.ContainerLevelAccess;

/** GSE service and crafting station. */
public final class SailingCraftingStationBlock extends Block {
    public SailingCraftingStationBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        player.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
            @Override public Component getDisplayName() {
                return Component.translatable("block.gonsiorr-sailing-engine.sailing_crafting_station");
            }
            @Override public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) { return pos; }
            @Override public SailingCraftingMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory,
                                                             Player user) {
                return new SailingCraftingMenu(id, inventory, ContainerLevelAccess.create(level, pos));
            }
        });
        return InteractionResult.CONSUME;
    }
}
