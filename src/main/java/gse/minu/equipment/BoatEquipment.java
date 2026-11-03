package gse.minu.equipment;

import net.minecraft.nbt.CompoundTag;
import java.util.EnumMap;
import java.util.Map;

/** Persistent, validated component tiers for one F18. Tier zero means no installed component. */
public final class BoatEquipment {
    public enum Slot { HULL, DAGGERBOARDS, MAINSAIL, JIB, MAST, RUDDER, TRAMPOLINE, BOWSPRIT, GENNAKER, TRAPEZES, FOILING_ALLOY }

    private static final Map<Slot, Integer> MIN_TIER = Map.of(
            Slot.BOWSPRIT, 3, Slot.GENNAKER, 1, Slot.TRAPEZES, 0, Slot.FOILING_ALLOY, 1);
    private final EnumMap<Slot, Integer> tiers = new EnumMap<>(Slot.class);

    public BoatEquipment() {
        // F18 starts with Tier I core parts; optional equipment slots are empty.
        tiers.put(Slot.HULL, 1);
        tiers.put(Slot.DAGGERBOARDS, 1);
        tiers.put(Slot.MAINSAIL, 1);
        tiers.put(Slot.JIB, 1);
        tiers.put(Slot.MAST, 1);
        tiers.put(Slot.RUDDER, 1);
        tiers.put(Slot.TRAMPOLINE, 1);
        for (Slot slot : Slot.values()) tiers.putIfAbsent(slot, 0);
    }

    public int tier(Slot slot) { return tiers.getOrDefault(slot, 0); }

    public int remove(Slot slot) {
        if (slot == Slot.TRAMPOLINE) return 0;
        int previous = tier(slot);
        tiers.put(slot, 0);
        if (!isCompatible()) {
            tiers.put(slot, previous);
            return 0;
        }
        return previous;
    }

    /** Installs a tier if all F18 component compatibility limits remain satisfied. */
    public boolean install(Slot slot, int tier) {
        if (tier == 0) return this.tier(slot) == 0 || remove(slot) > 0;
        int min = MIN_TIER.getOrDefault(slot, 1);
        int max = switch (slot) {
            case FOILING_ALLOY, TRAPEZES, TRAMPOLINE -> 1;
            case GENNAKER -> 3;
            case DAGGERBOARDS -> tier == 10 ? 10 : 5;
            default -> 5;
        };
        if (tier < min || tier > max) return false;
        int previous = tier(slot);
        tiers.put(slot, tier);
        if (!isCompatible()) {
            tiers.put(slot, previous);
            return false;
        }
        return true;
    }

    public boolean hasFoilingSetup() {
        return tier(Slot.HULL) == 5 && tier(Slot.DAGGERBOARDS) == 10
                && tier(Slot.MAINSAIL) == 5 && tier(Slot.JIB) == 5
                && tier(Slot.MAST) == 5 && tier(Slot.RUDDER) == 5
                && tier(Slot.FOILING_ALLOY) == 1;
    }

    public boolean canDeployGennaker() {
        return tier(Slot.GENNAKER) > 0 && tier(Slot.MAST) > 0 && tier(Slot.BOWSPRIT) >= 3
                && tier(Slot.GENNAKER) <= Math.min(3, Math.min(tier(Slot.MAST), tier(Slot.BOWSPRIT)));
    }

    private boolean isCompatible() {
        int hull = tier(Slot.HULL);
        int boards = tier(Slot.DAGGERBOARDS);
        if ((boards > hull && boards != 10) || tier(Slot.RUDDER) > hull || tier(Slot.MAST) > hull) return false;
        if (tier(Slot.MAINSAIL) > tier(Slot.MAST) || tier(Slot.JIB) > tier(Slot.MAST)) return false;
        int gennakerMax = Math.min(3, Math.min(tier(Slot.MAST), tier(Slot.BOWSPRIT)));
        if (tier(Slot.GENNAKER) > gennakerMax) return false;
        return boards != 10 || (tier(Slot.FOILING_ALLOY) == 1 && hull == 5
                && tier(Slot.MAINSAIL) == 5 && tier(Slot.JIB) == 5
                && tier(Slot.MAST) == 5 && tier(Slot.RUDDER) == 5);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (Slot slot : Slot.values()) tag.putInt(slot.name(), tier(slot));
        return tag;
    }

    public static BoatEquipment load(CompoundTag tag) {
        BoatEquipment equipment = new BoatEquipment();
        for (Slot slot : Slot.values()) {
            if (!tag.contains(slot.name())) continue;
            int tier = tag.getInt(slot.name());
            if (!validSavedTier(slot, tier)) continue;
            equipment.tiers.put(slot, tier);
        }
        return equipment.isCompatible() ? equipment : new BoatEquipment();
    }

    private static boolean validSavedTier(Slot slot, int tier) {
        if (tier == 0) return true;
        return switch (slot) {
            case HULL, MAINSAIL, JIB, MAST, RUDDER -> tier >= 1 && tier <= 5;
            case DAGGERBOARDS -> (tier >= 1 && tier <= 5) || tier == 10;
            case TRAMPOLINE, TRAPEZES, FOILING_ALLOY -> tier == 1;
            case BOWSPRIT -> tier >= 3 && tier <= 5;
            case GENNAKER -> tier >= 1 && tier <= 3;
        };
    }
}
