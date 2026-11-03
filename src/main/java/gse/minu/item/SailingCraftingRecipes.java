package gse.minu.item;

import gse.minu.equipment.BoatEquipment;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** GSE's shapeless 5x5 workshop recipes. Exact 5x5 placements can be added after balance review. */
public final class SailingCraftingRecipes {
    private record Key(Item item, F18MaterialItem.Material material, BoatEquipment.Slot component, int tier) {}
    private record Cost(Key key, int count) {}
    public record Recipe(List<Cost> costs, ItemStack output) {
        public void consume(Container input, Player player) {
            for (Cost cost : costs) {
                int remaining = cost.count();
                for (int i = 0; i < 25 && remaining > 0; i++) {
                    ItemStack stack = input.getItem(i);
                    if (key(stack).equals(cost.key())) {
                        int taken = Math.min(remaining, stack.getCount());
                        input.removeItem(i, taken);
                        if (cost.key().item() == Items.HONEY_BOTTLE) {
                            for (int bottle = 0; bottle < taken; bottle++) {
                                ItemStack remainder = new ItemStack(Items.GLASS_BOTTLE);
                                if (!player.getInventory().add(remainder)) player.drop(remainder, false);
                            }
                        }
                        remaining -= taken;
                    }
                }
            }
        }
    }

    private static final List<Recipe> RECIPES = createRecipes();
    private SailingCraftingRecipes() {}

    public static Recipe find(List<ItemStack> stacks) {
        Map<Key, Integer> present = new HashMap<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) present.merge(key(stack), stack.getCount(), Integer::sum);
        }
        for (Recipe recipe : RECIPES) {
            Map<Key, Integer> needed = new HashMap<>();
            recipe.costs().forEach(c -> needed.merge(c.key(), c.count(), Integer::sum));
            if (present.equals(needed)) return recipe;
        }
        return null;
    }

    private static Key key(ItemStack stack) {
        if (stack.is(F18MaterialItem.INSTANCE)) return new Key(null, F18MaterialItem.material(stack), null, 0);
        if (stack.is(F18ComponentItem.INSTANCE)) return new Key(null, null, F18ComponentItem.slot(stack), F18ComponentItem.tier(stack));
        return new Key(stack.getItem(), null, null, 0);
    }

    private static Key item(Item item) { return new Key(item, null, null, 0); }
    private static Key mat(F18MaterialItem.Material material) { return new Key(null, material, null, 0); }
    private static Key component(BoatEquipment.Slot slot, int tier) { return new Key(null, null, slot, tier); }
    private static Cost c(Key key, int count) { return new Cost(key, count); }
    private static Recipe r(ItemStack output, Cost... costs) { return new Recipe(List.of(costs), output); }
    private static Recipe material(F18MaterialItem.Material out, int count, Cost... costs) {
        return r(F18MaterialItem.create(out, count), costs);
    }
    private static Recipe component(BoatEquipment.Slot out, int tier, Cost... costs) {
        return r(F18ComponentItem.create(out, tier), costs);
    }

    private static List<Recipe> createRecipes() {
        List<Recipe> r = new ArrayList<>();
        r.add(material(F18MaterialItem.Material.FIBERGLASS, 4, c(item(Items.SAND),4), c(item(Items.GLASS),2), c(item(Items.IRON_INGOT),1)));
        r.add(material(F18MaterialItem.Material.EPOXY_RESIN, 2, c(item(Items.COAL),2), c(item(Items.GLASS),1), c(item(Items.HONEY_BOTTLE),1)));
        r.add(material(F18MaterialItem.Material.GLASS_LAMINATE, 2, c(mat(F18MaterialItem.Material.FIBERGLASS),2), c(mat(F18MaterialItem.Material.EPOXY_RESIN),1), c(item(Items.IRON_INGOT),1)));
        r.add(material(F18MaterialItem.Material.REINFORCING_FIBER, 2, c(mat(F18MaterialItem.Material.FIBERGLASS),2), c(item(Items.STRING),1), c(item(Items.COPPER_INGOT),1)));
        r.add(material(F18MaterialItem.Material.METAL_FITTINGS, 4, c(item(Items.IRON_INGOT),3), c(item(Items.COPPER_INGOT),1)));
        r.add(material(F18MaterialItem.Material.HEAT_RESISTANT_FIBER, 2, c(item(Items.QUARTZ),2), c(item(Items.BLAZE_POWDER),2), c(item(Items.GLASS),1)));
        r.add(material(F18MaterialItem.Material.NETHER_RESIN, 2, c(item(Items.MAGMA_CREAM),1), c(item(Items.BLAZE_POWDER),1), c(item(Items.HONEY_BOTTLE),1)));
        r.add(material(F18MaterialItem.Material.NETHER_COMPOSITE, 1, c(mat(F18MaterialItem.Material.HEAT_RESISTANT_FIBER),1), c(mat(F18MaterialItem.Material.NETHER_RESIN),1), c(item(Items.IRON_INGOT),1)));
        r.add(material(F18MaterialItem.Material.END_FIBER, 2, c(item(Items.CHORUS_FRUIT),2), c(item(Items.ENDER_PEARL),1), c(item(Items.PHANTOM_MEMBRANE),1)));
        r.add(material(F18MaterialItem.Material.END_RESIN, 2, c(item(Items.CHORUS_FRUIT),1), c(item(Items.ENDER_PEARL),1), c(item(Items.HONEY_BOTTLE),1)));
        r.add(material(F18MaterialItem.Material.END_COMPOSITE, 1, c(mat(F18MaterialItem.Material.END_FIBER),1), c(mat(F18MaterialItem.Material.END_RESIN),1), c(mat(F18MaterialItem.Material.NETHER_COMPOSITE),1)));
        r.add(material(F18MaterialItem.Material.FOILING_CORE, 1, c(mat(F18MaterialItem.Material.END_COMPOSITE),2), c(item(Items.DIAMOND),2), c(item(Items.NETHERITE_INGOT),1)));

        Key laminate=mat(F18MaterialItem.Material.GLASS_LAMINATE), fittings=mat(F18MaterialItem.Material.METAL_FITTINGS), fiber=mat(F18MaterialItem.Material.REINFORCING_FIBER);
        Key fiberglass=mat(F18MaterialItem.Material.FIBERGLASS), epoxy=mat(F18MaterialItem.Material.EPOXY_RESIN);
        Key heat=mat(F18MaterialItem.Material.HEAT_RESISTANT_FIBER), netherResin=mat(F18MaterialItem.Material.NETHER_RESIN), netherComp=mat(F18MaterialItem.Material.NETHER_COMPOSITE);
        Key endFiber=mat(F18MaterialItem.Material.END_FIBER), endResin=mat(F18MaterialItem.Material.END_RESIN), endComp=mat(F18MaterialItem.Material.END_COMPOSITE);
        r.add(component(BoatEquipment.Slot.HULL,1,c(laminate,8),c(fittings,4),c(item(Items.OAK_PLANKS),4)));
        r.add(component(BoatEquipment.Slot.HULL,2,c(component(BoatEquipment.Slot.HULL,1),1),c(laminate,6),c(fiber,4),c(fittings,6)));
        r.add(component(BoatEquipment.Slot.HULL,3,c(component(BoatEquipment.Slot.HULL,2),1),c(laminate,8),c(fiber,6),c(fittings,8)));
        r.add(component(BoatEquipment.Slot.HULL,4,c(component(BoatEquipment.Slot.HULL,3),1),c(netherComp,1),c(heat,6),c(netherResin,4),c(fittings,8)));
        r.add(component(BoatEquipment.Slot.HULL,5,c(component(BoatEquipment.Slot.HULL,4),1),c(endComp,1),c(endFiber,6),c(endResin,4),c(fittings,8)));
        tierRecipes(r, BoatEquipment.Slot.DAGGERBOARDS, new Cost[][]{
                {c(laminate,4),c(fittings,4),c(fiber,2)}, {c(laminate,5),c(fiber,4),c(fittings,4)},
                {c(laminate,6),c(fiber,6),c(fittings,4)}, {c(netherComp,1),c(heat,4),c(netherResin,3)},
                {c(endComp,1),c(endFiber,4),c(endResin,3)}});
        tierRecipes(r, BoatEquipment.Slot.MAINSAIL, new Cost[][]{
                {c(fiberglass,6),c(item(Items.STRING),4),c(epoxy,2)}, {c(fiberglass,8),c(item(Items.STRING),6),c(epoxy,3)},
                {c(fiberglass,8),c(fiber,8),c(epoxy,4)}, {c(netherComp,1),c(heat,5),c(netherResin,3)},
                {c(endComp,1),c(endFiber,5),c(endResin,3)}});
        tierRecipes(r, BoatEquipment.Slot.JIB, new Cost[][]{
                {c(fiberglass,4),c(item(Items.STRING),3),c(epoxy,1)}, {c(fiberglass,5),c(item(Items.STRING),4),c(epoxy,2)},
                {c(fiberglass,6),c(fiber,4),c(epoxy,2)}, {c(netherComp,1),c(heat,3),c(netherResin,2)},
                {c(endComp,1),c(endFiber,3),c(endResin,2)}});
        tierRecipes(r, BoatEquipment.Slot.MAST, new Cost[][]{
                {c(item(Items.OAK_PLANKS),8),c(fittings,6),c(fiber,2)}, {c(item(Items.OAK_PLANKS),8),c(fittings,8),c(fiber,4)},
                {c(laminate,6),c(fittings,8),c(fiber,4)}, {c(netherComp,1),c(heat,4),c(netherResin,4)},
                {c(endComp,1),c(endFiber,4),c(endResin,4)}});
        tierRecipes(r, BoatEquipment.Slot.RUDDER, new Cost[][]{
                {c(laminate,4),c(fittings,6),c(fiber,2)}, {c(laminate,5),c(fittings,8),c(fiber,3)},
                {c(laminate,6),c(fittings,8),c(fiber,4)}, {c(netherComp,1),c(heat,4),c(netherResin,3)},
                {c(endComp,1),c(endFiber,4),c(endResin,3)}});
        r.add(component(BoatEquipment.Slot.BOWSPRIT,3,c(laminate,4),c(fittings,6),c(fiber,2)));
        r.add(component(BoatEquipment.Slot.BOWSPRIT,4,c(component(BoatEquipment.Slot.BOWSPRIT,3),1),c(netherComp,1),c(heat,3),c(netherResin,3)));
        r.add(component(BoatEquipment.Slot.BOWSPRIT,5,c(component(BoatEquipment.Slot.BOWSPRIT,4),1),c(endComp,1),c(endFiber,3),c(endResin,3)));
        r.add(component(BoatEquipment.Slot.GENNAKER,1,c(fiberglass,6),c(item(Items.STRING),3),c(epoxy,2)));
        r.add(component(BoatEquipment.Slot.GENNAKER,2,c(component(BoatEquipment.Slot.GENNAKER,1),1),c(fiber,4),c(epoxy,3)));
        r.add(component(BoatEquipment.Slot.GENNAKER,3,c(component(BoatEquipment.Slot.GENNAKER,2),1),c(netherComp,1),c(heat,4),c(netherResin,2)));
        r.add(component(BoatEquipment.Slot.TRAPEZES,1,c(item(Items.CHAIN),4),c(item(Items.IRON_INGOT),4),c(item(Items.STRING),2)));
        r.add(component(BoatEquipment.Slot.TRAMPOLINE,1,c(item(Items.STRING),6),c(item(Items.OAK_PLANKS),4),c(item(Items.LEATHER),2)));
        r.add(component(BoatEquipment.Slot.FOILING_ALLOY,1,c(mat(F18MaterialItem.Material.FOILING_CORE),1),c(item(Items.ENDER_PEARL),2),c(item(Items.PRISMARINE_SHARD),2),c(item(Items.GOLD_INGOT),2)));
        r.add(component(BoatEquipment.Slot.DAGGERBOARDS,10,c(component(BoatEquipment.Slot.DAGGERBOARDS,5),1),c(component(BoatEquipment.Slot.FOILING_ALLOY,1),2),c(endComp,2),c(endFiber,4),c(endResin,2),c(fittings,4)));
        r.add(r(new ItemStack(F18Item.INSTANCE), c(component(BoatEquipment.Slot.HULL,1),1),
                c(component(BoatEquipment.Slot.DAGGERBOARDS,1),1), c(component(BoatEquipment.Slot.MAINSAIL,1),1),
                c(component(BoatEquipment.Slot.JIB,1),1), c(component(BoatEquipment.Slot.MAST,1),1),
                c(component(BoatEquipment.Slot.RUDDER,1),1), c(component(BoatEquipment.Slot.TRAMPOLINE,1),1)));
        return List.copyOf(r);
    }

    private static void tierRecipes(List<Recipe> recipes, BoatEquipment.Slot slot, Cost[][] tiers) {
        for (int i = 0; i < tiers.length; i++) {
            int tier = i + 1;
            List<Cost> costs = new ArrayList<>();
            if (tier == 2) costs.add(c(component(slot,1),1));
            if (tier == 3) costs.add(c(component(slot,2),1));
            if (tier == 4) costs.add(c(component(slot,3),1));
            if (tier == 5) costs.add(c(component(slot,4),1));
            costs.addAll(List.of(tiers[i]));
            recipes.add(component(slot,tier,costs.toArray(Cost[]::new)));
        }
    }
}
