package gse.minu.item;

import gse.minu.GonsioRRSailingEngine;
import gse.minu.entity.F18Entity;
import gse.minu.equipment.BoatEquipment;
import gse.minu.registry.BoatRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Places the F18 when the player uses the item while aiming at water. */
public final class F18Item extends Item {
    public static final F18Item INSTANCE = Registry.register(
            BuiltInRegistries.ITEM, GonsioRRSailingEngine.id("f18"),
            new F18Item(new Item.Properties().stacksTo(1))
    );

    private F18Item(Properties properties) {
        super(properties);
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(INSTANCE));
    }

    public static ItemStack createStackFrom(F18Entity boat) {
        ItemStack stack = new ItemStack(INSTANCE);
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putBoolean("SailsRaised", boat.isSailsRaised());
        data.putBoolean("GennakerDeployed", boat.isGennakerDeployed());
        data.putBoolean("Anchored", boat.isAnchored());
        data.putFloat("SailTrim", boat.getSailTrim());
        data.putInt("MainsailColor", boat.getMainsailColor());
        data.putInt("JibColor", boat.getJibColor());
        data.putInt("TrampolineColor", boat.getTrampolineColor());
        data.putInt("TrapezeColor", boat.getTrapezeColor());
        data.putString("MainsailPattern", boat.getMainsailPattern());
        data.putString("JibPattern", boat.getJibPattern());
        data.put("Equipment", boat.getEquipment().save());
        if (boat.hasCustomName()) {
            data.putString("CustomName", boat.getCustomName().getString());
            stack.set(DataComponents.CUSTOM_NAME, boat.getCustomName());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hit = player.pick(5.0D, 1.0F, true);
        if (hit instanceof BlockHitResult blockHit
                && level.getFluidState(blockHit.getBlockPos()).is(FluidTags.WATER)) {
            InteractionResult result = placeAt(level, player, stack, blockHit.getBlockPos(), blockHit.getLocation());
            return result.consumesAction()
                    ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
                    : InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos waterPos = context.getClickedPos();
        if (!level.getFluidState(waterPos).is(FluidTags.WATER)) {
            return InteractionResult.PASS;
        }
        return placeAt(level, context.getPlayer(), context.getItemInHand(), waterPos, context.getClickLocation());
    }

    private InteractionResult placeAt(Level level, Player player, ItemStack stack, BlockPos waterPos, Vec3 hitPos) {
        if (!level.getFluidState(waterPos).is(FluidTags.WATER)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            F18Entity f18 = new F18Entity(BoatRegistry.F18, level);
            f18.setPos(hitPos.x, hitPos.y, hitPos.z);
            f18.setYRot(player.getYRot());
            f18.refreshDimensions();
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            net.minecraft.nbt.CompoundTag saved = customData == null ? new net.minecraft.nbt.CompoundTag() : customData.copyTag();
            Component itemName = stack.get(DataComponents.CUSTOM_NAME);
            f18.setCustomName(saved.contains("CustomName") ? Component.literal(saved.getString("CustomName"))
                    : itemName == null ? Component.literal("F18") : itemName);
            f18.setSailsRaised(!saved.contains("SailsRaised") || saved.getBoolean("SailsRaised"));
            f18.setGennakerDeployed(saved.getBoolean("GennakerDeployed"));
            f18.setAnchored(saved.getBoolean("Anchored"));
            f18.setSailTrim(saved.contains("SailTrim") ? saved.getFloat("SailTrim") : 0.70F);
            f18.setMainsailColor(saved.contains("MainsailColor") ? saved.getInt("MainsailColor") : 0xFFFFFF);
            f18.setJibColor(saved.contains("JibColor") ? saved.getInt("JibColor") : 0xFFFFFF);
            f18.setTrampolineColor(saved.contains("TrampolineColor") ? saved.getInt("TrampolineColor") : 0xFFFFFF);
            f18.setTrapezeColor(saved.contains("TrapezeColor") ? saved.getInt("TrapezeColor") : 0xFFFFFF);
            f18.setMainsailPattern(saved.getString("MainsailPattern"));
            f18.setJibPattern(saved.getString("JibPattern"));
            if (saved.contains("Equipment", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                f18.setEquipment(BoatEquipment.load(saved.getCompound("Equipment")));
            }
            if (!level.addFreshEntity(f18)) {
                return InteractionResult.FAIL;
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
