package gse.minu.item;

import gse.minu.entity.F18CatamaranEntity;
import gse.minu.registry.GSEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Places the catamaran on a water block when used against it. */
public final class F18CatamaranItem extends Item {
    public F18CatamaranItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos waterPos = context.getClickedPos();
        if (!level.getFluidState(waterPos).is(FluidTags.WATER)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            Player player = context.getPlayer();
            F18CatamaranEntity catamaran = new F18CatamaranEntity(GSEEntities.F18_CATAMARAN, level);
            catamaran.setPos(waterPos.getX() + 0.5D, waterPos.getY() + 0.2D, waterPos.getZ() + 0.5D);
            if (player != null) {
                catamaran.setYRot(player.getYRot());
            }
            if (level.addFreshEntity(catamaran) && player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
