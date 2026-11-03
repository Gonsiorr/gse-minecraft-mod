package gse.minu.item;

import gse.minu.GonsioRRSailingEngine;
import gse.minu.equipment.BoatEquipment;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import java.util.Locale;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Craftable GSE component item. Its slot and tier are preserved when removed from a catamaran. */
public final class F18ComponentItem extends Item {
    public static final F18ComponentItem INSTANCE = Registry.register(BuiltInRegistries.ITEM,
            GonsioRRSailingEngine.id("f18_component"), new F18ComponentItem(new Properties().stacksTo(1)));

    private F18ComponentItem(Properties properties) { super(properties); }

    public static ItemStack create(BoatEquipment.Slot slot, int tier) {
        return create(slot, tier, 0xFFFFFFFF);
    }

    public static ItemStack create(BoatEquipment.Slot slot, int tier, int color) {
        return create(slot, tier, color, "");
    }

    public static ItemStack create(BoatEquipment.Slot slot, int tier, int color, String pattern) {
        ItemStack stack = new ItemStack(INSTANCE);
        CompoundTag tag = new CompoundTag();
        tag.putString("Slot", slot.name());
        tag.putInt("Tier", tier);
        tag.putInt("Color", color | 0xFF000000);
        tag.putString("Pattern", pattern == null ? "" : pattern);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static BoatEquipment.Slot slot(ItemStack stack) {
        if (!stack.is(INSTANCE)) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        try { return BoatEquipment.Slot.valueOf(data.copyTag().getString("Slot")); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    public static int tier(ItemStack stack) {
        if (!stack.is(INSTANCE)) return 0;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? 0 : data.copyTag().getInt("Tier");
    }

    public static int color(ItemStack stack) {
        if (!stack.is(INSTANCE)) return 0xFFFFFFFF;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0xFFFFFFFF;
        CompoundTag tag = data.copyTag();
        return tag.contains("Color") ? tag.getInt("Color") | 0xFF000000 : 0xFFFFFFFF;
    }

    public static String pattern(ItemStack stack) {
        if (!stack.is(INSTANCE)) return "";
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? "" : data.copyTag().getString("Pattern");
    }

    @Override
    public Component getName(ItemStack stack) {
        BoatEquipment.Slot slot = slot(stack);
        if (slot == null) return Component.translatable("item.gse.component.unknown");
        net.minecraft.network.chat.MutableComponent name =
                Component.translatable("item.gse.component." + slot.name().toLowerCase(Locale.ROOT));
        if (slot == BoatEquipment.Slot.TRAMPOLINE || slot == BoatEquipment.Slot.TRAPEZES
                || slot == BoatEquipment.Slot.FOILING_ALLOY) return name;
        int tier = tier(stack);
        return name.append(Component.literal(tier == 10 ? " X" : " " + toRoman(tier)));
    }

    private static String toRoman(int value) {
        return switch (value) { case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> "?"; };
    }
}
