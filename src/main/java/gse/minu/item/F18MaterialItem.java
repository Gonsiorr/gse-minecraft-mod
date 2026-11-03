package gse.minu.item;

import gse.minu.GonsioRRSailingEngine;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.Locale;

/** One stackable material item with its GSE material kind stored in data components. */
public final class F18MaterialItem extends Item {
    public enum Material {
        FIBERGLASS("Fiberglass"), EPOXY_RESIN("Epoxy Resin"), GLASS_LAMINATE("Glass Laminate"),
        REINFORCING_FIBER("Reinforcing Fiber"), METAL_FITTINGS("Metal Fittings"),
        HEAT_RESISTANT_FIBER("Heat Resistant Fiber"), NETHER_RESIN("Nether Resin"),
        NETHER_COMPOSITE("Nether Composite"), END_FIBER("End Fiber"), END_RESIN("End Resin"),
        END_COMPOSITE("End Composite"), FOILING_CORE("Foiling Core");
        private final String displayName;
        Material(String displayName) { this.displayName = displayName; }
        public String displayName() { return displayName; }
    }

    public static final F18MaterialItem INSTANCE = Registry.register(BuiltInRegistries.ITEM,
            GonsioRRSailingEngine.id("f18_material"), new F18MaterialItem(new Properties()));

    private F18MaterialItem(Properties properties) { super(properties); }

    public static ItemStack create(Material material, int count) {
        ItemStack stack = new ItemStack(INSTANCE, count);
        CompoundTag tag = new CompoundTag();
        tag.putString("Material", material.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static Material material(ItemStack stack) {
        if (!stack.is(INSTANCE)) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains("Material")) return null;
        try { return Material.valueOf(data.copyTag().getString("Material")); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    @Override
    public Component getName(ItemStack stack) {
        Material material = material(stack);
        return material == null ? Component.translatable("item.gse.material.unknown")
                : Component.translatable("item.gse.material." + material.name().toLowerCase(Locale.ROOT));
    }
}
