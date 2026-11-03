package gse.minu.physics;

import gse.minu.entity.F18Entity;
import gse.minu.wind.WindManager;
import gse.minu.equipment.BoatEquipment;
import gse.minu.boat.F18BoatDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Deterministic first-pass F18 polar, trim, inertia, drag and wind drift calculations. */
public final class SailingPhysicsEngine {
    public static final int LEFT = 1, RIGHT = 2, TRIM_IN = 4, TRIM_OUT = 8;
    private SailingPhysicsEngine() {}

    public static double tick(F18Entity boat, WindManager.Snapshot wind, int controls) {
        boolean skipper = boat.getControllingPassenger() != null;
        boolean afloat = boat.isAfloat();
        boolean left = (controls & LEFT) != 0;
        boolean right = (controls & RIGHT) != 0;
        boolean trimIn = (controls & TRIM_IN) != 0;
        boolean trimOut = (controls & TRIM_OUT) != 0;
        float trim = boat.getSailTrim();
        if (skipper) {
            if (trimIn && !trimOut) trim = Math.min(1.0F, trim + 0.005F);
            if (trimOut && !trimIn) trim = Math.max(0.0F, trim - 0.0075F);
        }
        boat.setSailTrim(trim);

        float rudder = skipper && boat.getEquipment().tier(BoatEquipment.Slot.RUDDER) > 0
                ? ((right ? 1.0F : 0.0F) - (left ? 1.0F : 0.0F)) : 0.0F;
        boat.setRudderAngle(rudder);
        Vec3 velocity = boat.getDeltaMovement();
        float currentYaw = boat.getYRot();
        double preTurnForward = heading(currentYaw).dot(new Vec3(velocity.x, 0.0D, velocity.z));
        float speedFactor = (float) Mth.clamp(Math.abs(preTurnForward) * 20.0D / 4.0D, 0.50D, 1.0D);
        if (rudder != 0.0F && !boat.isAnchored() && afloat) {
            double rudderTierFactor = 1.0D + Math.max(0, boat.getEquipment().tier(BoatEquipment.Slot.RUDDER) - 1) * 0.08D;
            // Minecraft yaw grows to port; positive rudder input is starboard/right.
            float proposedYaw = currentYaw - (float) (rudder * 1.2D * rudderTierFactor * speedFactor);
            if (boat.isGennakerDeployed()) {
                float windFromDegrees = (float) wind.directionFromDegrees();
                float before = Mth.wrapDegrees(currentYaw + 180.0F - windFromDegrees);
                float after = Mth.wrapDegrees(proposedYaw + 180.0F - windFromDegrees);
                if (Math.abs(before) >= 7.5F && Math.abs(after) < 7.5F) {
                    after = Math.copySign(7.5F, before);
                    proposedYaw = windFromDegrees + after - 180.0F;
                }
            }
            boat.setYRot(proposedYaw);
        }
        boat.updateTackSide(wind.directionFromDegrees());

        Vec3 forward = heading(boat.getYRot());
        Vec3 rightVector = new Vec3(forward.z, 0.0D, -forward.x);
        double forwardSpeed = new Vec3(velocity.x, 0.0D, velocity.z).dot(forward);
        double sideSpeed = new Vec3(velocity.x, 0.0D, velocity.z).dot(rightVector);
        double targetSpeed = 0.0D;
        double windFrom = Math.toRadians(wind.directionFromDegrees());
        Vec3 windSource = new Vec3(-Math.sin(windFrom), 0.0D, -Math.cos(windFrom));
        double angle = Math.toDegrees(Math.acos(Mth.clamp(forward.dot(windSource), -1.0D, 1.0D)));
        BoatEquipment equipment = boat.getEquipment();

        if (skipper && afloat && boat.isSailsRaised() && !boat.isAnchored() && wind.knots() > 0.0D
                && equipment.tier(BoatEquipment.Slot.HULL) > 0
                && equipment.tier(BoatEquipment.Slot.MAST) > 0
                && equipment.tier(BoatEquipment.Slot.MAINSAIL) > 0
                && equipment.tier(BoatEquipment.Slot.JIB) > 0) {
            double polar = polar(angle);
            double trimFactor = trimEfficiency(Math.abs(trim * 100.0D - optimalTrim(angle)));
            double windFactor = windEfficiency(wind.knots(), equipment.tier(BoatEquipment.Slot.HULL));
            double gennaker = boat.isGennakerDeployed() && equipment.canDeployGennaker()
                    ? gennakerFactor(angle, wind.knots()) : 1.0D;
            targetSpeed = hullSpeed(equipment) * polar * trimFactor * windFactor * gennaker;
        }

        double desiredForward = targetSpeed / 20.0D;
        double sailAccel = switch (equipment.tier(BoatEquipment.Slot.MAINSAIL)) {
            case 2 -> 1.15D; case 3 -> 1.30D; case 4 -> 1.45D; case 5 -> 1.60D; default -> 1.0D;
        };
        sailAccel *= 1.0D + Math.max(0, equipment.tier(BoatEquipment.Slot.MAST) - 1) * 0.02D;
        double inertiaFactor = 180.0D / F18BoatDefinition.MASS_KG;
        double maxAccelPerTick = 1.15D * inertiaFactor * sailAccel / 400.0D;
        double nextForward;
        if (desiredForward > forwardSpeed) {
            double remainingRatio = desiredForward <= 0.0D ? 0.0D : Mth.clamp((desiredForward-forwardSpeed)/desiredForward, 0.0D, 1.0D);
            double acceleration = maxAccelPerTick * (0.35D + 0.65D * remainingRatio);
            nextForward = Math.min(desiredForward, forwardSpeed + acceleration);
        } else {
            double speedMps = Math.abs(forwardSpeed) * 20.0D;
            double dragScale = switch (equipment.tier(BoatEquipment.Slot.HULL)) {
                case 2 -> 0.95D; case 3 -> 0.90D; case 4 -> 0.85D; case 5, 10 -> 0.80D; default -> 1.0D;
            };
            double drag = (0.18D + speedMps * 0.055D) * dragScale / 400.0D;
            nextForward = forwardSpeed < 0.0D ? Math.min(0.0D, forwardSpeed + drag) : Math.max(desiredForward, forwardSpeed - drag);
        }

        int boardTier = equipment.tier(BoatEquipment.Slot.DAGGERBOARDS);
        double daggerboardFactor = boardTier == 0 ? 0.9995D
                : boardTier >= 10 ? 0.975D : 0.997D - Math.max(0, boardTier - 1) * 0.003D;
        sideSpeed *= daggerboardFactor;
        if (skipper && afloat && !boat.isAnchored()) {
            Vec3 windToward = windSource.scale(-1.0D);
            double windForce = Math.min(0.0015D, wind.knots() * 0.000035D);
            double driftCoefficient = interpolate(angle, new double[][]{{0,.95},{30,.85},{60,.70},{90,.62},{120,.45},{150,.28},{180,.12}});
            sideSpeed += windToward.dot(rightVector) * windForce * driftCoefficient;
            if (!boat.isSailsRaised() || polar(angle) < 0.12D || trim < 0.35F) {
                nextForward += windToward.dot(forward) * windForce * 0.30D;
            }
        }
        if (boat.isAnchored()) {
            nextForward = 0.0D;
            sideSpeed = 0.0D;
        }

        // On a flat shoreline, carry the boat onto dry ground by roughly one block per m/s,
        // capped at four blocks. Below 1 m/s it settles at the water's edge.
        if (!afloat && !boat.isAnchored()) {
            double speedPerTick = Math.sqrt(nextForward * nextForward + sideSpeed * sideSpeed);
            double speedMps = speedPerTick * 20.0D;
            if (speedMps < 1.0D) {
                nextForward = 0.0D;
                sideSpeed = 0.0D;
            } else {
                double beachDistance = Math.min(speedMps, 4.0D);
                double perTickDeceleration = speedPerTick * speedPerTick / (2.0D * beachDistance);
                double nextMagnitude = Math.max(0.0D, speedPerTick - perTickDeceleration);
                double ratio = speedPerTick == 0.0D ? 0.0D : nextMagnitude / speedPerTick;
                nextForward *= ratio;
                sideSpeed *= ratio;
            }
        }

        // Leaving the helm drops the sails and lets the catamaran coast to a stop.
        // Once it has settled, the anchor engages automatically as specified.
        if (!skipper && boat.hasEverHadSkipper()
                && Math.abs(nextForward) < 0.0015D && Math.abs(sideSpeed) < 0.0015D) {
            nextForward = 0.0D;
            sideSpeed = 0.0D;
            boat.setAnchored(true);
        }

        Vec3 horizontal = forward.scale(nextForward).add(rightVector.scale(sideSpeed));
        boat.setDeltaMovement(horizontal.x, velocity.y, horizontal.z);
        boat.setSpeedMps((float) (Math.sqrt(horizontal.x*horizontal.x + horizontal.z*horizontal.z) * 20.0D));
        return targetSpeed;
    }

    private static Vec3 heading(float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians));
    }

    private static double polar(double a) {
        return interpolate(a, new double[][]{{0,0},{7.5,0},{30,.65},{45,.85},{60,.96},{70,1.00},{90,.97},{120,.94},{150,.87},{180,.76}});
    }

    private static double optimalTrim(double a) {
        return interpolate(a, new double[][]{{0,100},{7.5,100},{15,100},{30,95},{45,85},{70,70},{90,60},{120,40},{150,25},{180,15}});
    }

    private static double trimEfficiency(double deviation) {
        return interpolate(deviation, new double[][]{{0,1.0},{5,.99},{10,.95},{15,.88},{20,.78},{30,.55},{40,.30},{50,.10},{100,.10}});
    }

    private static double hullSpeed(BoatEquipment equipment) {
        if (equipment.tier.;ëKh‘éì¶»§q«^t();
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
