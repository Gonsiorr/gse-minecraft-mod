package gse.minu.workshop;

import gse.minu.equipment.BoatEquipment;
import gse.minu.item.F18ComponentItem;
import gse.minu.item.F18Item;
import gse.minu.item.SailingCraftingRecipes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.BannerPatternItem;

/** 5x5 GSE crafting grid plus component service slots for a recovered F18. */
public final class SailingCraftingMenu extends AbstractContainerMenu {
    public static final int TYPE_ID = 0;
    private final SimpleContainer inventory = new SimpleContainer(28);
    private final ContainerLevelAccess access;
    private final ContainerData data = new SimpleContainerData(1);
    private boolean updatingResult;

    public SailingCraftingMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public SailingCraftingMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(SailingCraftingStationRegistry.MENU, containerId);
        this.access = access;
        addDataSlots(data);
        this.inventory.addListener(container -> updateResult());
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                int index = row * 5 + column;
                addSlot(new Slot(inventory, index, 8 + column * 18, 36 + row * 18) {
                    @Override public boolean isActive() { return data.get(0) == 0; }
                });
            }
        }
        addSlot(new Slot(inventory, 25, 122, 72) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean isActive() { return data.get(0) == 0; }
            @Override public void onTake(Player player, ItemStack stack) {
                SailingCraftingRecipes.Recipe recipe = currentRecipe();
                if (recipe != null) {
                    updatingResult = true;
                    recipe.consume(inventory, player);
                    inventory.setItem(25, ItemStack.EMPTY);
                    updatingResult = false;
                    updateResult();
                }
                super.onTake(player, stack);
            }
        });
        addSlot(new Slot(inventory, 26, 158, 46) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(F18Item.INSTANCE); }
            @Override public boolean isActive() { return data.get(0) != 0; }
        });
        addSlot(new Slot(inventory, 27, 158, 82) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(F18ComponentItem.INSTANCE) || stack.getItem() instanceof DyeItem
                        || stack.getItem() instanceof BannerPatternItem;
            }
            @Override public boolean isActive() { return data.get(0) != 0; }
        });
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 150 + row * 18));
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 208));
    }

    private SailingCraftingRecipes.Recipe currentRecipe() {
        return SailingCraftingRecipes.find(inventory.getItems().subList(0, 25));
    }

    private void updateResult() {
        if (updatingResult) return;
        updatingResult = true;
        SailingCraftingRecipes.Recipe recipe = currentRecipe();
        inventory.setItem(25, recipe == null ? ItemStack.EMPTY : recipe.output().copy());
        updatingResult = false;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 1 && tab() == 1) return installComponent();
        if (id == 2 && tab() == 1) return removeComponent();
        if (id == 3 && tab() == 2) return customizeSail(false);
        if (id == 4 && tab() == 2) return customizeSail(true);
        if (id == 5 && tab() == 2) return customizeAccessory(BoatEquipment.Slot.TRAMPOLINE);
        if (id == 6 && tab() == 2) return customizeAccessory(BoatEquipment.Slot.TRAPEZES);
        if (id >= 10 && id <= 12) { data.set(0, id - 10); return true; }
        return false;
    }

    public int tab() { return data.get(0); }
    public void setClientTab(int tab) { data.set(0, tab); }

    public String getBoatName() {
        ItemStack boat = inventory.getItem(26);
        if (!boat.is(F18Item.INSTANCE)) return "F18";
        Component name = boat.get(DataComponents.CUSTOM_NAME);
        return name == null ? "F18" : name.getString();
    }

    public boolean applyBoatName(String requestedName) {
        ItemStack boat = inventory.getItem(26);
        if (tab() != 2 || !boat.is(F18Item.INSTANCE)) return false;
        String name = requestedName.replace('\n', ' ').replace('\r', ' ').strip();
        if (name.length() > 32) name = name.substring(0, 32);
        if (name.isEmpty()) name = "F18";
        boat.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        CompoundTag tag = customData(boat);
        tag.putString("CustomName", name);
        setCustomData(boat, tag);
        return true;
    }

    private boolean installComponent() {
        ItemStack boat = inventory.getItem(26);
        ItemStack component = inventory.getItem(27);
        BoatEquipment.Slot slot = F18ComponentItem.slot(component);
        int tier = F18ComponentItem.tier(component);
        if (!boat.is(F18Item.INSTANCE) || slot == null || tier <= 0) return false;
        CompoundTag data = customData(boat);
        BoatEquipment equipment = equipment(data);
        int previous = equipment.tier(slot);
        int incomingColor = F18ComponentItem.color(component);
        if (!equipment.install(slot, tier)) return false;
        data.put("Equipment", equipment.save());
        String colorKey = colorKey(slot);
        int previousColor = colorKey == null || !data.contains(colorKey) ? 0xFFFFFFFF : data.getInt(colorKey);
        if (colorKey != null) data.putInt(colorKey, incomingColor);
        String patternKey = patternKey(slot);
        String previousPattern = patternKey == null ? "" : data.getString(patternKey);
        if (patternKey != null) data.putString(patternKey, F18ComponentItem.pattern(component));
        setCustomData(boat, data);
        inventory.setItem(27, previous > 0
                ? F18ComponentItem.create(slot, previous, previousColor, previousPattern) : ItemStack.EMPTY);
        return true;
    }

    private boolean removeComponent() {
        ItemStack boat = inventory.getItem(26);
        ItemStack component = inventory.getItem(27);
        BoatEquipment.Slot slot = F18ComponentItem.slot(component);
        if (!boat.is(F18Item.INSTANCE) || slot == null) return false;
        CompoundTag data = customData(boat);
        BoatEquipment equipment = equipment(data);
        int removed = equipment.remove(slot);
        if (removed <= 0) return false;
        data.put("Equipment", equipment.save());
        if (slot == BoatEquipment.Slot.GENNAKER) data.putBoolean("GennakerDeployed", false);
        String colorKey = colorKey(slot);
        int color = colorKey == null || !data.contains(colorKey) ? 0xFFFFFFFF : data.getInt(colorKey);
        if (colorKey != null) data.putInt(colorKey, 0xFFFFFFFF);
        String patternKey = patternKey(slot);
        String pattern = patternKey == null ? "" : data.getString(patternKey);
        if (patternKey != null) data.putString(patternKey, "");
        setCustomData(boat, data);
        inventory.setItem(27, F18ComponentItem.create(slot, removed, color, pattern));
        return true;
    }

    private boolean customizeSail(boolean jib) {
        ItemStack boat = inventory.getItem(26);
        ItemStack input = inventory.getItem(27);
        if (!boat.is(F18Item.INSTANCE)) return false;
        CompoundTag data = customData(boat);
        if (input.getItem() instanceof DyeItem dye) {
            data.putInt(jib ? "JibColor" : "MainsailColor", dye.getDyeColor().getTextureDiffuseColor());
            input.shrink(1);
        } else if (input.getItem() instanceof BannerPatternItem patternItem) {
            String path = patternItem.getBannerPattern().location().getPath();
            if (path.startsWith("pattern_item/")) path = path.substring("pattern_item/".length());
            data.putString(jib ? "JibPattern" : "MainsailPattern", path);
            input.shrink(1);
        } else {
            return false;
        }
        setCustomData(boat, data);
        inventory.setItem(27, input.isEmpty() ? ItemStack.EMPTY : input);
        return true;
    }

    private boolean customizeAccessory(BoatEquipment.Slot slot) {
        ItemStack boat = inventory.getItem(26);
        ItemStack input = inventory.getItem(27);
        if (!boat.is(F18Item.INSTANCE) || !(input.getItem() instanceof DyeItem dye)) return false;
        CompoundTag data = customData(boat);
        data.putInt(slot == BoatEquipment.Slot.TRAMPOLINE ? "TrampolineColor" : "TrapezeColor",
                dye.getDyeColor().getTextureDiffuseColor());
        input.shrink(1);
        setCustomData(boat, data);
        inventory.setItem(27, input.isEmpty() ? ItemStack.EMPTY : input);
        return true;
    }

    private static String colorKey(BoatEquipment.Slot slot) {
        return switch (slot) {
            case MAINSAIL -> "MainsailColor";
            case JIB -> "JibColor";
            case TRAMPOLINE -> "TrampolineColor";
            case TRAPEZES -> "TrapezeColor";
            default -> null;
        };
    }

    private static String patternKey(BoatEquipment.Slot slot) {
        return switch (slot) {
            case MAINSAIL -> "MainsailPattern";
            case JIB -> "JibPattern";
            default -> null;
        };
    }

    private static CompoundTag customData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static BoatEquipment equipment(CompoundTag data) {
        return data.contains("Equipment", CompoundTag.TAG_COMPOUND)
                ? BoatEquipment.load(data.getCompound("Equipment")) : new BoatEquipment();
    }

    private static void setCustomData(ItemStack stack, CompoundTag data) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return empty;
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index == 25) {
            if (!moveItemStackTo(original, 28, 64, true)) return empty;
            slot.onTake(player, original);
        } else if (index >= 28) {
            if (original.is(F18Item.INSTANCE)) {
                if (!moveItemStackTo(original, 26, 27, false)) return empty;
            } else if (original.is(F18ComponentItem.INSTANCE)) {
                if (!moveItemStackTo(original, 27, 28, false)) return empty;
            } else if (!moveItemStackTo(original, 0, 25, false)) return empty;
        } else if (!moveItemStackTo(original, 28, 64, false)) return empty;
        if (original.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, SailingCraftingStationRegistry.STATION);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide) return;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (i == 25) continue;
            ItemStack remaining = inventory.removeItemNoUpdate(i);
            if (!remaining.isEmpty() && !player.getInventory().add(remaining)) {
                player.drop(remaining, false);
            }
        }
    }
}
